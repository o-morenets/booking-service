package com.example.booking.service;

import com.example.booking.dto.CreateUnitRequest;
import com.example.booking.dto.UnitResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface UnitService {

    UnitResponse createUnit(CreateUnitRequest request);

    Page<UnitResponse> searchAvailable(
            Integer rooms,
            String type,
            Integer floor,
            LocalDate startDate,
            LocalDate endDate,
            BigDecimal maxCost,
            Pageable pageable
    );

    long getAvailableUnitsCount();
}
