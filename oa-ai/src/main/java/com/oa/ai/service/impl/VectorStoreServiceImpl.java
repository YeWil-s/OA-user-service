package com.oa.ai.service.impl;

import com.oa.ai.entity.KnowledgeDoc;
import com.oa.ai.mapper.KnowledgeDocMapper;
import com.oa.ai.service.VectorStoreService;
import com.oa.ai.vo.SourceRefVO;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.search.SearchProtocol;
import redis.clients.jedis.search.SearchProtocol.SearchCommand;
import redis.clients.jedis.util.SafeEncoder;

import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class VectorStoreServiceImpl implements VectorStoreService {

    private static final Logger log = LoggerFactory.getLogger(VectorStoreServiceImpl.class);
    private static final String PREFIX = "doc:knowledge:";

    private final StringRedisTemplate redisTemplate;
    private final JedisPool jedisPool;
    private final KnowledgeDocMapper knowledgeDocMapper;

    public VectorStoreServiceImpl(StringRedisTemplate redisTemplate, JedisPool jedisPool,
                                  KnowledgeDocMapper knowledgeDocMapper) {
        this.redisTemplate = redisTemplate;
        this.jedisPool = jedisPool;
        this.knowledgeDocMapper = knowledgeDocMapper;
    }

    @Override
    public void store(Long docId, String title, String content, String category, String tags, String accessRoles, float[] embedding) {
        String key = PREFIX + docId;
        Map<String, String> fields = new HashMap<>();
        fields.put("doc_id", String.valueOf(docId));
        fields.put("title", title != null ? title : "");
        fields.put("content", content != null ? content : "");
        fields.put("category", category != null ? category : "");
        fields.put("tags", tags != null ? tags : "");
        fields.put("access_roles", accessRoles != null ? accessRoles : "");
        redisTemplate.opsForHash().putAll(key, fields);
        byte[] vecBytes = floatArrayToBytes(embedding);
        redisTemplate.execute((RedisConnection conn) -> {
            conn.hSet(key.getBytes(), "embedding".getBytes(), vecBytes);
            return null;
        });
    }

    @Override
    public void delete(Long docId) {
        redisTemplate.delete(PREFIX + docId);
    }

    @Override
    public List<SourceRefVO> search(float[] queryEmbedding, List<String> userRoles, int topK) {
        List<SourceRefVO> sources = new ArrayList<>();
        try {
            String roleFilter = userRoles.stream()
                    .map(r -> "@access_roles:{" + r + "}")
                    .collect(Collectors.joining("|"));
            byte[] vecBytes = floatArrayToBytes(queryEmbedding);

            try (Jedis jedis = jedisPool.getResource()) {
                // Use sendCommand for maximum compatibility
                List<byte[]> cmdArgs = new ArrayList<>();
                cmdArgs.add(SafeEncoder.encode("idx:knowledge"));
                cmdArgs.add(SafeEncoder.encode("(" + roleFilter + ")=>[KNN " + topK + " @embedding $vec]"));
                cmdArgs.add(SafeEncoder.encode("PARAMS"));
                cmdArgs.add(SafeEncoder.encode("2"));
                cmdArgs.add(SafeEncoder.encode("vec"));
                cmdArgs.add(vecBytes);
                cmdArgs.add(SafeEncoder.encode("LIMIT"));
                cmdArgs.add(SafeEncoder.encode("0"));
                cmdArgs.add(SafeEncoder.encode(String.valueOf(topK)));
                cmdArgs.add(SafeEncoder.encode("RETURN"));
                cmdArgs.add(SafeEncoder.encode("3"));
                cmdArgs.add(SafeEncoder.encode("doc_id"));
                cmdArgs.add(SafeEncoder.encode("title"));
                cmdArgs.add(SafeEncoder.encode("content"));
                cmdArgs.add(SafeEncoder.encode("DIALECT"));
                cmdArgs.add(SafeEncoder.encode("2"));

                Object raw = jedis.sendCommand(SearchCommand.SEARCH, cmdArgs.toArray(new byte[0][]));
                if (!(raw instanceof List<?> rawList) || rawList.size() < 2) return sources;

                // Jedis returns Long for counts, not byte[]
                long total = ((Number) rawList.get(0)).longValue();
                log.info("Jedis KNN results: total={}", total);
                if (total == 0) return sources;

                double score = 1.0;
                for (int i = 2; i < rawList.size(); i += 2) {
                    Object fieldsObj = rawList.get(i);
                    if (!(fieldsObj instanceof List<?> fields)) continue;

                    Map<String, String> map = new HashMap<>();
                    for (int j = 0; j + 1 < fields.size(); j += 2) {
                        String key = fields.get(j) instanceof byte[] ? SafeEncoder.encode((byte[]) fields.get(j)) : String.valueOf(fields.get(j));
                        String value = fields.get(j + 1) instanceof byte[] ? SafeEncoder.encode((byte[]) fields.get(j + 1)) : String.valueOf(fields.get(j + 1));
                        map.put(key, value);
                    }
                    Long docId = Long.valueOf(map.getOrDefault("doc_id", "0"));
                    String title = map.getOrDefault("title", "");
                    String content = map.getOrDefault("content", "");
                    String snippet = content.length() > 200 ? content.substring(0, 200) + "..." : content;
                    sources.add(new SourceRefVO(docId, title, snippet, score));
                    score -= 0.1;
                }
            }
        } catch (Exception e) {
            log.error("Vector search error: {}", e.getMessage());
        }
        return sources;
    }

    @Override
    public List<SourceRefVO> searchByKeyword(String query, List<String> userRoles, int topK) {
        return searchFromMysql(query, userRoles, topK);
    }

    @Override
    public List<SourceRefVO> searchFromMysql(String query, List<String> userRoles, int topK) {
        List<SourceRefVO> sources = new ArrayList<>();
        try {
            String cleaned = query
                    .replaceAll("[?？!！。，,，\\s]", "")
                    .replaceAll("是怎么样|怎么回事|什么样|怎么样|如何|怎么[办样]|是什么|什么是|哪一个|有哪些|能不能|可以吗|请问|告诉我|帮我|的|吗|呢|吧|啊", "")
                    .trim();
            if (cleaned.isEmpty()) cleaned = query.trim();

            java.util.List<String> keywords = new java.util.ArrayList<>();
            for (int i = 0; i + 1 < cleaned.length(); i++) {
                String bigram = cleaned.substring(i, Math.min(i + 2, cleaned.length()));
                if (!keywords.contains(bigram)) keywords.add(bigram);
            }
            if (cleaned.length() >= 2 && !keywords.contains(cleaned)) keywords.add(cleaned);
            if (keywords.size() > 10) keywords = keywords.subList(0, 10);

            for (KnowledgeDoc doc : knowledgeDocMapper.searchByKeywords(keywords, topK * 3)) {
                if (!hasRoleAccess(doc.getAccessRoles(), userRoles)) continue;
                boolean exists = sources.stream().anyMatch(s -> s.getDocId().equals(doc.getId()));
                if (exists) continue;
                String snippet = doc.getContent() != null && doc.getContent().length() > 300
                        ? doc.getContent().substring(0, 300) + "..." : doc.getContent();
                sources.add(new SourceRefVO(doc.getId(), doc.getTitle(), snippet != null ? snippet : "", 0.9 - sources.size() * 0.1));
                if (sources.size() >= topK) break;
            }
        } catch (Exception e) {
            log.error("MySQL search error: {}", e.getMessage());
        }
        return sources;
    }

    private boolean hasRoleAccess(String accessRoles, List<String> userRoles) {
        if (accessRoles == null || userRoles == null) return false;
        for (String role : userRoles) {
            if (accessRoles.contains("\"" + role + "\"")) return true;
        }
        return false;
    }

    private byte[] floatArrayToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }
}
