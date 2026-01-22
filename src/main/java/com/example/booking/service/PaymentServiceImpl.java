package com.example.booking.service;

import com.example.booking.config.PaymentProperties;
import com.example.booking.entity.Booking;
import com.example.booking.entity.Payment;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.PaymentRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

import static com.example.booking.entity.BookingStatus.PAID;
import static com.example.booking.entity.BookingStatus.PENDING_PAYMENT;
import static com.example.booking.entity.EventType.PAYMENT_SUCCESS;

@Service
@RequiredArgsConstructor
public class PaymentServiceImpl implements PaymentService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final EventService eventService;
    private final PaymentProperties paymentProperties;

    @Transactional
    @Override
    public void payBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != PENDING_PAYMENT) {
            throw new IllegalStateException("Booking cannot be paid");
        }

        BigDecimal baseCost = booking.getUnit().getBaseCost();
        BigDecimal totalAmount = baseCost.add(baseCost.multiply(paymentProperties.getMarkup()));

        Payment payment = new Payment(
                UUID.randomUUID(),
                booking,
                totalAmount,
                true,
                Instant.now(),
                Instant.now()
        );

        paymentRepository.save(payment);

        booking.setStatus(PAID);

        eventService.log(
                PAYMENT_SUCCESS,
                "Payment successful, paymentId=" + payment.getId()
        );
    }
}
