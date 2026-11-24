package com.example.hotel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.hotel.dto.BookingRequest;
import com.example.hotel.dto.BookingResponse;
import com.example.hotel.exception.ApiException;
import com.example.hotel.model.Booking;
import com.example.hotel.model.Hotel;
import com.example.hotel.model.RoomAvailability;
import com.example.hotel.repository.BookingRepository;
import com.example.hotel.repository.HotelRepository;
import com.example.hotel.repository.RoomAvailabilityRepository;
import com.example.hotel.service.BookingService;
import java.math.BigDecimal;
import java.time.Clock;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.test.util.ReflectionTestUtils;

class BookingServiceTest {
    static final Clock CLOCK = Clock.fixed(Instant.parse("2026-11-24T08:00:00Z"), ZoneOffset.UTC);
    static final LocalDate IN = LocalDate.of(2026, 12, 10);
    static final LocalDate OUT = LocalDate.of(2026, 12, 12);

    HotelRepository hotels = mock(HotelRepository.class);
    BookingRepository bookings = mock(BookingRepository.class);
    RoomAvailabilityRepository availability = mock(RoomAvailabilityRepository.class);
    BookingService service = new BookingService(hotels, bookings, availability, CLOCK);
    Hotel hotel = new Hotel("Test Hotel", "Goa", "Addr", "Desc", 4, new BigDecimal("4500"), 2);

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(hotel, "id", 1L);
        when(hotels.findByIdForUpdate(1L)).thenReturn(Optional.of(hotel));
        when(availability.findRange(eq(1L), any(), any())).thenReturn(List.of());
        when(bookings.existsByReference(anyString())).thenReturn(false);
        when(bookings.save(any(Booking.class))).thenAnswer(i -> i.getArgument(0));
        when(availability.saveAll(any())).thenAnswer(i -> i.getArgument(0));
    }

    RoomAvailability row(LocalDate date, int booked) {
        RoomAvailability r = new RoomAvailability(1L, date);
        for (int i = 0; i < booked; i++) r.reserveOne();
        return r;
    }

    BookingRequest request(LocalDate in, LocalDate out) {
        return new BookingRequest(1L, "Asha Rao", "asha@example.com", in, out);
    }

    @Test
    void bookingCalculatesTotalOnTheBackendAndReservesEachNight() {
        BookingResponse r = service.book(request(IN, IN.plusDays(3)));

        assertEquals(new BigDecimal("13500.00"), r.totalAmount()); // 3 nights x 4500
        assertEquals(3, r.nights());
        assertEquals("CONFIRMED", r.status());
        assertTrue(r.reference().startsWith("HB-"));

        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<RoomAvailability>> captor = ArgumentCaptor.forClass(List.class);
        verify(availability).saveAll(captor.capture());
        assertEquals(3, captor.getValue().size());
        captor.getValue().forEach(row -> assertEquals(1, row.getBookedRooms()));
    }

    @Test
    void bookingFailsWithConflictWhenAnyNightIsFull() {
        when(availability.findRange(eq(1L), any(), any()))
                .thenReturn(List.of(row(IN.plusDays(1), 2))); // hotel has 2 rooms, second night is full

        ApiException e = assertThrows(ApiException.class, () -> service.book(request(IN, OUT)));
        assertEquals(409, e.getStatus().value());
    }

    @Test
    void lastRoomCanBeBookedButNotTheOneAfter() {
        when(availability.findRange(eq(1L), any(), any())).thenReturn(List.of(row(IN, 1), row(IN.plusDays(1), 1)));
        assertDoesNotThrow(() -> service.book(request(IN, OUT))); // takes the 2nd (last) room

        when(availability.findRange(eq(1L), any(), any())).thenReturn(List.of(row(IN, 2), row(IN.plusDays(1), 2)));
        assertThrows(ApiException.class, () -> service.book(request(IN, OUT)));
    }

    @Test
    void backToBackStayIsAllowedOnTheCheckoutDay() {
        // Both rooms are taken on the nights of the 10th and 11th (a stay that checks out on the 12th)
        List<RoomAvailability> stored = List.of(row(IN, 2), row(IN.plusDays(1), 2));
        when(availability.findRange(eq(1L), any(), any())).thenAnswer(inv -> {
            LocalDate from = inv.getArgument(1);
            LocalDate to = inv.getArgument(2);
            return stored.stream()
                    .filter(r -> !r.getStayDate().isBefore(from) && r.getStayDate().isBefore(to)).toList();
        });

        assertDoesNotThrow(() -> service.book(request(OUT, OUT.plusDays(1)))); // arrives on the 12th: fine
        assertThrows(ApiException.class, () -> service.book(request(IN, OUT))); // overlaps the full nights
    }

    @Test
    void bookingRejectsPastCheckInAndBadDates() {
        assertThrows(ApiException.class, () -> service.book(request(LocalDate.of(2026, 11, 23), OUT)));
        assertThrows(ApiException.class, () -> service.book(request(OUT, IN)));
        assertThrows(ApiException.class, () -> service.book(request(IN, IN)));
    }

    @Test
    void bookingUnknownHotelIsNotFound() {
        when(hotels.findByIdForUpdate(99L)).thenReturn(Optional.empty());
        ApiException e = assertThrows(ApiException.class,
                () -> service.book(new BookingRequest(99L, "A", "a@b.co", IN, OUT)));
        assertEquals(404, e.getStatus().value());
    }

    private Booking confirmedBooking(LocalDate in, LocalDate out) {
        return new Booking("HB-TEST1234", hotel, "Asha Rao", "asha@example.com", in, out,
                (int) java.time.temporal.ChronoUnit.DAYS.between(in, out), new BigDecimal("9000.00"),
                LocalDateTime.of(2026, 11, 20, 10, 0));
    }

    @Test
    void cancelReleasesTheReservedNights() {
        Booking booking = confirmedBooking(IN, OUT);
        RoomAvailability n1 = row(IN, 1), n2 = row(IN.plusDays(1), 2);
        when(bookings.findForUpdate("HB-TEST1234", "asha@example.com")).thenReturn(Optional.of(booking));
        when(availability.findRange(eq(1L), eq(IN), eq(OUT))).thenReturn(List.of(n1, n2));

        BookingResponse r = service.cancel("hb-test1234", "asha@example.com");

        assertEquals("CANCELLED", r.status());
        assertEquals(0, n1.getBookedRooms());
        assertEquals(1, n2.getBookedRooms());
    }

    @Test
    void cancellingTwiceOrAfterCheckInIsRejected() {
        Booking cancelled = confirmedBooking(IN, OUT);
        cancelled.cancel(LocalDateTime.now(CLOCK));
        when(bookings.findForUpdate("HB-TEST1234", "asha@example.com")).thenReturn(Optional.of(cancelled));
        assertEquals(409, assertThrows(ApiException.class,
                () -> service.cancel("HB-TEST1234", "asha@example.com")).getStatus().value());

        Booking started = confirmedBooking(LocalDate.of(2026, 11, 22), LocalDate.of(2026, 11, 26));
        when(bookings.findForUpdate("HB-OLD00000", "asha@example.com")).thenReturn(Optional.of(started));
        assertEquals(409, assertThrows(ApiException.class,
                () -> service.cancel("HB-OLD00000", "asha@example.com")).getStatus().value());
    }

    @Test
    void wrongEmailGivesNotFound() {
        when(bookings.findByReferenceAndGuestEmailIgnoreCase("HB-TEST1234", "wrong@example.com"))
                .thenReturn(Optional.empty());
        assertEquals(404, assertThrows(ApiException.class,
                () -> service.find("HB-TEST1234", "wrong@example.com")).getStatus().value());
    }
}
