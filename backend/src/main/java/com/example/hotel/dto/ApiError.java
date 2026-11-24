package com.example.hotel.dto;

import java.time.LocalDateTime;
import java.util.Map;

/** Every error from the API has this shape. */
public record ApiError(int status, String error, String message,
                       Map<String, String> fieldErrors, LocalDateTime timestamp) {
}
