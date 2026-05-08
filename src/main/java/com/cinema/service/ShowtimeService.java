package com.cinema.service;

import com.cinema.entity.Showtime;
import com.cinema.repository.ShowtimeRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository) {
        this.showtimeRepository = showtimeRepository;
    }

    public List<Showtime> getByMovieId(Long movieId) {
        return showtimeRepository.findByMovieId(movieId);
    }

    public List<Showtime> getByMovieIdAndDate(Long movieId, String date) {
        return showtimeRepository.findByMovieIdAndShowDate(movieId, date);
    }

    public List<Showtime> getByDate(String date) {
        return showtimeRepository.findByShowDate(date);
    }

    public Showtime getById(Long id) {
        return showtimeRepository.findById(id).orElse(null);
    }
}
