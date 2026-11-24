package com.example.hotel.repository;

import com.example.hotel.model.RoomAvailability;
import java.time.LocalDate;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface RoomAvailabilityRepository extends JpaRepository<RoomAvailability, Long> {

    /** Nights from (inclusive) to (exclusive) that have at least one booking. */
    @Query("select a from RoomAvailability a where a.hotelId = :hotelId and a.stayDate >= :from and a.stayDate < :to")
    List<RoomAvailability> findRange(@Param("hotelId") Long hotelId,
                                     @Param("from") LocalDate from,
                                     @Param("to") LocalDate to);
}
