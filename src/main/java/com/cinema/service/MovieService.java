package com.cinema.service;

import com.cinema.entity.Movie;
import com.cinema.repository.MovieRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class MovieService {
    private final MovieRepository movieRepository;

    public MovieService(MovieRepository movieRepository) {
        this.movieRepository = movieRepository;
    }

    public List<Movie> getShowingMovies() {
        return movieRepository.findByStatus("showing");
    }

    public List<Movie> getAllMovies() {
        return movieRepository.findAll();
    }

    public Optional<Movie> getMovieById(Long id) {
        return movieRepository.findById(id);
    }

    public List<Movie> searchMovies(String keyword) {
        return movieRepository.findByTitleContainingOrGenreContaining(keyword, keyword);
    }
}
