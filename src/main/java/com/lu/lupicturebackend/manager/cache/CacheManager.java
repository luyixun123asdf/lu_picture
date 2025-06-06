package com.lu.lupicturebackend.manager.cache;

import cn.hutool.core.util.RandomUtil;
import cn.hutool.json.JSONUtil;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.github.benmanes.caffeine.cache.Cache;
import com.lu.lupicturebackend.common.BaseResponse;
import com.lu.lupicturebackend.common.ResultUtils;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class CacheManager {

    @FunctionalInterface
    public interface DatabaseOperation<T> {
        T execute();
    }

    public  <T> BaseResponse<T> queryWithCache(Cache<String, String> LOCAL_CACHE, String cacheKey,
                                               StringRedisTemplate stringRedisTemplate, DatabaseOperation<T> databaseOperation) {
        // 先从本地缓存查询
        String cacheValue = LOCAL_CACHE.getIfPresent(cacheKey);
        if (cacheValue != null) {
            T result = JSONUtil.toBean(cacheValue, (Class<T>) Page.class); // 注意：这里假设返回的是Page.class类型
            return ResultUtils.success(result);
        }

        // 本地缓存未命中，从Redis中获取分布式缓存
        ValueOperations<String, String> stringStringValueOperations = stringRedisTemplate.opsForValue();
        String s = stringStringValueOperations.get(cacheKey);
        if (s != null) {
            // 命中直接返回结果，更新本地缓存
            LOCAL_CACHE.put(cacheKey, s);
            T result = JSONUtil.toBean(s, (Class<T>) Page.class); // 注意：这里假设返回的是Page.class类型
            log.info("命中缓存 ：",result.toString());
            return ResultUtils.success(result);
        }

        // 执行数据库操作
        T result = databaseOperation.execute();
        log.info("未命中缓存 ：",result.toString());
        // 存入Redis
        int expireTime = 300 + RandomUtil.randomInt(0, 300);
        stringStringValueOperations.set(cacheKey, JSONUtil.toJsonStr(result), expireTime, TimeUnit.SECONDS);

        // 写入本地缓存
        LOCAL_CACHE.put(cacheKey, JSONUtil.toJsonStr(result));

        return ResultUtils.success(result);
    }


}
