package com.cinema.repository;

import com.cinema.entity.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SeatRepository extends JpaRepository<Seat, Long> {
    List<Seat> findByHallId(Long hallId);
    List<Seat> findByHallIdAndRowLabel(Long hallId, String rowLabel);
}
