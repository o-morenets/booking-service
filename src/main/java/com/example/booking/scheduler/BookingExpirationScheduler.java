package com.example.booking.scheduler;

import com.example.booking.cache.AvailabilityCacheService;
import com.example.booking.entity.Booking;
import com.example.booking.entity.BookingStatus;
import com.example.booking.repository.BookingRepository;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Component
@RequiredArgsConstructor
public class BookingExpirationScheduler {

    private final BookingRepository bookingRepository;
    private final AvailabilityCacheService cacheService;

    /**
     * Cancels unpaid bookings older than 15 minutes
     */
    @Scheduled(fixedRate = 60_000) // every 1 minute
    @Transactional
    public void expireBookings() {
        Instant limit = Instant.now().minus(15, ChronoUnit.MINUTES);

        List<Booking> expired = bookingRepository.findExpired(limit);

        for (Booking booking : expired) {
            booking.setStatus(BookingStatus.EXPIRED);
            cacheService.incrementAvailableUnits();
        }
    }
}
