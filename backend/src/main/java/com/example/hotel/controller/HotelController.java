package com.example.hotel.controller;

import com.example.hotel.dto.AlternativeStay;
import com.example.hotel.dto.HotelResponse;
import com.example.hotel.service.HotelService;
import java.time.LocalDate;
import java.util.List;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/hotels")
public class HotelController {
    private final HotelService service;

    public HotelController(HotelService service) { this.service = service; }

    /** Browse and search. With dates, every hotel also gets a price quote and availability. */
    @GetMapping
    public List<HotelResponse> search(
            @RequestParam(required = false) String city,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return service.search(city, checkIn, checkOut);
    }

    @GetMapping("/{id}")
    public HotelResponse get(
            @PathVariable Long id,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return service.get(id, checkIn, checkOut);
    }

    /** Surprise feature: nearby dates with a free room, for the same number of nights. */
    @GetMapping("/{id}/alternatives")
    public List<AlternativeStay> alternatives(
            @PathVariable Long id,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkIn,
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate checkOut) {
        return service.alternatives(id, checkIn, checkOut);
    }
}
