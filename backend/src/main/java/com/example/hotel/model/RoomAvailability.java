package com.example.hotel.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/** How many rooms of a hotel are booked on one night. */
@Entity
@Table(name = "room_availability")
public class RoomAvailability {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long hotelId;
    private LocalDate stayDate;
    private int bookedRooms;

    protected RoomAvailability() {}

    public RoomAvailability(Long hotelId, LocalDate stayDate) {
        this.hotelId = hotelId;
        this.stayDate = stayDate;
        this.bookedRooms = 0;
    }

    public Long getHotelId() { return hotelId; }
    public LocalDate getStayDate() { return stayDate; }
    public int getBookedRooms() { return bookedRooms; }

    public void reserveOne() { bookedRooms++; }
    public void releaseOne() { if (bookedRooms > 0) bookedRooms--; }
}
