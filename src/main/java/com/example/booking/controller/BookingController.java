package com.example.booking.controller;

import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.service.BookingService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/bookings")
@RequiredArgsConstructor
public class BookingController {

    private final BookingService bookingService;

    /**
     * Create booking (PENDING_PAYMENT)
     */
    @PostMapping
    public BookingResponse book(@RequestBody CreateBookingRequest request) {
        return bookingService.createBooking(request);
    }

    /**
     * Cancel booking manually
     */
    @PostMapping("/{bookingId}/cancel")
    public void cancel(@PathVariable UUID bookingId) {
        bookingService.cancelBooking(bookingId);
    }

    /**
     * Emulate payment
     */
    @PostMapping("/{bookingId}/pay")
    public void pay(@PathVariable UUID bookingId) {
        bookingService.pay(bookingId);
    }
}
