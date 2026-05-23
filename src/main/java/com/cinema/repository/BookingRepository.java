package com.cinema.repository;

import com.cinema.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking findByBookingCode(String bookingCode);
    List<Booking> findByUserPhoneOrderByCreatedAtDesc(String userPhone);
    
    List<Booking> findByUserIdOrderByCreatedAtDesc(Long userId);
    List<Booking> findByUserId(Long userId);
    
    @Query("SELECT b FROM Booking b WHERE b.showtimeId = :showtimeId AND b.status = 'confirmed'")
    List<Booking> findConfirmedByShowtimeId(@Param("showtimeId") Long showtimeId);
}
