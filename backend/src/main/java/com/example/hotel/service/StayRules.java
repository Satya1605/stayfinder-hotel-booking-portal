package com.example.hotel.service;

import com.example.hotel.exception.ApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;

/** Date rules and pricing for a stay. Pure functions, so they are easy to unit test. */
public final class StayRules {
    public static final int MAX_NIGHTS = 30;

    private StayRules() {}

    public static void validate(LocalDate checkIn, LocalDate checkOut, LocalDate today) {
        if (checkIn == null || checkOut == null) {
            throw ApiException.badRequest("Select both check-in and check-out dates");
        }
        if (checkIn.isBefore(today)) {
            throw ApiException.badRequest("Check-in cannot be in the past");
        }
        if (!checkOut.isAfter(checkIn)) {
            throw ApiException.badRequest("Check-out must be after check-in");
        }
        if (nights(checkIn, checkOut) > MAX_NIGHTS) {
            throw ApiException.badRequest("A stay can be at most " + MAX_NIGHTS + " nights");
        }
    }

    /** The check-out day is not a night of the stay: 10th to 12th is 2 nights (10th and 11th). */
    public static int nights(LocalDate checkIn, LocalDate checkOut) {
        return (int) ChronoUnit.DAYS.between(checkIn, checkOut);
    }

    public static BigDecimal totalAmount(BigDecimal pricePerNight, int nights) {
        return pricePerNight.multiply(BigDecimal.valueOf(nights)).setScale(2, RoundingMode.HALF_UP);
    }
}
