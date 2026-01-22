package com.example.booking.dto;

import com.example.booking.entity.Unit;

import java.math.BigDecimal;
import java.util.UUID;

public record UnitResponse(
        UUID id,
        int rooms,
        String type,
        int floor,
        BigDecimal totalCost,
        String description
) {
    public static UnitResponse from(Unit unit, BigDecimal markup) {
        return new UnitResponse(
                unit.getId(),
                unit.getRooms(),
                unit.getType().name(),
                unit.getFloor(),
                unit.getBaseCost().multiply(BigDecimal.ONE.add(markup)), // baseCost + baseCost * markup = baseCost * (1 + markup)
                unit.getDescription()
        );
    }
}
