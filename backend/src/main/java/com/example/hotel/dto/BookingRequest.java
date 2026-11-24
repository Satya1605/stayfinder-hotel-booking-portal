package com.example.hotel.dto;

import jakarta.validation.constraints.*;
import java.time.LocalDate;

public record BookingRequest(
        @NotNull(message = "Choose a hotel") Long hotelId,
        @NotBlank(message = "Enter the guest name") @Size(max = 100, message = "Name is too long (max 100 characters)") String guestName,
        @NotBlank(message = "Enter an email address")
        @Size(max = 150, message = "Email is too long (max 150 characters)")
        @Pattern(regexp = "^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$", message = "Enter a valid email address") String guestEmail,
        @NotNull(message = "Choose a check-in date") LocalDate checkIn,
        @NotNull(message = "Choose a check-out date") LocalDate checkOut) {
}
