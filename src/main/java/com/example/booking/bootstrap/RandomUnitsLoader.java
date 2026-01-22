package com.example.booking.bootstrap;

import com.example.booking.entity.AccommodationType;
import com.example.booking.entity.Unit;
import com.example.booking.repository.UnitRepository;
import com.example.booking.service.EventService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.event.ApplicationReadyEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.concurrent.ThreadLocalRandom;

import static com.example.booking.entity.EventType.UNIT_CREATED;

@Component
@RequiredArgsConstructor
public class RandomUnitsLoader {

    private static final int UNITS_TO_CREATE = 90;

    private final UnitRepository unitRepository;
    private final EventService eventService;

    @EventListener(ApplicationReadyEvent.class)
    @Transactional
    public void load() {
        if (unitRepository.count() >= UNITS_TO_CREATE) {
            return;
        }

        for (int i = 0; i < UNITS_TO_CREATE; i++) {
            var unit = randomUnit();
            unitRepository.save(unit);

            eventService.log(
                    UNIT_CREATED,
                    "Random unit created, unitId=" + unit.getId()
            );
        }
    }

    private Unit randomUnit() {
        Unit unit = new Unit();

        unit.setRooms(randomInt(1, 5));
        unit.setFloor(randomInt(1, 10));
        unit.setType(randomAccommodationType());
        unit.setBaseCost(randomCost());
        unit.setDescription(randomDescription());

        return unit;
    }

    private int randomInt(int min, int max) {
        return ThreadLocalRandom.current().nextInt(max - min + 1) + min;
    }

    private BigDecimal randomCost() {
        int value = randomInt(50, 300);
        return BigDecimal.valueOf(value);
    }

    private AccommodationType randomAccommodationType() {
        AccommodationType[] values = AccommodationType.values();
        return values[ThreadLocalRandom.current().nextInt(values.length)];
    }

    private String randomDescription() {
        return "Auto-generated unit";
    }
}
