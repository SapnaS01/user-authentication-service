package com.self.userauth.util;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;

import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class RedisHelper {
	private final RedisTemplate<String, Object> redisTemplate;
	private final ObjectMapper objectMapper;

	// save a value with TTL (time unit for ttl)
	public <T> void setWithTtl(String key, T value, long ttl, TimeUnit unit) {
		try {
			redisTemplate.opsForValue().set(key, value, ttl, unit);
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
	}

	// get a value and deserialize
	public <T> T get(String key, Class<T> clazz) {
		Object obj = redisTemplate.opsForValue().get(key);
		if (obj == null) return null;

		// if the stored object is already of type T, cast directly
		if (clazz.isInstance(obj)) {
			return clazz.cast(obj);
		}

		// otherwise, convert via objectMapper
		return objectMapper.convertValue(obj, clazz);
	}

	// delete a key
	public void delete(String key) {
		redisTemplate.delete(key);
	}

	// Get a value and delete it (atomic consume)
	public <T> T getAndDelete(String key, Class<T> clazz) {
		T value = get(key, clazz);
		if (value != null) delete(key); // remove immediately after fetching
		return value;
	}
}
