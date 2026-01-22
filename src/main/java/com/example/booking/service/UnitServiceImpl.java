package com.example.booking.service;

import com.example.booking.cache.AvailabilityCacheService;
import com.example.booking.config.PaymentProperties;
import com.example.booking.dto.CreateUnitRequest;
import com.example.booking.dto.UnitResponse;
import com.example.booking.entity.AccommodationType;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.Unit;
import com.example.booking.repository.UnitRepository;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import jakarta.persistence.criteria.Subquery;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;

import static com.example.booking.entity.EventType.UNIT_CREATED;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;
    private final EventService eventService;
    private final AvailabilityCacheService cacheService;
    private final PaymentProperties paymentProperties;

    @Override
    @Transactional
    public UnitResponse createUnit(CreateUnitRequest request) {
        Unit unit = new Unit();
        unit.setRooms(request.rooms());
        unit.setType(AccommodationType.valueOf(request.type()));
        unit.setFloor(request.floor());
        unit.setBaseCost(request.baseCost());
        unit.setDescription(request.description());

        Unit saved = unitRepository.save(unit);

        eventService.log(
                UNIT_CREATED,
                "Unit created, unitId=" + unit.getId()
        );

        cacheService.incrementAvailableUnits();

        return toResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
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
                                maxCost.subtract(
                                        maxCost.multiply(paymentProperties.getMarkup())
                                )
                        )
                );
            }

            if (startDate != null && endDate != null) {
                Subquery<Long> subquery = query.subquery(Long.class);
                Root<Booking> booking = subquery.from(Booking.class);

                subquery.select(cb.literal(1L))
                        .where(
                                cb.equal(booking.get("unit"), root),
                                booking.get("status").in(
                                        BookingStatus.PENDING_PAYMENT,
                                        BookingStatus.PAID
                                ),
                                cb.lessThan(booking.get("startDate"), endDate),
                                cb.greaterThan(booking.get("endDate"), startDate)
                        );

                predicate = cb.and(predicate, cb.not(cb.exists(subquery)));
            }

            return predicate;
        };

        return unitRepository
                .findAll(spec, pageable)
                .map(this::toResponse);
    }

    @Override
    public long getAvailableUnitsCount() {
        return cacheService.getAvailableUnitsCount();
    }

    private UnitResponse toResponse(Unit unit) {
        return new UnitResponse(
                unit.getId(),
                unit.getRooms(),
                unit.getType().name(),
                unit.getFloor(),
                unit.getBaseCost().add(unit.getBaseCost().multiply(paymentProperties.getMarkup())),
                unit.getDescription()
        );
    }
}
