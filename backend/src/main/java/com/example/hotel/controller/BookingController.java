package com.example.hotel.controller;

import com.example.hotel.dto.BookingRequest;
import com.example.hotel.dto.BookingResponse;
import com.example.hotel.service.BookingService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/bookings")
public class BookingController {
    private final BookingService service;

    public BookingController(BookingService service) { this.service = service; }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public BookingResponse book(@Valid @RequestBody BookingRequest request) {
        return service.book(request);
    }

    @GetMapping("/{reference}")
    public BookingResponse find(@PathVariable String reference, @RequestParam String email) {
        return service.find(reference, email);
    }

    @PostMapping("/{reference}/cancel")
    public BookingResponse cancel(@PathVariable String reference, @RequestParam String email) {
        return service.cancel(reference, email);
    }
}
