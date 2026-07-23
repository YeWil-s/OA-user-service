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
    private static final String INDEX = "idx:knowledge";

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
    public void store(KnowledgeDoc doc, String tags, float[] embedding) {
        String key = PREFIX + doc.getId();
        Map<String, String> fields = new HashMap<>();
        fields.put("doc_id", String.valueOf(doc.getId()));
        fields.put("title", safe(doc.getTitle()));
        fields.put("content", safe(doc.getContent()));
        fields.put("category", safeValue(doc.getCategory()));
        fields.put("tags", safe(tags));
        fields.put("access_roles", normalizeTagValues(doc.getAccessRoles()));
        fields.put("access_positions", normalizeTagValues(doc.getAccessPositions()));
        fields.put("access_depts", normalizeTagValues(doc.getAccessDepts()));
        redisTemplate.opsForHash().putAll(key, fields);
        if (doc.getDeptId() != null) {
            redisTemplate.opsForHash().put(key, "dept_id", String.valueOf(doc.getDeptId()));
        } else {
            redisTemplate.opsForHash().delete(key, "dept_id");
        }
        if (doc.getAccessMode() != null) {
            redisTemplate.opsForHash().put(key, "access_mode", String.valueOf(doc.getAccessMode()));
        } else {
            redisTemplate.opsForHash().delete(key, "access_mode");
        }
        if (doc.getVersion() != null) {
            redisTemplate.opsForHash().put(key, "version", String.valueOf(doc.getVersion()));
        } else {
            redisTemplate.opsForHash().delete(key, "version");
        }
        if (doc.getVectorStatus() != null) {
            redisTemplate.opsForHash().put(key, "vector_status", String.valueOf(doc.getVectorStatus()));
        } else {
            redisTemplate.opsForHash().delete(key, "vector_status");
        }

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
    public List<SourceRefVO> search(float[] queryEmbedding, List<String> userRoles, Long deptId, Long positionId, int topK) {
        List<SourceRefVO> sources = new ArrayList<>();
        try {
            String accessFilter = buildAccessFilter(userRoles, deptId, positionId);
            String baseFilter = "@vector_status:[1 1]";
            String query = accessFilter.isBlank() ? baseFilter : "(" + baseFilter + " " + accessFilter + ")";
            byte[] vecBytes = floatArrayToBytes(queryEmbedding);

            try (Jedis jedis = jedisPool.getResource()) {
                List<byte[]> cmdArgs = new ArrayList<>();
                cmdArgs.add(SafeEncoder.encode(INDEX));
                cmdArgs.add(SafeEncoder.encode("(" + query + ")=>[KNN " + topK + " @embedding $vec AS score]"));
                cmdArgs.add(SafeEncoder.encode("PARAMS"));
                cmdArgs.add(SafeEncoder.encode("2"));
                cmdArgs.add(SafeEncoder.encode("vec"));
                cmdArgs.add(vecBytes);
                cmdArgs.add(SafeEncoder.encode("SORTBY"));
                cmdArgs.add(SafeEncoder.encode("score"));
                cmdArgs.add(SafeEncoder.encode("LIMIT"));
                cmdArgs.add(SafeEncoder.encode("0"));
                cmdArgs.add(SafeEncoder.encode(String.valueOf(topK)));
                cmdArgs.add(SafeEncoder.encode("RETURN"));
                cmdArgs.add(SafeEncoder.encode("4"));
                cmdArgs.add(SafeEncoder.encode("doc_id"));
                cmdArgs.add(SafeEncoder.encode("title"));
                cmdArgs.add(SafeEncoder.encode("content"));
                cmdArgs.add(SafeEncoder.encode("score"));
                cmdArgs.add(SafeEncoder.encode("DIALECT"));
                cmdArgs.add(SafeEncoder.encode("2"));

                Object raw = jedis.sendCommand(SearchCommand.SEARCH, cmdArgs.toArray(new byte[0][]));
                if (!(raw instanceof List<?> rawList) || rawList.size() < 2) return sources;

                long total = ((Number) rawList.get(0)).longValue();
                if (total == 0) return sources;

                for (int i = 1; i < rawList.size(); i++) {
                    Object item = rawList.get(i);
                    if (!(item instanceof List<?> fields)) continue;

                    Map<String, String> map = new HashMap<>();
                    for (int j = 0; j + 1 < fields.size(); j += 2) {
                        String keyField = asString(fields.get(j));
                        String valueField = asString(fields.get(j + 1));
                        map.put(keyField, valueField);
                    }

                    Long docId = Long.valueOf(map.getOrDefault("doc_id", "0"));
                    String title = map.getOrDefault("title", "");
                    String content = map.getOrDefault("content", "");
                    double score = parseDouble(map.get("score"), 0.0d);
                    String snippet = content.length() > 200 ? content.substring(0, 200) + "..." : content;
                    sources.add(new SourceRefVO(docId, title, snippet, score));
                }
            }
        } catch (Exception e) {
            log.error("Vector search error: {}", e.getMessage());
        }
        return sources;
    }

    @Override
    public List<SourceRefVO> searchByKeyword(String query, List<String> userRoles, Long deptId, Long positionId, int topK) {
        return searchFromMysql(query, userRoles, deptId, positionId, topK);
    }

    @Override
    public List<SourceRefVO> searchFromMysql(String query, List<String> userRoles, Long deptId, Long positionId, int topK) {
        List<SourceRefVO> sources = new ArrayList<>();
        try {
            String cleaned = query
                    .replaceAll("[?？!！。.,，；;\\s]", "")
                    .replaceAll("是什么样|怎么样|怎么回事|什么样|如何|怎么([办样])|是什么|哪一个|有哪些|能不能|可以吗|请问|告诉我|帮我|的呀|吧|呢|吗", "")
                    .trim();
            if (cleaned.isEmpty()) cleaned = query.trim();

            List<String> keywords = new ArrayList<>();
            for (int i = 0; i + 1 < cleaned.length(); i++) {
                String bigram = cleaned.substring(i, Math.min(i + 2, cleaned.length()));
                if (!keywords.contains(bigram)) keywords.add(bigram);
            }
            if (cleaned.length() >= 2 && !keywords.contains(cleaned)) keywords.add(cleaned);
            if (keywords.size() > 10) keywords = keywords.subList(0, 10);
            if (keywords.isEmpty()) {
                return sources;
            }

            for (var doc : knowledgeDocMapper.searchByKeywords(keywords, topK * 3)) {
                if (!hasAnyAccess(doc, userRoles, deptId, positionId)) continue;
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

    private String buildAccessFilter(List<String> userRoles, Long deptId, Long positionId) {
        List<String> filters = new ArrayList<>();
        if (userRoles != null) {
            userRoles.stream()
                    .filter(r -> r != null && !r.isBlank())
                    .map(r -> "@access_roles:{" + r + "}")
                    .forEach(filters::add);
        }
        if (deptId != null) {
            filters.add("@access_depts:{" + deptId + "}");
        }
        if (positionId != null) {
            filters.add("@access_positions:{" + positionId + "}");
        }
        if (filters.isEmpty()) {
            return "";
        }
        return "(" + String.join("|", filters) + ")";
    }

    private boolean hasAnyAccess(KnowledgeDoc doc, List<String> userRoles, Long deptId, Long positionId) {
        if (doc.getAccessRoles() == null && doc.getAccessDepts() == null && doc.getAccessPositions() == null) {
            return true;
        }
        if (hasRoleAccess(doc.getAccessRoles(), userRoles)) {
            return true;
        }
        if (deptId != null && containsJsonValue(doc.getAccessDepts(), deptId)) {
            return true;
        }
        return positionId != null && containsJsonValue(doc.getAccessPositions(), positionId);
    }

    private boolean hasRoleAccess(String accessRoles, List<String> userRoles) {
        if (accessRoles == null || userRoles == null) return false;
        for (String role : userRoles) {
            if (role != null && accessRoles.contains("\"" + role + "\"")) {
                return true;
            }
        }
        return false;
    }

    private boolean containsJsonValue(String json, Long value) {
        return json != null && value != null && json.contains("\"" + value + "\"");
    }

    private String normalizeTagValues(String value) {
        if (value == null) return "";
        return value.replace("[", "")
                .replace("]", "")
                .replace("\"", "")
                .replace(" ", "");
    }

    private String normalizeTagValues(List<Long> values) {
        if (values == null || values.isEmpty()) return "";
        return values.stream().map(String::valueOf).collect(Collectors.joining(","));
    }

    private String safe(String value) {
        return value == null ? "" : value;
    }

    private String safeValue(Number value) {
        return value == null ? "" : String.valueOf(value);
    }

    private String asString(Object value) {
        if (value == null) return "";
        return value instanceof byte[] bytes ? SafeEncoder.encode(bytes) : String.valueOf(value);
    }

    private double parseDouble(String value, double fallback) {
        try {
            return value == null ? fallback : Double.parseDouble(value);
        } catch (Exception e) {
            return fallback;
        }
    }

    private byte[] floatArrayToBytes(float[] floats) {
        ByteBuffer buffer = ByteBuffer.allocate(floats.length * 4);
        buffer.order(ByteOrder.LITTLE_ENDIAN);
        buffer.asFloatBuffer().put(floats);
        return buffer.array();
    }
}
