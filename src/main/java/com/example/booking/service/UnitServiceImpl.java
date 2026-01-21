package com.example.booking.service;

import com.example.booking.dto.CreateUnitRequest;
import com.example.booking.dto.UnitResponse;
import com.example.booking.repository.UnitRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;

@Service
@RequiredArgsConstructor
public class UnitServiceImpl implements UnitService {

    private final UnitRepository unitRepository;

    @Override
    public UnitResponse createUnit(CreateUnitRequest request) {
        return null;
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
        return null;
    }

    @Override
    public long getAvailableUnitsCount() {
        return unitRepository.countCurrentlyAvailable();
    }
}
