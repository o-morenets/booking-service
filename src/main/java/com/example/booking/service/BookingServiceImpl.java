package com.example.booking.service;

import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.EventType;
import com.example.booking.entity.Payment;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.PaymentRepository;
import com.example.booking.repository.UnitRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

import static com.example.booking.entity.BookingStatus.PAID;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final PaymentRepository paymentRepository;
    private final UnitRepository unitRepository;
    private final EventService eventService;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        // 1. Перевіряємо, що Unit існує
        var unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new IllegalArgumentException("Unit not found"));

        // 2. Перевіряємо доступність юніта на діапазон дат
        boolean available = bookingRepository
                .isUnitAvailable(unit.getId(), request.startDate(), request.endDate());

        if (!available) {
            throw new IllegalStateException("Unit is not available for selected dates");
        }

        // 3. Створюємо booking
        Booking booking = new Booking();
        booking.setUnit(unit);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setStatus(BookingStatus.PENDING_PAYMENT);
        booking.setCreatedAt(Instant.now());
        booking.setExpiresAt(Instant.now().plus(15, ChronoUnit.MINUTES));

        Booking saved = bookingRepository.save(booking);

        eventService.log(
                EventType.BOOKING_CREATED,
                "Booking created, bookingId=" + saved.getId()
        );

        return BookingResponse.from(saved);
    }

    @Override
    @Transactional
    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() == PAID) {
            throw new IllegalStateException("Paid booking cannot be cancelled");
        }

        booking.setStatus(BookingStatus.CANCELLED);
    }

    @Override
    @Transactional
    public void pay(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != BookingStatus.PENDING_PAYMENT) {
            throw new IllegalStateException("Booking cannot be paid");
        }

        booking.setStatus(PAID);

        Payment payment = new Payment();
        payment.setBooking(booking);
        payment.setAmount(booking.getUnit().getBaseCost());
        payment.setCreatedAt(Instant.now());

        paymentRepository.save(payment);
    }
}
