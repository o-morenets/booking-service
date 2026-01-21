package com.example.booking.dto;

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
}