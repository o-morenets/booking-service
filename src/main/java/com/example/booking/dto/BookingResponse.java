package com.example.booking.dto;

import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        UUID unitId,
        UUID userId,
        LocalDate startDate,
        LocalDate endDate,
        String status
) {
}