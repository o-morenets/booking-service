package com.example.booking.controller;

import com.example.booking.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
public class PaymentController {

    private final PaymentService paymentService;

    /**
     * Emulate payment
     */
    @PostMapping("/{bookingId}/pay")
    public void pay(@PathVariable UUID bookingId) {
        paymentService.payBooking(bookingId);
    }
}
