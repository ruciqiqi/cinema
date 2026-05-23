package com.cinema.service;

import com.cinema.entity.Hall;
import com.cinema.entity.Movie;
import com.cinema.entity.Showtime;
import com.cinema.repository.HallRepository;
import com.cinema.repository.MovieRepository;
import com.cinema.repository.ShowtimeRepository;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Random;

@Service
public class ShowtimeService {
    private final ShowtimeRepository showtimeRepository;
    private final MovieRepository movieRepository;
    private final HallRepository hallRepository;

    public ShowtimeService(ShowtimeRepository showtimeRepository,
                           MovieRepository movieRepository,
                           HallRepository hallRepository) {
        this.showtimeRepository = showtimeRepository;
        this.movieRepository = movieRepository;
        this.hallRepository = hallRepository;
    }

    public List<Showtime> getByMovieId(Long movieId) {
        return showtimeRepository.findByMovieIdAndShowDateGreaterThanEqual(
                movieId, LocalDate.now().toString());
    }

    public List<Showtime> getByMovieIdAndDate(Long movieId, String date) {
        return showtimeRepository.findByMovieIdAndShowDate(movieId, date);
    }

    public List<Showtime> getByDate(String date) {
        return showtimeRepository.findByShowDate(date);
    }

    public List<Showtime> getFutureShowtimes() {
        return showtimeRepository.findByShowDateGreaterThanEqual(
                LocalDate.now().toString());
    }

    public Showtime getById(Long id) {
        return showtimeRepository.findById(id).orElse(null);
    }

    /**
     * Maintain rolling 3-day showtime window.
     * Deletes past-date showtimes and generates missing dates for the next 3 days.
     * Called on startup and via scheduled task / admin endpoint.
     */
    @Transactional
    public int maintainRollingShowtimes() {
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String dayAfter = LocalDate.now().plusDays(2).toString();
        String[] dates = {today, tomorrow, dayAfter};

        // Delete past-date showtimes
        int deleted = showtimeRepository.deleteByShowDateLessThan(today);

        // Only generate if there are movies and halls
        List<Movie> showingMovies = movieRepository.findByStatus("showing");
        List<Hall> halls = hallRepository.findAll();
        if (showingMovies.isEmpty() || halls.isEmpty()) return deleted;

        // Hall config for schedule generation
        String[] hallNames = {"1号激光IMAX厅", "2号杜比全景声厅", "3号豪华VIP厅",
                "4号杜比厅", "5号普通厅", "6号IMAX厅", "7号杜比厅", "8号VIP厅", "9号普通厅"};
        String[][] timeSlots = {
                {"10:00", "13:30", "17:00", "20:30"},
                {"11:00", "14:30", "18:30", "21:00"},
                {"10:30", "14:00", "19:00", "21:30"}
        };
        double[][] priceConfigs = {{35.9, 55.9}, {29.9, 49.9}, {59.9, 89.9}};

        Random rnd = new Random();
        int added = 0;
        int hallLimit = Math.min(halls.size(), 3);

        for (Movie movie : showingMovies) {
            for (int h = 0; h < hallLimit; h++) {
                for (String date : dates) {
                    // Check if this movie+hall+date combo already exists
                    List<Showtime> existing = showtimeRepository
                            .findByMovieIdAndHallIdAndShowDate(movie.getId(), halls.get(h).getId(), date);
                    if (!existing.isEmpty()) continue;

                    int start = rnd.nextInt(timeSlots[h % timeSlots.length].length - 2);
                    for (int t = start; t < start + 3 && t < timeSlots[h % timeSlots.length].length; t++) {
                        String slotTime = timeSlots[h % timeSlots.length][t];
                        // Check time conflict with existing showtimes in same hall+date
                        if (hasTimeConflict(halls.get(h).getId(), date, slotTime, movie.getDuration())) continue;

                        Showtime st = new Showtime();
                        st.setMovieId(movie.getId());
                        st.setHallId(halls.get(h).getId());
                        st.setHallName(hallNames[h % hallNames.length]);
                        st.setShowDate(date);
                        st.setShowTime(slotTime);
                        st.setPriceStandard(priceConfigs[h % priceConfigs.length][0]);
                        st.setPriceVip(priceConfigs[h % priceConfigs.length][1]);
                        showtimeRepository.save(st);
                        added++;
                    }
                }
            }
        }

        return deleted + added;
    }

    private boolean hasTimeConflict(Long hallId, String date, String time, Integer duration) {
        int newStart = parseTime(time);
        int newEnd = newStart + (duration != null ? duration : 120);
        List<Showtime> showtimes = showtimeRepository.findByHallIdAndShowDate(hallId, date);
        for (Showtime s : showtimes) {
            Movie m = movieRepository.findById(s.getMovieId()).orElse(null);
            int existStart = parseTime(s.getShowTime());
            int existEnd = existStart + (m != null && m.getDuration() != null ? m.getDuration() : 120);
            if (newStart < existEnd && newEnd > existStart) return true;
        }
        return false;
    }

    private int parseTime(String time) {
        if (time == null) return 0;
        String[] parts = time.split(":");
        try {
            return Integer.parseInt(parts[0]) * 60 + Integer.parseInt(parts[1]);
        } catch (Exception e) {
            return 0;
        }
    }

    @Scheduled(cron = "0 1 0 * * *")
    public void scheduledMaintainShowtimes() {
        maintainRollingShowtimes();
    }
}
