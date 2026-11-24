package com.example.hotel;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.example.hotel.dto.AlternativeStay;
import com.example.hotel.model.Hotel;
import com.example.hotel.model.RoomAvailability;
import com.example.hotel.repository.RoomAvailabilityRepository;
import com.example.hotel.service.AvailabilityService;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class AvailabilityServiceTest {
    static final LocalDate TODAY = LocalDate.of(2026, 11, 24);
    static final LocalDate IN = LocalDate.of(2026, 12, 10);
    static final LocalDate OUT = LocalDate.of(2026, 12, 12);

    RoomAvailabilityRepository repo = mock(RoomAvailabilityRepository.class);
    AvailabilityService service = new AvailabilityService(repo);

    Hotel hotel() {
        Hotel h = new Hotel("Test", "Goa", "Addr", "Desc", 4, new BigDecimal("1000"), 2);
        ReflectionTestUtils.setField(h, "id", 1L);
        return h;
    }

    RoomAvailability row(LocalDate date, int booked) {
        RoomAvailability r = new RoomAvailability(1L, date);
        for (int i = 0; i < booked; i++) r.reserveOne();
        return r;
    }

    @Test
    void freeRoomsIsTheTightestNightOfTheStay() {
        Map<LocalDate, Integer> booked = Map.of(IN, 1, IN.plusDays(1), 2);
        assertEquals(0, AvailabilityService.freeRooms(2, booked, IN, OUT)); // second night is full
        assertEquals(1, AvailabilityService.freeRooms(2, booked, IN, IN.plusDays(1))); // first night only
    }

    @Test
    void checkoutDayDoesNotBlockTheNextGuest() {
        Map<LocalDate, Integer> booked = Map.of(IN, 2, IN.plusDays(1), 2); // full on the 10th and 11th
        // a guest arriving on the 12th is fine, because the earlier stay checks out on the 12th
        assertEquals(2, AvailabilityService.freeRooms(2, booked, OUT, OUT.plusDays(1)));
    }

    @Test
    void alternativesAreNearbyStaysOfTheSameLengthWithAFreeRoom() {
        // Hotel has 2 rooms. Both are taken on 10, 11 and 12 December.
        when(repo.findRange(eq(1L), any(), any())).thenReturn(List.of(
                row(IN, 2), row(IN.plusDays(1), 2), row(IN.plusDays(2), 2)));

        List<AlternativeStay> result = service.alternatives(hotel(), IN, OUT, TODAY);

        assertEquals(3, result.size());
        // 8th-10th is the nearest free stay (2 days earlier); 9th-11th clashes with the full 10th
        assertEquals(LocalDate.of(2026, 12, 8), result.get(0).checkIn());
        assertEquals(2, result.get(0).nights());
        assertEquals(new BigDecimal("2000.00"), result.get(0).totalAmount());
        result.forEach(a -> assertTrue(AvailabilityService.freeRooms(2,
                Map.of(IN, 2, IN.plusDays(1), 2, IN.plusDays(2), 2), a.checkIn(), a.checkOut()) > 0));
    }

    @Test
    void alternativesNeverStartInThePast() {
        when(repo.findRange(eq(1L), any(), any())).thenReturn(List.of());
        List<AlternativeStay> result = service.alternatives(hotel(), TODAY, TODAY.plusDays(2), TODAY);
        result.forEach(a -> assertFalse(a.checkIn().isBefore(TODAY)));
    }
}
