package com.cinema.repository;

import com.cinema.entity.Showtime;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ShowtimeRepository extends JpaRepository<Showtime, Long> {
    List<Showtime> findByMovieId(Long movieId);
    List<Showtime> findByMovieIdAndShowDate(Long movieId, String showDate);
    List<Showtime> findByShowDate(String showDate);
    List<Showtime> findByShowDateGreaterThanEqual(String showDate);
    List<Showtime> findByMovieIdAndShowDateGreaterThanEqual(Long movieId, String showDate);
    List<Showtime> findByMovieIdAndHallIdAndShowDate(Long movieId, Long hallId, String showDate);
    List<Showtime> findByHallIdAndShowDate(Long hallId, String showDate);
    int deleteByShowDateLessThan(String showDate);
}
