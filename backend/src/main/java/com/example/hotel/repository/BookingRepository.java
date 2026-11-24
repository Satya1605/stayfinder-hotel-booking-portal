package com.example.hotel.repository;

import com.example.hotel.model.Booking;
import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    Optional<Booking> findByReferenceAndGuestEmailIgnoreCase(String reference, String guestEmail);

    /** Locks the booking row, so two cancel requests for the same booking cannot both succeed. */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select b from Booking b where b.reference = :reference and lower(b.guestEmail) = lower(:email)")
    Optional<Booking> findForUpdate(@Param("reference") String reference, @Param("email") String email);

    boolean existsByReference(String reference);
}
