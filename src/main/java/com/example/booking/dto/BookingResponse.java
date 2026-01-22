package com.example.booking.dto;

import com.example.booking.entity.Booking;

import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public record BookingResponse(
        UUID bookingId,
        UUID unitId,
        UUID userId,
        LocalDate startDate,
        LocalDate endDate,
        String status,
        Instant createdAt
) {
    public static BookingResponse from(Booking booking) {
        return new BookingResponse(
                booking.getId(),
                booking.getUnit().getId(),
                booking.getUser().getId(),
                booking.getStartDate(),
                booking.getEndDate(),
                booking.getStatus().name(),
                booking.getCreatedAt()
        );
    }
}
