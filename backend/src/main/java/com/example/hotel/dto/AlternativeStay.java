package com.example.hotel.dto;

import java.math.BigDecimal;
import java.time.LocalDate;

/** Surprise feature: a nearby stay of the same length that still has a free room. */
public record AlternativeStay(LocalDate checkIn, LocalDate checkOut, int nights,
                              BigDecimal totalAmount, long daysFromRequested) {
}
