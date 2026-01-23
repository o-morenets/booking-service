package com.example.booking.scheduler;

import com.example.booking.cache.AvailabilityCacheService;
import com.example.booking.entity.Booking;
import com.example.booking.repository.BookingRepository;
import com.example.booking.service.EventService;
import com.example.booking.utils.DateUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static com.example.booking.entity.BookingStatus.EXPIRED;
import static com.example.booking.entity.EventType.BOOKING_EXPIRED;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class BookingExpirationSchedulerTest {

    @Mock
    private BookingRepository bookingRepository;

    @Mock
    private EventService eventService;

    @Mock
    private AvailabilityCacheService cacheService;

    @InjectMocks
    private BookingExpirationScheduler scheduler;

    private Booking booking1;
    private Booking booking2;

    @BeforeEach
    void setUp() {
        booking1 = new Booking();
        booking1.setId(UUID.randomUUID());
        booking1.setStartDate(LocalDate.now());
        booking1.setEndDate(LocalDate.now());

        booking2 = new Booking();
        booking2.setId(UUID.randomUUID());
        booking2.setStartDate(LocalDate.now().minusDays(1));
        booking2.setEndDate(LocalDate.now().minusDays(1));
    }

    @Test
    void expireBookings_marksExpiredAndLogsAndUpdatesCache() {
        List<Booking> expiredBookings = List.of(booking1, booking2);

        when(bookingRepository.findExpired(any(Instant.class)))
                .thenReturn(expiredBookings);

        try (MockedStatic<DateUtils> dateUtilsMock = mockStatic(DateUtils.class)) {
            dateUtilsMock.when(() -> DateUtils.affectsToday(booking1.getStartDate(), booking1.getEndDate()))
                    .thenReturn(true);
            dateUtilsMock.when(() -> DateUtils.affectsToday(booking2.getStartDate(), booking2.getEndDate()))
                    .thenReturn(false);

            scheduler.expireBookings();

            verify(cacheService, times(1)).incrementAvailableUnits();

            verify(eventService).log(BOOKING_EXPIRED, "Booking expired, bookingId=" + booking1.getId());
            verify(eventService).log(BOOKING_EXPIRED, "Booking expired, bookingId=" + booking2.getId());

            assertEquals(EXPIRED, booking1.getStatus());
            assertEquals(EXPIRED, booking2.getStatus());
        }

        verify(bookingRepository).findExpired(any(Instant.class));
    }

    @Test
    void expireBookings_noExpiredBookings_nothingHappens() {
        when(bookingRepository.findExpired(any(Instant.class))).thenReturn(List.of());

        scheduler.expireBookings();

        verifyNoInteractions(eventService, cacheService);
    }
}
