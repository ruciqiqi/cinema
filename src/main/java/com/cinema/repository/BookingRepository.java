package com.cinema.repository;

import com.cinema.entity.Booking;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {
    Booking findByBookingCode(String bookingCode);
    List<Booking> findByUserPhoneOrderByCreatedAtDesc(String userPhone);
}
