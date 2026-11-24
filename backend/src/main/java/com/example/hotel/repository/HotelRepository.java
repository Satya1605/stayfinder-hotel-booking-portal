package com.example.hotel.repository;

import com.example.hotel.model.Hotel;
import jakarta.persistence.LockModeType;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface HotelRepository extends JpaRepository<Hotel, Long> {

    List<Hotel> findByCityContainingIgnoreCaseOrderByName(String city);

    List<Hotel> findAllByOrderByName();

    /** SELECT ... FOR UPDATE on the hotel row: bookings for one hotel are processed one at a time. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select h from Hotel h where h.id = :id")
    Optional<Hotel> findByIdForUpdate(@Param("id") Long id);
}
