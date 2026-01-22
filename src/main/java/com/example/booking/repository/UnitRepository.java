package com.example.booking.repository;

import com.example.booking.entity.Unit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

import java.util.UUID;

public interface UnitRepository extends JpaRepository<Unit, UUID>, JpaSpecificationExecutor<Unit> {

    @Query("""
            SELECT COUNT(u)
            FROM Unit u
            WHERE NOT EXISTS (
                SELECT b FROM Booking b
                WHERE b.unit = u
                    AND b.status IN ('PENDING_PAYMENT', 'PAID')
                    AND CURRENT_DATE BETWEEN b.startDate AND b.endDate
            )
            """)
    long countAvailableToday();
}
