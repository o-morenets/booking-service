package com.example.booking.service;

import com.example.booking.cache.AvailabilityCacheService;
import com.example.booking.config.PaymentProperties;
import com.example.booking.dto.CreateUnitRequest;
import com.example.booking.dto.UnitResponse;
import com.example.booking.entity.AccommodationType;
import com.example.booking.entity.Unit;
import com.example.booking.repository.UnitRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.example.booking.entity.EventType.UNIT_CREATED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UnitServiceImplTest {

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private EventService eventService;

    @Mock
    private AvailabilityCacheService cacheService;

    @Mock
    private PaymentProperties paymentProperties;

    @InjectMocks
    private UnitServiceImpl unitService;

    @Captor
    private ArgumentCaptor<Unit> unitCaptor;

    @Test
    void testCreateUnit() {

        CreateUnitRequest request = new CreateUnitRequest(
                2,
                "APARTMENTS",
                3,
                BigDecimal.valueOf(100),
                "Nice unit"
        );

        Unit savedUnit = new Unit();
        savedUnit.setId(UUID.randomUUID());
        savedUnit.setRooms(request.rooms());
        savedUnit.setType(AccommodationType.valueOf(request.type()));
        savedUnit.setFloor(request.floor());
        savedUnit.setBaseCost(request.baseCost());
        savedUnit.setDescription(request.description());

        when(unitRepository.save(any(Unit.class))).thenReturn(savedUnit);
        when(paymentProperties.getMarkup()).thenReturn(BigDecimal.valueOf(0.2));

        UnitResponse response = unitService.createUnit(request);

        assertNotNull(response);
        assertEquals(savedUnit.getId(), response.id());
        assertEquals(savedUnit.getRooms(), response.rooms());
        assertEquals(savedUnit.getType().name(), response.type());

        verify(unitRepository).save(unitCaptor.capture());
        verify(eventService).log(eq(UNIT_CREATED), contains("unitId=" + savedUnit.getId()));
        verify(cacheService).incrementAvailableUnits();

        Unit captured = unitCaptor.getValue();
        assertEquals(request.rooms(), captured.getRooms());
        assertEquals(AccommodationType.valueOf(request.type()), captured.getType());
        assertEquals(request.floor(), captured.getFloor());
        assertEquals(request.baseCost(), captured.getBaseCost());
        assertEquals(request.description(), captured.getDescription());
    }

    @Test
    void testGetAvailableUnitsCount() {
        when(cacheService.getAvailableUnitsCount()).thenReturn(42L);

        long count = unitService.getAvailableUnitsCount();

        assertEquals(42L, count);
        verify(cacheService).getAvailableUnitsCount();
    }

    @Test
    void testSearchAvailable() {
        Unit unit1 = new Unit();
        UUID unitId = UUID.randomUUID();
        unit1.setId(unitId);
        unit1.setRooms(2);
        unit1.setType(AccommodationType.APARTMENTS);
        unit1.setFloor(3);
        unit1.setBaseCost(BigDecimal.valueOf(100));

        Page<Unit> page = new PageImpl<>(List.of(unit1));

        when(unitRepository.findAll(
                ArgumentMatchers.<Specification<Unit>>any(),
                ArgumentMatchers.any(Pageable.class)
        )).thenReturn(page);

        when(paymentProperties.getMarkup()).thenReturn(BigDecimal.valueOf(0.2));

        Page<UnitResponse> result = unitService.searchAvailable(
                2,
                "APARTMENT",
                3,
                LocalDate.now(),
                LocalDate.now().plusDays(1),
                BigDecimal.valueOf(150),
                PageRequest.of(0, 10)
        );

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals(unitId, result.getContent().getFirst().id());
    }
}
