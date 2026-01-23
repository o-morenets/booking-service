package com.example.booking.utils;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;

class DateUtilsTest {

    @Test
    void affectsToday_whenStartBeforeAndEndAfterToday_returnsTrue() {
        LocalDate today = LocalDate.now();

        boolean result = DateUtils.affectsToday(
                today.minusDays(1),
                today.plusDays(1)
        );

        assertTrue(result);
    }

    @Test
    void affectsToday_whenStartIsTodayAndEndIsToday_returnsTrue() {
        LocalDate today = LocalDate.now();

        boolean result = DateUtils.affectsToday(today, today);

        assertTrue(result);
    }

    @Test
    void affectsToday_whenStartAfterToday_returnsFalse() {
        LocalDate today = LocalDate.now();

        boolean result = DateUtils.affectsToday(
                today.plusDays(1),
                today.plusDays(2)
        );

        assertFalse(result);
    }

    @Test
    void affectsToday_whenEndBeforeToday_returnsFalse() {
        LocalDate today = LocalDate.now();

        boolean result = DateUtils.affectsToday(
                today.minusDays(5),
                today.minusDays(1)
        );

        assertFalse(result);
    }

    @Test
    void affectsToday_whenStartBeforeTodayAndEndIsToday_returnsTrue() {
        LocalDate today = LocalDate.now();

        boolean result = DateUtils.affectsToday(
                today.minusDays(3),
                today
        );

        assertTrue(result);
    }

    @Test
    void affectsToday_whenStartIsTodayAndEndAfterToday_returnsTrue() {
        LocalDate today = LocalDate.now();

        boolean result = DateUtils.affectsToday(
                today,
                today.plusDays(10)
        );

        assertTrue(result);
    }
}
