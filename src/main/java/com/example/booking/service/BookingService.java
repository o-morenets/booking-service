package com.example.booking.service;

import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.CreateBookingRequest;

import java.util.UUID;

public interface BookingService {

    BookingResponse createBooking(CreateBookingRequest request);

    void cancelBooking(UUID bookingId);
}
