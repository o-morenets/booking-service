package com.example.booking.cache;

import com.example.booking.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

class CacheWarmUpTest {

    @Test
    void rebuildCache_callsCacheService() {
        UnitRepository unitRepository = Mockito.mock(UnitRepository.class);
        AvailabilityCacheService cacheService = Mockito.mock(AvailabilityCacheService.class);

        Mockito.when(unitRepository.countAvailableToday()).thenReturn(42L);

        CacheWarmUp cacheWarmUp = new CacheWarmUp(cacheService, unitRepository);
        cacheWarmUp.rebuildCache();

        Mockito.verify(cacheService).setAvailableUnits(42L);
    }
}
