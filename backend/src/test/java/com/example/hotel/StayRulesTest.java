package com.example.hotel;

import static org.junit.jupiter.api.Assertions.*;

import com.example.hotel.exception.ApiException;
import com.example.hotel.service.StayRules;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.junit.jupiter.api.Test;

class StayRulesTest {
    static final LocalDate TODAY = LocalDate.of(2026, 11, 24);

    @Test
    void totalIsPricePerNightTimesNights() {
        assertEquals(new BigDecimal("13500.00"), StayRules.totalAmount(new BigDecimal("4500"), 3));
        assertEquals(new BigDecimal("6500.00"), StayRules.totalAmount(new BigDecimal("6500.00"), 1));
    }

    @Test
    void nightsExcludeTheCheckoutDay() {
        assertEquals(2, StayRules.nights(LocalDate.of(2026, 12, 10), LocalDate.of(2026, 12, 12)));
    }

    @Test
    void acceptsCheckInToday() {
        assertDoesNotThrow(() -> StayRules.validate(TODAY, TODAY.plusDays(1), TODAY));
    }

    @Test
    void rejectsCheckInInThePast() {
        ApiException e = assertThrows(ApiException.class,
                () -> StayRules.validate(TODAY.minusDays(1), TODAY.plusDays(2), TODAY));
        assertEquals(400, e.getStatus().value());
    }

    @Test
    void rejectsCheckOutOnOrBeforeCheckIn() {
        assertThrows(ApiException.class, () -> StayRules.validate(TODAY.plusDays(5), TODAY.plusDays(5), TODAY));
        assertThrows(ApiException.class, () -> StayRules.validate(TODAY.plusDays(5), TODAY.plusDays(3), TODAY));
    }

    @Test
    void rejectsMissingDatesAndVeryLongStays() {
        assertThrows(ApiException.class, () -> StayRules.validate(null, TODAY.plusDays(2), TODAY));
        assertThrows(ApiException.class, () -> StayRules.validate(TODAY, TODAY.plusDays(31), TODAY));
        assertDoesNotThrow(() -> StayRules.validate(TODAY, TODAY.plusDays(30), TODAY));
    }
}
