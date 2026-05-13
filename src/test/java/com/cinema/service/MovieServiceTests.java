package com.cinema.service;

import com.cinema.entity.Movie;
import com.cinema.repository.MovieRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class MovieServiceTests {

    @Mock
    private MovieRepository movieRepository;

    @InjectMocks
    private MovieService movieService;

    @Test
    void testGetShowingMovies() {
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Test Movie");
        movie1.setStatus("showing");

        Movie movie2 = new Movie();
        movie2.setId(2L);
        movie2.setTitle("Another Movie");
        movie2.setStatus("showing");

        when(movieRepository.findByStatus("showing")).thenReturn(Arrays.asList(movie1, movie2));

        List<Movie> movies = movieService.getShowingMovies();

        assertEquals(2, movies.size());
        verify(movieRepository, times(1)).findByStatus("showing");
    }

    @Test
    void testGetAllMovies() {
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Test Movie");

        Movie movie2 = new Movie();
        movie2.setId(2L);
        movie2.setTitle("Another Movie");

        when(movieRepository.findAll()).thenReturn(Arrays.asList(movie1, movie2));

        List<Movie> movies = movieService.getAllMovies();

        assertEquals(2, movies.size());
        verify(movieRepository, times(1)).findAll();
    }

    @Test
    void testGetMovieById() {
        Movie movie = new Movie();
        movie.setId(1L);
        movie.setTitle("Test Movie");

        when(movieRepository.findById(1L)).thenReturn(Optional.of(movie));

        Optional<Movie> found = movieService.getMovieById(1L);

        assertTrue(found.isPresent());
        assertEquals("Test Movie", found.get().getTitle());
    }

    @Test
    void testGetMovieByIdNotFound() {
        when(movieRepository.findById(99L)).thenReturn(Optional.empty());

        Optional<Movie> found = movieService.getMovieById(99L);

        assertFalse(found.isPresent());
    }

    @Test
    void testSearchMovies() {
        Movie movie1 = new Movie();
        movie1.setId(1L);
        movie1.setTitle("Action Movie");
        movie1.setGenre("Action");

        when(movieRepository.findByTitleContainingOrGenreContaining("Action", "Action"))
                .thenReturn(Arrays.asList(movie1));

        List<Movie> movies = movieService.searchMovies("Action");

        assertEquals(1, movies.size());
        assertEquals("Action Movie", movies.get(0).getTitle());
    }
}