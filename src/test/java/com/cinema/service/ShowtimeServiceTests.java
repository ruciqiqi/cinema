package com.cinema.service;

import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Showtime;
import com.cinema.repository.HallRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ShowtimeRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ShowtimeServiceTests {

    @Mock private ShowtimeRepository showtimeRepository;
    @Mock private MovieRepository movieRepository;
    @Mock private HallRepository hallRepository;

    @InjectMocks
    private ShowtimeService showtimeService;

    private Showtime showtime1;
    private Showtime showtime2;
    private Movie movie;
    private String today;
    private String tomorrow;

    @BeforeEach
    void setUp() {
        today = LocalDate.now().toString();
        tomorrow = LocalDate.now().plusDays(1).toString();

        movie = new Movie();
        movie.setId(1L);
        movie.setTitle("测试电影");
        movie.setStatus("showing");

        showtime1 = new Showtime();
        showtime1.setId(1L);
        showtime1.setMovieId(1L);
        showtime1.setHallId(1L);
        showtime1.setHallName("1号厅");
        showtime1.setShowDate(today);
        showtime1.setShowTime("14:30");
        showtime1.setPriceStandard(39.9);
        showtime1.setPriceVip(59.9);

        showtime2 = new Showtime();
        showtime2.setId(2L);
        showtime2.setMovieId(1L);
        showtime2.setHallId(1L);
        showtime2.setHallName("1号厅");
        showtime2.setShowDate(today);
        showtime2.setShowTime("19:00");
        showtime2.setPriceStandard(39.9);
        showtime2.setPriceVip(59.9);
    }

    // ==================== SS-01 按电影ID查询未来场次 ====================
    @Test
    void testGetByMovieIdReturnsFutureShowtimes() {
        when(showtimeRepository.findByMovieIdAndShowDateGreaterThanEqual(1L, today))
                .thenReturn(Arrays.asList(showtime1, showtime2));

        java.util.List<Showtime> list = showtimeService.getByMovieId(1L);

        assertEquals(2, list.size());
        verify(showtimeRepository, times(1))
                .findByMovieIdAndShowDateGreaterThanEqual(1L, today);
    }

    // ==================== SS-02 按电影ID和日期查询场次 ====================
    @Test
    void testGetByMovieIdAndDate() {
        when(showtimeRepository.findByMovieIdAndShowDate(1L, tomorrow))
                .thenReturn(Arrays.asList(showtime1));

        java.util.List<Showtime> list = showtimeService.getByMovieIdAndDate(1L, tomorrow);

        assertEquals(1, list.size());
        assertEquals(tomorrow, list.get(0).getShowDate());
    }

    // ==================== SS-03 按日期查询全部场次 ====================
    @Test
    void testGetByDate() {
        Showtime otherShowtime = new Showtime();
        otherShowtime.setMovieId(2L);
        otherShowtime.setShowDate(today);
        when(showtimeRepository.findByShowDate(today))
                .thenReturn(Arrays.asList(showtime1, showtime2, otherShowtime));

        java.util.List<Showtime> list = showtimeService.getByDate(today);

        assertEquals(3, list.size());
    }

    // ==================== SS-04 按日期查询 - 无场次 ====================
    @Test
    void testGetByDateEmpty() {
        when(showtimeRepository.findByShowDate("2099-12-31")).thenReturn(new ArrayList<>());

        java.util.List<Showtime> list = showtimeService.getByDate("2099-12-31");

        assertTrue(list.isEmpty());
    }

    // ==================== SS-05 未来场次列表 ====================
    @Test
    void testGetFutureShowtimes() {
        when(showtimeRepository.findByShowDateGreaterThanEqual(today))
                .thenReturn(Arrays.asList(showtime1, showtime2));

        java.util.List<Showtime> list = showtimeService.getFutureShowtimes();

        assertEquals(2, list.size());
    }

    // ==================== SS-06 根据ID查询场次（存在） ====================
    @Test
    void testGetByIdExists() {
        when(showtimeRepository.findById(1L)).thenReturn(Optional.of(showtime1));

        Showtime result = showtimeService.getById(1L);

        assertNotNull(result);
        assertEquals("14:30", result.getShowTime());
        assertEquals("1号厅", result.getHallName());
    }

    // ==================== SS-07 根据ID查询场次（不存在） ====================
    @Test
    void testGetByIdNotFound() {
        when(showtimeRepository.findById(999L)).thenReturn(Optional.empty());

        Showtime result = showtimeService.getById(999L);

        assertNull(result);
    }

    // ==================== SS-08 3日滚动场次生成 - 有电影有影厅 ====================
    @Test
    void testMaintainRollingShowtimesWithMoviesAndHalls() {
        Hall hall = new Hall();
        hall.setId(1L);
        hall.setName("1号激光IMAX厅");

        when(movieRepository.findByStatus("showing"))
                .thenReturn(Arrays.asList(movie));
        when(hallRepository.findAll()).thenReturn(Arrays.asList(hall));
        when(showtimeRepository.count()).thenReturn(5L);
        doNothing().when(showtimeRepository).deleteAll();
        when(showtimeRepository.save(any(Showtime.class))).thenAnswer(inv -> inv.getArgument(0));

        int result = showtimeService.maintainRollingShowtimes();

        assertTrue(result >= 5); // 删除了5条旧场次
        verify(showtimeRepository, times(1)).deleteAll();
        verify(showtimeRepository, atLeastOnce()).save(any(Showtime.class));
    }

    // ==================== SS-09 3日滚动场次生成 - 无电影 ====================
    @Test
    void testMaintainRollingShowtimesNoMovies() {
        when(movieRepository.findByStatus("showing")).thenReturn(new ArrayList<>());
        when(showtimeRepository.count()).thenReturn(3L);
        doNothing().when(showtimeRepository).deleteAll();

        int result = showtimeService.maintainRollingShowtimes();

        assertEquals(3, result); // 仅删除旧场次，不生成新场次
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }

    // ==================== SS-10 3日滚动场次生成 - 无影院 ====================
    @Test
    void testMaintainRollingShowtimesNoHalls() {
        when(movieRepository.findByStatus("showing")).thenReturn(Arrays.asList(movie));
        when(hallRepository.findAll()).thenReturn(new ArrayList<>());
        when(showtimeRepository.count()).thenReturn(2L);
        doNothing().when(showtimeRepository).deleteAll();

        int result = showtimeService.maintainRollingShowtimes();

        assertEquals(2, result);
        verify(showtimeRepository, never()).save(any(Showtime.class));
    }
}
