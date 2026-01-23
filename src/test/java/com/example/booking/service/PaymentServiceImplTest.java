package com.example.booking.service;

import com.example.booking.config.PaymentProperties;
import com.example.booking.entity.Booking;
import com.example.booking.entity.Payment;
import com.example.booking.entity.Unit;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.PaymentRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static com.example.booking.entity.BookingStatus.PAID;
import static com.example.booking.entity.BookingStatus.PENDING_PAYMENT;
import static com.example.booking.entity.EventType.PAYMENT_SUCCESS;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PaymentServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private EventService eventService;

    @Mock
    private PaymentProperties paymentProperties;

    @InjectMocks
    private PaymentServiceImpl paymentService;

    @Test
    void payBooking_success() {
        UUID bookingId = UUID.randomUUID();

        Unit unit = new Unit();
        unit.setBaseCost(new BigDecimal("100.00"));

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(PENDING_PAYMENT);
        booking.setUnit(unit);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        when(paymentProperties.getMarkup())
                .thenReturn(new BigDecimal("0.10"));

        when(paymentRepository.save(any(Payment.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        paymentService.payBooking(bookingId);

        assertEquals(PAID, booking.getStatus());

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());

        Payment savedPayment = paymentCaptor.getValue();
        assertEquals(
                0,
                savedPayment.getAmount().compareTo(new BigDecimal("110.00"))
        );
        assertTrue(savedPayment.isSuccessful());
        assertEquals(booking, savedPayment.getBooking());

        verify(eventService).log(
                eq(PAYMENT_SUCCESS),
                contains("paymentId=")
        );
    }

    @Test
    void payBooking_bookingNotFound() {
        UUID bookingId = UUID.randomUUID();

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> paymentService.payBooking(bookingId)
        );

        assertEquals("Booking not found", ex.getMessage());
        verifyNoInteractions(paymentRepository, eventService);
    }

    @Test
    void payBooking_bookingNotPendingPayment() {
        UUID bookingId = UUID.randomUUID();

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(PAID);

        when(bookingRepository.findById(bookingId))
                .thenReturn(Optional.of(booking));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> paymentService.payBooking(bookingId)
        );

        assertEquals("Booking cannot be paid", ex.getMessage());
        verifyNoInteractions(paymentRepository, eventService);
    }
}
