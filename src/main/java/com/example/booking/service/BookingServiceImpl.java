package com.example.booking.service;

import com.example.booking.cache.AvailabilityCacheService;
import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.entity.Booking;
import com.example.booking.entity.User;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.UnitRepository;
import com.example.booking.utils.DateUtils;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.UUID;

import static com.example.booking.entity.BookingStatus.*;
import static com.example.booking.entity.EventType.BOOKING_CANCELLED;
import static com.example.booking.entity.EventType.BOOKING_CREATED;

@Service
@RequiredArgsConstructor
public class BookingServiceImpl implements BookingService {

    private final BookingRepository bookingRepository;
    private final UnitRepository unitRepository;
    private final EventService eventService;
    private final AvailabilityCacheService cacheService;

    @Override
    @Transactional
    public BookingResponse createBooking(CreateBookingRequest request) {

        // 1. Check if Unit exists
        var unit = unitRepository.findById(request.unitId())
                .orElseThrow(() -> new IllegalArgumentException("Unit not found"));

        // 2. Check for availability for dates range
        boolean available = bookingRepository
                .isUnitAvailable(unit.getId(), request.startDate(), request.endDate());

        if (!available) {
            throw new IllegalStateException("Unit is not available for selected dates");
        }

        User user = new User();
        user.setId(request.userId());

        // 3. Create booking
        Booking booking = new Booking();
        booking.setUnit(unit);
        booking.setUser(user);
        booking.setStartDate(request.startDate());
        booking.setEndDate(request.endDate());
        booking.setStatus(PENDING_PAYMENT);
        booking.setCreatedAt(Instant.now());

        Booking saved = bookingRepository.save(booking);

        // 4. Log the event
        eventService.log(
                BOOKING_CREATED,
                "Booking created, bookingId=" + saved.getId()
        );

        // 5. Update cache - only if booking dates range affects today
        if (DateUtils.affectsToday(booking.getStartDate(), booking.getEndDate())) {
            cacheService.decrementAvailableUnits();
        }

        return BookingResponse.from(saved);
    }

    @Override
    @Transactional
    public void cancelBooking(UUID bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new IllegalArgumentException("Booking not found"));

        if (booking.getStatus() != PENDING_PAYMENT) {
            throw new IllegalStateException("Only bookings with status PENDING_PAYMENT can be cancelled");
        }

        booking.setStatus(CANCELLED);

        eventService.log(
                BOOKING_CANCELLED,
                "Booking cancelled, bookingId=" + booking.getId()
        );

        if (DateUtils.affectsToday(booking.getStartDate(), booking.getEndDate())) {
            cacheService.incrementAvailableUnits();
        }
    }
}
