package com.example.booking.dto;

import java.time.LocalDate;
import java.util.UUID;

public record CreateBookingRequest(
        UUID unitId,
        UUID userId,
        LocalDate startDate,
        LocalDate endDate
) {
}