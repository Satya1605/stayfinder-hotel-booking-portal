package com.example.hotel.service;

import com.example.hotel.dto.AlternativeStay;
import com.example.hotel.model.Hotel;
import com.example.hotel.model.RoomAvailability;
import com.example.hotel.repository.RoomAvailabilityRepository;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class AvailabilityService {
    static final int SEARCH_RADIUS_DAYS = 14;
    static final int MAX_SUGGESTIONS = 3;

    private final RoomAvailabilityRepository repository;

    public AvailabilityService(RoomAvailabilityRepository repository) {
        this.repository = repository;
    }

    /** Booked rooms per night, for nights from (inclusive) to (exclusive). Missing nights mean 0 booked. */
    public Map<LocalDate, Integer> bookedByDate(Long hotelId, LocalDate from, LocalDate to) {
        return repository.findRange(hotelId, from, to).stream()
                .collect(Collectors.toMap(RoomAvailability::getStayDate, RoomAvailability::getBookedRooms));
    }

    public int freeRooms(Hotel hotel, LocalDate checkIn, LocalDate checkOut) {
        return freeRooms(hotel.getTotalRooms(), bookedByDate(hotel.getId(), checkIn, checkOut), checkIn, checkOut);
    }

    /**
     * Rooms free for the WHOLE stay = the smallest number free on any single night.
     * The check-out day is not included, so back-to-back stays never clash.
     */
    public static int freeRooms(int totalRooms, Map<LocalDate, Integer> booked, LocalDate checkIn, LocalDate checkOut) {
        int free = totalRooms;
        for (LocalDate night = checkIn; night.isBefore(checkOut); night = night.plusDays(1)) {
            free = Math.min(free, totalRooms - booked.getOrDefault(night, 0));
        }
        return Math.max(free, 0);
    }

    /**
     * Surprise feature: when the chosen dates are sold out, find the nearest stays of the SAME length
     * (up to 14 days earlier or later) that still have a free room. One database query covers the whole search.
     */
    public List<AlternativeStay> alternatives(Hotel hotel, LocalDate checkIn, LocalDate checkOut, LocalDate today) {
        int nights = StayRules.nights(checkIn, checkOut);
        Map<LocalDate, Integer> booked = bookedByDate(hotel.getId(),
                checkIn.minusDays(SEARCH_RADIUS_DAYS), checkOut.plusDays(SEARCH_RADIUS_DAYS));

        List<AlternativeStay> found = new ArrayList<>();
        for (int offset = 1; offset <= SEARCH_RADIUS_DAYS && found.size() < MAX_SUGGESTIONS; offset++) {
            for (int direction : new int[] {-1, 1}) { // earlier first, then later
                LocalDate start = checkIn.plusDays((long) direction * offset);
                LocalDate end = start.plusDays(nights);
                if (start.isBefore(today) || found.size() >= MAX_SUGGESTIONS) continue;
                if (freeRooms(hotel.getTotalRooms(), booked, start, end) > 0) {
                    found.add(new AlternativeStay(start, end, nights,
                            StayRules.totalAmount(hotel.getPricePerNight(), nights), (long) direction * offset));
                }
            }
        }
        return found;
    }
}
