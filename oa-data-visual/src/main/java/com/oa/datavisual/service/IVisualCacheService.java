package com.oa.datavisual.service;

import com.fasterxml.jackson.core.type.TypeReference;

import java.util.function.Supplier;

public interface IVisualCacheService {

    <T> T get(String key, TypeReference<T> typeReference, Supplier<T> loader);

    void evictMonth(String statMonth);
}
