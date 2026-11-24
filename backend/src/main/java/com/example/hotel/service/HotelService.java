package com.example.hotel.service;

import com.example.hotel.dto.AlternativeStay;
import com.example.hotel.dto.HotelResponse;
import com.example.hotel.dto.StayQuote;
import com.example.hotel.exception.ApiException;
import com.example.hotel.model.Hotel;
import com.example.hotel.repository.HotelRepository;
import java.time.Clock;
import java.time.LocalDate;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class HotelService {
    private final HotelRepository hotels;
    private final AvailabilityService availability;
    private final Clock clock;

    public HotelService(HotelRepository hotels, AvailabilityService availability, Clock clock) {
        this.hotels = hotels;
        this.availability = availability;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<HotelResponse> search(String city, LocalDate checkIn, LocalDate checkOut) {
        boolean hasDates = validateOptionalDates(checkIn, checkOut);
        List<Hotel> found = (city == null || city.isBlank())
                ? hotels.findAllByOrderByName()
                : hotels.findByCityContainingIgnoreCaseOrderByName(city.trim());
        return found.stream()
                .map(h -> HotelResponse.from(h, hasDates ? quote(h, checkIn, checkOut) : null))
                .toList();
    }

    @Transactional(readOnly = true)
    public HotelResponse get(Long id, LocalDate checkIn, LocalDate checkOut) {
        boolean hasDates = validateOptionalDates(checkIn, checkOut);
        Hotel hotel = find(id);
        return HotelResponse.from(hotel, hasDates ? quote(hotel, checkIn, checkOut) : null);
    }

    @Transactional(readOnly = true)
    public List<AlternativeStay> alternatives(Long id, LocalDate checkIn, LocalDate checkOut) {
        LocalDate today = LocalDate.now(clock);
        StayRules.validate(checkIn, checkOut, today);
        return availability.alternatives(find(id), checkIn, checkOut, today);
    }

    private Hotel find(Long id) {
        return hotels.findById(id).orElseThrow(() -> ApiException.notFound("Hotel not found"));
    }

    private StayQuote quote(Hotel hotel, LocalDate checkIn, LocalDate checkOut) {
        int nights = StayRules.nights(checkIn, checkOut);
        return new StayQuote(nights, StayRules.totalAmount(hotel.getPricePerNight(), nights),
                availability.freeRooms(hotel, checkIn, checkOut));
    }

    /** Both dates empty is fine (just browsing). Otherwise both must be valid. */
    private boolean validateOptionalDates(LocalDate checkIn, LocalDate checkOut) {
        if (checkIn == null && checkOut == null) return false;
        StayRules.validate(checkIn, checkOut, LocalDate.now(clock));
        return true;
    }
}
