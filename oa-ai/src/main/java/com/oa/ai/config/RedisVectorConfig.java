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

import java.util.ArrayList;
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
        poolConfig.setJmxEnabled(false);
        jedisPool = new JedisPool(poolConfig, redisHost, redisPort, 2000, null, redisDatabase);

        try (Jedis jedis = jedisPool.getResource()) {
            boolean indexExisted;
            try {
                jedis.sendCommand(SearchCommand.INFO, INDEX);
                indexExisted = true;
                log.info("Vector index '{}' already exists", INDEX);
            } catch (Exception e) {
                indexExisted = false;
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
                        SafeEncoder.encode("access_depts"), SafeEncoder.encode("TAG"), SafeEncoder.encode("SEPARATOR"), SafeEncoder.encode(","),
                        SafeEncoder.encode("access_positions"), SafeEncoder.encode("TAG"), SafeEncoder.encode("SEPARATOR"), SafeEncoder.encode(","),
                        SafeEncoder.encode("vector_status"), SafeEncoder.encode("NUMERIC"),
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

            if (indexExisted) {
                String[][] missingFields = {
                    {"access_depts", "TAG", "SEPARATOR", ","},
                    {"access_positions", "TAG", "SEPARATOR", ","},
                    {"vector_status", "NUMERIC"},
                };
                for (String[] field : missingFields) {
                    try {
                        jedis.sendCommand(SearchCommand.ALTER, buildAlterArgs(INDEX, field));
                        log.info("Added missing field '{}' to index '{}'", field[0], INDEX);
                    } catch (Exception ignored) {
                        // field already exists — ignore
                    }
                }
            }
        } catch (Exception e) {
            log.warn("Redis Stack not available — vector search disabled, falling back to MySQL keyword search: {}", e.getMessage());
        }
    }

    private byte[][] buildAlterArgs(String index, String[] fieldDef) {
        List<byte[]> args = new ArrayList<>();
        args.add(SafeEncoder.encode(index));
        args.add(SafeEncoder.encode("SCHEMA"));
        args.add(SafeEncoder.encode("ADD"));
        for (String def : fieldDef) {
            args.add(SafeEncoder.encode(def));
        }
        return args.toArray(new byte[0][]);
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

