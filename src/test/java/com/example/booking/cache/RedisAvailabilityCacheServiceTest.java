package com.example.booking.cache;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RedisAvailabilityCacheServiceTest {

    @Mock
    private RedisTemplate<String, Object> redisTemplate;

    @Mock
    private ValueOperations<String, Object> valueOperations;

    private RedisAvailabilityCacheService cacheService;

    @BeforeEach
    void setUp() {
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);
        cacheService = new RedisAvailabilityCacheService(redisTemplate);
    }

    @Test
    void getAvailableUnitsCount_whenValueExists_returnsValue() {
        when(valueOperations.get(CacheKeys.UNITS_AVAILABLE_TODAY))
                .thenReturn(5L);

        long result = cacheService.getAvailableUnitsCount();

        assertEquals(5L, result);
    }

    @Test
    void getAvailableUnitsCount_whenValueIsNull_returnsZero() {
        when(valueOperations.get(CacheKeys.UNITS_AVAILABLE_TODAY))
                .thenReturn(null);

        long result = cacheService.getAvailableUnitsCount();

        assertEquals(0L, result);
    }

    @Test
    void getAvailableUnitsCount_whenValueIsInteger_returnsConvertedLong() {
        when(valueOperations.get(CacheKeys.UNITS_AVAILABLE_TODAY))
                .thenReturn(3);

        long result = cacheService.getAvailableUnitsCount();

        assertEquals(3L, result);
    }

    @Test
    void setAvailableUnits_setsValueInRedis() {
        cacheService.setAvailableUnits(10L);

        verify(valueOperations).set(CacheKeys.UNITS_AVAILABLE_TODAY, 10L);
    }

    @Test
    void incrementAvailableUnits_incrementsValueInRedis() {
        cacheService.incrementAvailableUnits();

        verify(valueOperations).increment(CacheKeys.UNITS_AVAILABLE_TODAY);
    }

    @Test
    void decrementAvailableUnits_decrementsValueInRedis() {
        cacheService.decrementAvailableUnits();

        verify(valueOperations).decrement(CacheKeys.UNITS_AVAILABLE_TODAY);
    }
}
