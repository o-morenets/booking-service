package com.example.booking.service;

import com.example.booking.dto.CreateUnitRequest;
import com.example.booking.dto.UnitResponse;
import com.example.booking.entity.AccommodationType;
import com.example.booking.entity.Booking;
import com.example.booking.entity.EventType;
import com.example.booking.entity.Unit;
import com.example.booking.repository.UnitRepository;
import jakarta.persistence.criteria.Join;
import jakarta.persistence.criteria.JoinType;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private static final BigDecimal MARKUP = BigDecimal.valueOf(1.15);

    private final UnitRepository unitRepository;
    private final EventService eventService;

    @Override
    public UnitResponse createUnit(CreateUnitRequest request) {
        Unit unit = new Unit();
        unit.setRooms(request.rooms());
        unit.setFloor(request.floor());
        unit.setType(AccommodationType.valueOf(request.type()));
        unit.setBaseCost(request.baseCost());
        unit.setDescription(request.description());
        unit.setActive(true);

        Unit saved = unitRepository.save(unit);

        eventService.log(
                EventType.UNIT_CREATED,
                "Unit created with id=" + unit.getId()
        );

        return toResponse(saved);
    }

    @Override
    public Page<UnitResponse> searchAvailable(
            Integer rooms,
            String type,
            Integer floor,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal maxCost,
            Pageable pageable
    ) {
        Specification<Unit> spec = (root, query, cb) -> {

            Predicate predicate = cb.conjunction();

            if (rooms != null) {
                predicate = cb.and(predicate, cb.equal(root.get("rooms"), rooms));
            }

            if (type != null) {
                predicate = cb.and(predicate, cb.equal(root.get("type"), AccommodationType.valueOf(type)));
            }

            if (floor != null) {
                predicate = cb.and(predicate, cb.equal(root.get("floor"), floor));
            }

            if (maxCost != null) {
                predicate = cb.and(
                        predicate,
                        cb.lessThanOrEqualTo(
                                root.get("baseCost"),
                                maxCost.divide(MARKUP, 2, RoundingMode.HALF_UP)
                        )
                );
            }


            Join<Unit, Booking> bookingJoin =
                    root.join("bookings", JoinType.LEFT);

            Predicate noOverlap = cb.or(
                    cb.isNull(bookingJoin.get("id")),
                    cb.or(
                            cb.lessThan(bookingJoin.get("endDate"), startDate),
                            cb.greaterThan(bookingJoin.get("startDate"), endDate)
                    )
            );

            predicate = cb.and(predicate, noOverlap);

            return predicate;
        };

        return unitRepository
                .findAll(spec, pageable)
                .map(this::toResponse);
    }

    @Override
    public long getAvailableUnitsCount() {
        return unitRepository.countCurrentlyAvailable();
    }

    private UnitResponse toResponse(Unit unit) {
        return new UnitResponse(
                unit.getId(),
                unit.getRooms(),
                unit.getType().name(),
                unit.getFloor(),
                unit.getBaseCost().multiply(MARKUP),
                unit.getDescription()
        );
    }
}
