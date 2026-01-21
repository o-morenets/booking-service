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

    public static UnitResponse from(Unit unit) {
        return new UnitResponse(
                unit.getId(),
                unit.getRooms(),
                unit.getType().name(),
                unit.getFloor(),
                unit.getBaseCost(),
                unit.getDescription()
        );
    }
}
