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
        return Optional.ofNullable(redisTemplate.opsForValue().get(CacheKeys.UNITS_AVAILABLE_TODAY))
                .map(v -> ((Number) v).longValue())
                .orElse(0L);
    }

    @Override
    public void setAvailableUnits(long actualValue) {
        redisTemplate.opsForValue().set(CacheKeys.UNITS_AVAILABLE_TODAY, actualValue);
    }

    @Override
    public void incrementAvailableUnits() {
        redisTemplate.opsForValue().increment(CacheKeys.UNITS_AVAILABLE_TODAY);
    }

    @Override
    public void decrementAvailableUnits() {
        redisTemplate.opsForValue().decrement(CacheKeys.UNITS_AVAILABLE_TODAY);
    }
}
