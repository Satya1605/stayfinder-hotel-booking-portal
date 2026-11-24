package com.example.hotel.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Table(name = "bookings")
public class Booking {
    public static final String CONFIRMED = "CONFIRMED";
    public static final String CANCELLED = "CANCELLED";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;

    @ManyToOne(fetch = FetchType.EAGER, optional = false)
    @JoinColumn(name = "hotel_id")
    private Hotel hotel;

    private String guestName;
    private String guestEmail;
    private LocalDate checkIn;
    private LocalDate checkOut;
    private int nights;
    private BigDecimal totalAmount;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime cancelledAt;

    protected Booking() {}

    public Booking(String reference, Hotel hotel, String guestName, String guestEmail,
                   LocalDate checkIn, LocalDate checkOut, int nights, BigDecimal totalAmount,
                   LocalDateTime createdAt) {
        this.reference = reference;
        this.hotel = hotel;
        this.guestName = guestName;
        this.guestEmail = guestEmail;
        this.checkIn = checkIn;
        this.checkOut = checkOut;
        this.nights = nights;
        this.totalAmount = totalAmount;
        this.status = CONFIRMED;
        this.createdAt = createdAt;
    }

    public String getReference() { return reference; }
    public Hotel getHotel() { return hotel; }
    public String getGuestName() { return guestName; }
    public String getGuestEmail() { return guestEmail; }
    public LocalDate getCheckIn() { return checkIn; }
    public LocalDate getCheckOut() { return checkOut; }
    public int getNights() { return nights; }
    public BigDecimal getTotalAmount() { return totalAmount; }
    public String getStatus() { return status; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getCancelledAt() { return cancelledAt; }

    public boolean isCancelled() { return CANCELLED.equals(status); }

    public void cancel(LocalDateTime when) {
        this.status = CANCELLED;
        this.cancelledAt = when;
    }
}
