package com.example.hotel.service;

import com.example.hotel.dto.BookingRequest;
import com.example.hotel.dto.BookingResponse;
import com.example.hotel.exception.ApiException;
import com.example.hotel.model.Booking;
import com.example.hotel.model.Hotel;
import com.example.hotel.model.RoomAvailability;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomAvailabilityRepository;
import java.security.SecureRandom;
import java.time.Clock;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class BookingService {
    private static final String REFERENCE_CHARS = "ABCDEFGHJKMNPQRSTUVWXYZ23456789"; // no 0/O or 1/I
    private final SecureRandom random = new SecureRandom();

    private final HotelRepository hotels;
    private final BookingRepository bookings;
    private final RoomAvailabilityRepository availability;
    private final Clock clock;

    public BookingService(HotelRepository hotels, BookingRepository bookings,
                          RoomAvailabilityRepository availability, Clock clock) {
        this.hotels = hotels;
        this.bookings = bookings;
        this.availability = availability;
        this.clock = clock;
    }

    /**
     * READ_COMMITTED: every query sees the latest committed data.
     * The hotel row lock (FOR UPDATE) makes two guests booking the same hotel wait for each other,
     * so the last room can only be given to one of them.
     */
    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse book(BookingRequest req) {
        LocalDate today = LocalDate.now(clock);
        StayRules.validate(req.checkIn(), req.checkOut(), today);

        Hotel hotel = hotels.findByIdForUpdate(req.hotelId())
                .orElseThrow(() -> ApiException.notFound("Hotel not found"));

        // One availability row per night; create the rows that do not exist yet
        Map<LocalDate, RoomAvailability> existing = availability
                .findRange(hotel.getId(), req.checkIn(), req.checkOut()).stream()
                .collect(Collectors.toMap(RoomAvailability::getStayDate, Function.identity()));
        List<RoomAvailability> nightsToReserve = new ArrayList<>();
        for (LocalDate night = req.checkIn(); night.isBefore(req.checkOut()); night = night.plusDays(1)) {
            RoomAvailability row = existing.getOrDefault(night, new RoomAvailability(hotel.getId(), night));
            if (row.getBookedRooms() >= hotel.getTotalRooms()) {
                throw ApiException.conflict("No room is available for the selected dates. Please try other dates.");
            }
            nightsToReserve.add(row);
        }
        nightsToReserve.forEach(RoomAvailability::reserveOne);
        availability.saveAll(nightsToReserve);

        int nights = StayRules.nights(req.checkIn(), req.checkOut());
        Booking booking = new Booking(newReference(), hotel, req.guestName().trim(), req.guestEmail().trim(),
                req.checkIn(), req.checkOut(), nights,
                StayRules.totalAmount(hotel.getPricePerNight(), nights), LocalDateTime.now(clock));
        return BookingResponse.from(bookings.save(booking), today);
    }

    @Transactional(readOnly = true)
    public BookingResponse find(String reference, String email) {
        Booking booking = bookings
                .findByReferenceAndGuestEmailIgnoreCase(normalise(reference), email.trim())
                .orElseThrow(BookingService::notFound);
        return BookingResponse.from(booking, LocalDate.now(clock));
    }

    @Transactional(isolation = Isolation.READ_COMMITTED)
    public BookingResponse cancel(String reference, String email) {
        LocalDate today = LocalDate.now(clock);
        Booking booking = bookings.findForUpdate(normalise(reference), email.trim())
                .orElseThrow(BookingService::notFound);

        if (booking.isCancelled()) {
            throw ApiException.conflict("This booking is already cancelled");
        }
        if (booking.getCheckIn().isBefore(today)) {
            throw ApiException.conflict("This stay has already started, so it can no longer be cancelled");
        }

        hotels.findByIdForUpdate(booking.getHotel().getId()); // same lock as booking
        List<RoomAvailability> rows = availability.findRange(
                booking.getHotel().getId(), booking.getCheckIn(), booking.getCheckOut());
        rows.forEach(RoomAvailability::releaseOne);
        availability.saveAll(rows);

        booking.cancel(LocalDateTime.now(clock));
        return BookingResponse.from(booking, today);
    }

    private static ApiException notFound() {
        // Same message for a wrong reference and a wrong email, so nobody can probe for valid references
        return ApiException.notFound("No booking found for that reference and email");
    }

    private static String normalise(String reference) {
        return reference.trim().toUpperCase();
    }

    private String newReference() {
        String reference;
        do {
            StringBuilder sb = new StringBuilder("HB-");
            for (int i = 0; i < 8; i++) sb.append(REFERENCE_CHARS.charAt(random.nextInt(REFERENCE_CHARS.length())));
            reference = sb.toString();
        } while (bookings.existsByReference(reference));
        return reference;
    }
}
