package com.example.booking.controller;

import com.example.booking.dto.CreateUnitRequest;
import com.example.booking.dto.UnitResponse;
import com.example.booking.service.UnitService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.time.LocalDate;

@RestController
@RequestMapping("/api/units")
@RequiredArgsConstructor
public class UnitController {

    private final UnitService unitService;

    /**
     * Create new Unit
     */
    @PostMapping
    public UnitResponse create(@RequestBody CreateUnitRequest request) {
        return unitService.createUnit(request);
    }

    /**
     * Search available Units by criteria
     */
    @GetMapping("/search")
    public Page<UnitResponse> search(
            @RequestParam(required = false) Integer rooms,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) Integer floor,
            @RequestParam LocalDate startDate,
            @RequestParam LocalDate endDate,
            @RequestParam(required = false) BigDecimal maxCost,
            @PageableDefault() Pageable pageable
    ) {
        return unitService.searchAvailable(
                rooms,
                type,
                floor,
                startDate,
                endDate,
                maxCost,
                pageable
        );
    }

    /**
     * Cached number of available Units
     */
    @GetMapping("/available/count")
    public long availableCount() {
        return unitService.getAvailableUnitsCount();
    }
}