package com.oa.ai.config;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;
import redis.clients.jedis.search.SearchProtocol.SearchCommand;
import redis.clients.jedis.util.SafeEncoder;

import java.util.List;

@Configuration
public class RedisVectorConfig {

    private static final Logger log = LoggerFactory.getLogger(RedisVectorConfig.class);
    private static final String INDEX = "idx:knowledge";

    @Value("${spring.data.redis.host:localhost}")
    private String redisHost;
    @Value("${spring.data.redis.port:6379}")
    private int redisPort;
    @Value("${spring.data.redis.database:0}")
    private int redisDatabase;

    private JedisPool jedisPool;

    @PostConstruct
    public void init() {
        JedisPoolConfig poolConfig = new JedisPoolConfig();
        jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 2000, null, redisDatabase);

        try (Jedis jedis = jedisPool.getResource()) {
            try {
                jedis.sendCommand(SearchCommand.INFO, INDEX);
                log.info("Vector index '{}' already exists, skipping creation", INDEX);
            } catch (Exception e) {
                log.info("Creating vector index '{}'...", INDEX);
                String result = SafeEncoder.encode((byte[]) jedis.sendCommand(
                        SearchCommand.CREATE,
                        SafeEncoder.encode(INDEX),
                        SafeEncoder.encode("ON"),
                        SafeEncoder.encode("HASH"),
                        SafeEncoder.encode("PREFIX"),
                        SafeEncoder.encode("1"),
                        SafeEncoder.encode("doc:knowledge:"),
                        SafeEncoder.encode("SCHEMA"),
                        SafeEncoder.encode("doc_id"), SafeEncoder.encode("NUMERIC"), SafeEncoder.encode("SORTABLE"),
                        SafeEncoder.encode("title"), SafeEncoder.encode("TEXT"), SafeEncoder.encode("WEIGHT"), SafeEncoder.encode("2.0"),
                        SafeEncoder.encode("content"), SafeEncoder.encode("TEXT"), SafeEncoder.encode("WEIGHT"), SafeEncoder.encode("1.0"),
                        SafeEncoder.encode("category"), SafeEncoder.encode("TAG"), SafeEncoder.encode("SEPARATOR"), SafeEncoder.encode(","),
                        SafeEncoder.encode("tags"), SafeEncoder.encode("TAG"), SafeEncoder.encode("SEPARATOR"), SafeEncoder.encode(","),
                        SafeEncoder.encode("access_roles"), SafeEncoder.encode("TAG"), SafeEncoder.encode("SEPARATOR"), SafeEncoder.encode(","),
                        SafeEncoder.encode("embedding"), SafeEncoder.encode("VECTOR"), SafeEncoder.encode("HNSW"),
                        SafeEncoder.encode("6"), SafeEncoder.encode("TYPE"), SafeEncoder.encode("FLOAT32"),
                        SafeEncoder.encode("DIM"), SafeEncoder.encode("1024"),
                        SafeEncoder.encode("DISTANCE_METRIC"), SafeEncoder.encode("COSINE")));
                if ("OK".equals(result)) {
                    log.info("Vector index '{}' created successfully", INDEX);
                } else {
                    log.error("Failed to create vector index '{}': {}", INDEX, result);
                }
            }
        } catch (Exception e) {
            log.error("Failed to initialize Redis vector index '{}': {}", INDEX, e.getMessage());
        }
    }

    @Bean
    public JedisPool jedisPool() {
        return jedisPool;
    }

    @PreDestroy
    public void cleanup() {
        if (jedisPool != null) {
            jedisPool.close();
        }
    }
}
