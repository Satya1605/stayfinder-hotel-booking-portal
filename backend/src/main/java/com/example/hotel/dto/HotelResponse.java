package com.example.hotel.dto;

import com.example.hotel.model.Hotel;
import java.math.BigDecimal;
import java.util.List;

/** stay is null when no dates were chosen. */
public record HotelResponse(Long id, String name, String city, String address, String description,
                            int stars, List<String> amenities, BigDecimal pricePerNight,
                            int totalRooms, StayQuote stay) {

    public static HotelResponse from(Hotel h, StayQuote stay) {
        return new HotelResponse(h.getId(), h.getName(), h.getCity(), h.getAddress(), h.getDescription(),
                h.getStars(), h.getAmenities().stream().sorted().toList(), h.getPricePerNight(),
                h.getTotalRooms(), stay);
    }
}
