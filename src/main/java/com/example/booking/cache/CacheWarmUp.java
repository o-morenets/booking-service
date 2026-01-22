package com.example.booking.cache;

import com.example.booking.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CacheWarmUp {

    private final AvailabilityCacheService cacheService;
    private final UnitRepository unitRepository;

    @EventListener(ApplicationReadyEvent.class)
    public void rebuildCache() {
        long countAvailableToday = unitRepository.countAvailableToday();
        cacheService.setAvailableUnits(countAvailableToday);
    }
}
