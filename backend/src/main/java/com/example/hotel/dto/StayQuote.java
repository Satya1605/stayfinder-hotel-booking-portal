package com.example.hotel.dto;

import java.math.BigDecimal;

/** Price and availability of a hotel for one chosen stay. Calculated on the backend. */
public record StayQuote(int nights, BigDecimal totalAmount, int availableRooms) {
}
