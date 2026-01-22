package com.example.booking.utils;

import java.time.LocalDate;

public class DateUtils {

    public static boolean affectsToday(LocalDate start, LocalDate end) {
        LocalDate today = LocalDate.now();
        return !start.isAfter(today) && !end.isBefore(today);
    }
}
