package com.example.booking.repository;

import com.example.booking.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public interface BookingRepository extends JpaRepository<Booking, UUID> {

    @Query("""
            SELECT b FROM Booking b
            WHERE b.status = 'PENDING_PAYMENT' AND b.createdAt < :limit
            """)
    List<Booking> findExpired(Instant limit);
}
