package com.oa.datavisual.service.impl;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.oa.common.utils.RedisUtils;
import com.oa.datavisual.service.IVisualCacheService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.time.Duration;
import java.util.function.Supplier;

@Service
public class VisualCacheServiceImpl implements IVisualCacheService {

    private static final Logger log = LoggerFactory.getLogger(VisualCacheServiceImpl.class);
    private static final Duration CACHE_TTL = Duration.ofSeconds(60);

    private final ObjectProvider<RedisUtils> redisUtilsProvider;
    private final ObjectMapper objectMapper;

    public VisualCacheServiceImpl(ObjectProvider<RedisUtils> redisUtilsProvider, ObjectMapper objectMapper) {
        this.redisUtilsProvider = redisUtilsProvider;
        this.objectMapper = objectMapper;
    }

    @Override
    public <T> T get(String key, TypeReference<T> typeReference, Supplier<T> loader) {
        RedisUtils redisUtils = redisUtilsProvider.getIfAvailable();
        if (redisUtils != null) {
            try {
                String cached = redisUtils.get(key);
                if (StringUtils.hasText(cached)) {
                    return objectMapper.readValue(cached, typeReference);
                }
            } catch (Exception ex) {
                log.debug("Read visual cache failed, key={}", key, ex);
            }
        }

        T data = loader.get();
        if (redisUtils != null) {
            try {
                redisUtils.set(key, objectMapper.writeValueAsString(data), CACHE_TTL);
            } catch (Exception ex) {
                log.debug("Write visual cache failed, key={}", key, ex);
            }
        }
        return data;
    }

    @Override
    public void evictMonth(String statMonth) {
        RedisUtils redisUtils = redisUtilsProvider.getIfAvailable();
        if (redisUtils == null || !StringUtils.hasText(statMonth)) {
            return;
        }

        String[] keys = {
                "visual:overview:" + statMonth,
                "visual:dept-distribution:" + statMonth,
                "visual:dept-overtime:" + statMonth,
                "visual:approval-stats:" + statMonth,
                "visual:approval-speed:" + statMonth,
                "visual:screen:attendance:" + statMonth,
                "visual:screen:hr:" + statMonth,
                "visual:screen:approval:" + statMonth
        };
        for (String key : keys) {
            deleteQuietly(redisUtils, key);
        }
        for (int months = 1; months <= 12; months++) {
            deleteQuietly(redisUtils, "visual:attendance-trend:" + statMonth + ":" + months);
        }
    }

    private void deleteQuietly(RedisUtils redisUtils, String key) {
        try {
            redisUtils.delete(key);
        } catch (Exception ex) {
            log.debug("Delete visual cache failed, key={}", key, ex);
        }
    }
}
