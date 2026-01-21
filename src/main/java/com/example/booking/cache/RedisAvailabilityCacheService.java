package com.example.booking.cache;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class RedisAvailabilityCacheService implements AvailabilityCacheService {

    private final RedisTemplate<String, Object> redisTemplate;

    @Override
    public long getAvailableUnitsCount() {
        return Optional.ofNullable(redisTemplate.opsForValue().get(CacheKeys.AVAILABLE_UNITS_COUNT))
                .map(v -> (Long) v)
                .orElse(0L);
    }

    @Override
    public void incrementAvailableUnits() {
        redisTemplate.opsForValue().increment(CacheKeys.AVAILABLE_UNITS_COUNT, 1);
    }

    @Override
    public void decrementAvailableUnits() {
        redisTemplate.opsForValue().decrement(CacheKeys.AVAILABLE_UNITS_COUNT, 1);
    }

    @Override
    public void rebuildAvailableUnits(long actualValue) {
        redisTemplate.opsForValue().set(CacheKeys.AVAILABLE_UNITS_COUNT, actualValue);
    }
}
