package com.example.booking.service;

import com.example.booking.cache.AvailabilityCacheService;
import com.example.booking.dto.BookingResponse;
import com.example.booking.dto.CreateBookingRequest;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.entity.Unit;
import com.example.booking.entity.User;
import com.example.booking.repository.BookingRepository;
import com.example.booking.repository.UnitRepository;
import com.example.booking.utils.DateUtils;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static com.example.booking.entity.BookingStatus.CANCELLED;
import static com.example.booking.entity.BookingStatus.PENDING_PAYMENT;
import static com.example.booking.entity.EventType.BOOKING_CANCELLED;
import static com.example.booking.entity.EventType.BOOKING_CREATED;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingServiceImplTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private UnitRepository unitRepository;

    @Mock
    private EventService eventService;

    @Mock
    private AvailabilityCacheService cacheService;

    @InjectMocks
    private BookingServiceImpl bookingService;

    @Test
    void testCreateBooking_AffectsToday() {
        UUID unitId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);

        Unit unit = new Unit();
        unit.setId(unitId);

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.isUnitAvailable(unitId, start, end)).thenReturn(true);

        Booking savedBooking = new Booking();
        UUID bookingId = UUID.randomUUID();
        savedBooking.setId(bookingId);
        savedBooking.setUnit(unit);
        savedBooking.setUser(new User());
        savedBooking.setStartDate(start);
        savedBooking.setEndDate(end);
        savedBooking.setStatus(PENDING_PAYMENT);
        savedBooking.setCreatedAt(Instant.now());

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        try (MockedStatic<DateUtils> utilities = mockStatic(DateUtils.class)) {
            utilities.when(() -> DateUtils.affectsToday(start, end)).thenReturn(true);

            BookingResponse response = bookingService.createBooking(
                    new CreateBookingRequest(unitId, userId, start, end)
            );

            assertNotNull(response);
            assertEquals(bookingId, response.bookingId());

            verify(bookingRepository).save(any(Booking.class));
            verify(eventService).log(eq(BOOKING_CREATED), contains("bookingId=" + bookingId));
            verify(cacheService).decrementAvailableUnits();
        }
    }

    @Test
    void testCreateBooking_DoesNotAffectToday() {
        UUID unitId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(6);

        Unit unit = new Unit();
        unit.setId(unitId);

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.isUnitAvailable(unitId, start, end)).thenReturn(true);

        Booking savedBooking = new Booking();
        UUID bookingId = UUID.randomUUID();
        savedBooking.setId(bookingId);
        savedBooking.setUnit(unit);
        savedBooking.setUser(new User());
        savedBooking.setStartDate(start);
        savedBooking.setEndDate(end);
        savedBooking.setStatus(PENDING_PAYMENT);
        savedBooking.setCreatedAt(Instant.now());

        when(bookingRepository.save(any(Booking.class))).thenReturn(savedBooking);

        try (MockedStatic<DateUtils> utilities = mockStatic(DateUtils.class)) {
            utilities.when(() -> DateUtils.affectsToday(start, end)).thenReturn(false);

            BookingResponse response = bookingService.createBooking(
                    new CreateBookingRequest(unitId, userId, start, end)
            );

            assertNotNull(response);
            assertEquals(bookingId, response.bookingId());

            verify(bookingRepository).save(any(Booking.class));
            verify(eventService).log(eq(BOOKING_CREATED), contains("bookingId=" + bookingId));
            verify(cacheService, never()).decrementAvailableUnits();
        }
    }

    @Test
    void testCancelBooking_AffectsToday() {
        UUID bookingId = UUID.randomUUID();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(PENDING_PAYMENT);
        booking.setStartDate(start);
        booking.setEndDate(end);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        try (MockedStatic<DateUtils> utilities = mockStatic(DateUtils.class)) {
            utilities.when(() -> DateUtils.affectsToday(start, end)).thenReturn(true);

            bookingService.cancelBooking(bookingId);

            assertEquals(CANCELLED, booking.getStatus());
            verify(eventService).log(eq(BOOKING_CANCELLED), contains("bookingId=" + bookingId));
            verify(cacheService).incrementAvailableUnits();
        }
    }

    @Test
    void testCancelBooking_DoesNotAffectToday() {
        UUID bookingId = UUID.randomUUID();
        LocalDate start = LocalDate.now().plusDays(5);
        LocalDate end = LocalDate.now().plusDays(6);

        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(PENDING_PAYMENT);
        booking.setStartDate(start);
        booking.setEndDate(end);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        try (MockedStatic<DateUtils> utilities = mockStatic(DateUtils.class)) {
            utilities.when(() -> DateUtils.affectsToday(start, end)).thenReturn(false);

            bookingService.cancelBooking(bookingId);

            assertEquals(CANCELLED, booking.getStatus());
            verify(cacheService, never()).incrementAvailableUnits();
        }
    }

    @Test
    void testCreateBooking_UnitNotFound() {
        UUID unitId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);

        when(unitRepository.findById(unitId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.createBooking(new CreateBookingRequest(unitId, userId, start, end))
        );

        assertEquals("Unit not found", ex.getMessage());
    }

    @Test
    void testCreateBooking_UnitNotAvailable() {
        UUID unitId = UUID.randomUUID();
        UUID userId = UUID.randomUUID();
        LocalDate start = LocalDate.now();
        LocalDate end = LocalDate.now().plusDays(1);

        Unit unit = new Unit();
        unit.setId(unitId);

        when(unitRepository.findById(unitId)).thenReturn(Optional.of(unit));
        when(bookingRepository.isUnitAvailable(unitId, start, end)).thenReturn(false);

        Exception ex = assertThrows(IllegalStateException.class,
                () -> bookingService.createBooking(new CreateBookingRequest(unitId, userId, start, end))
        );

        assertEquals("Unit is not available for selected dates", ex.getMessage());
    }

    @Test
    void testCancelBooking_NotPendingPayment() {
        UUID bookingId = UUID.randomUUID();
        Booking booking = new Booking();
        booking.setId(bookingId);
        booking.setStatus(BookingStatus.PAID);

        when(bookingRepository.findById(bookingId)).thenReturn(Optional.of(booking));

        Exception ex = assertThrows(IllegalStateException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        assertEquals("Only bookings with status PENDING_PAYMENT can be cancelled", ex.getMessage());
    }

    @Test
    void testCancelBooking_NotFound() {
        UUID bookingId = UUID.randomUUID();
        when(bookingRepository.findById(bookingId)).thenReturn(Optional.empty());

        Exception ex = assertThrows(IllegalArgumentException.class,
                () -> bookingService.cancelBooking(bookingId)
        );

        assertEquals("Booking not found", ex.getMessage());
    }
}
