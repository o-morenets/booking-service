package com.example.booking.cache;

import com.example.booking.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;

@SpringBootTest
class CacheWarmUpIT {

    @Autowired
    CacheWarmUp cacheWarmUp;

    @MockBean
    UnitRepository unitRepository;

    @MockBean
    AvailabilityCacheService cacheService;

    @Test
    void rebuildCache_onStartup() {
        Mockito.when(unitRepository.countAvailableToday()).thenReturn(10L);
        cacheWarmUp.rebuildCache();
        Mockito.verify(cacheService).setAvailableUnits(10L);
    }
}
