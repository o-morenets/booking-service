package com.example.booking.bootstrap;

import com.example.booking.entity.AccommodationType;
import com.example.booking.entity.Unit;
import com.example.booking.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.Random;

@Component
@RequiredArgsConstructor
public class RandomUnitsLoader {

    private static final int UNITS_TO_CREATE = 90;

    private final UnitRepository unitRepository;
    private final Random random = new Random();

    @EventListener(ApplicationReadyEvent.class)
    public void load() {
        long existing = unitRepository.count();

        if (existing >= UNITS_TO_CREATE) {
            return; // already initialized
        }

        for (int i = 0; i < UNITS_TO_CREATE; i++) {
            unitRepository.save(randomUnit());
        }
    }

    private Unit randomUnit() {
        Unit unit = new Unit();

        unit.setRooms(randomInt(1, 5));
        unit.setFloor(randomInt(1, 10));
        unit.setType(randomAccommodationType());
        unit.setBaseCost(randomCost());
        unit.setDescription(randomDescription());
        unit.setActive(true);

        return unit;
    }

    private int randomInt(int min, int max) {
        return random.nextInt(max - min + 1) + min;
    }

    private BigDecimal randomCost() {
        int value = randomInt(50, 300);
        return BigDecimal.valueOf(value);
    }

    private AccommodationType randomAccommodationType() {
        AccommodationType[] values = AccommodationType.values();
        return values[random.nextInt(values.length)];
    }

    private String randomDescription() {
        return "Auto-generated unit";
    }
}
