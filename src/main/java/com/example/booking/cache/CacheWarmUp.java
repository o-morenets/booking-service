package com.example.booking.cache;

import com.example.booking.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.boot.context.event.ApplicationReadyEvent;

@Component
@RequiredArgsConstructor
public class CacheWarmUp {

    private final AvailabilityCacheService cacheService;
    private final UnitRepository unitRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildCache() {
        long actualAvailableUnits = unitRepository.countCurrentlyAvailable();
        cacheService.rebuildAvailableUnits(actualAvailableUnits);
    }
}
