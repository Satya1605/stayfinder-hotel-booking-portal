package com.example.hotel.dto;

import com.example.hotel.model.Booking;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

public record BookingResponse(String reference, Long hotelId, String hotelName, String city,
                              String guestName, String guestEmail, LocalDate checkIn, LocalDate checkOut,
                              int nights, BigDecimal pricePerNight, BigDecimal totalAmount,
                              String status, boolean cancellable, LocalDateTime createdAt) {

    public static BookingResponse from(Booking b, LocalDate today) {
        boolean cancellable = !b.isCancelled() && !b.getCheckIn().isBefore(today);
        return new BookingResponse(b.getReference(), b.getHotel().getId(), b.getHotel().getName(),
                b.getHotel().getCity(), b.getGuestName(), b.getGuestEmail(), b.getCheckIn(), b.getCheckOut(),
                b.getNights(), b.getHotel().getPricePerNight(), b.getTotalAmount(), b.getStatus(),
                cancellable, b.getCreatedAt());
    }
}
