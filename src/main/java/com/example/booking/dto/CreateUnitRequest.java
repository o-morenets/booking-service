package com.example.booking.dto;

import java.math.BigDecimal;

public record CreateUnitRequest(
        int rooms,
        String type,      // HOME | FLAT | APARTMENTS
        int floor,
        BigDecimal baseCost,
        String description
) {
}
