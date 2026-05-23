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
import java.util.*;

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

    private static final int BUFFER_MINUTES = 15;

    /**
     * Maintain rolling 3-day showtime window.
     * Clears all existing showtimes and regenerates conflict-free schedule.
     * Called on startup and via scheduled task / admin endpoint.
     */
    @Transactional
    public int maintainRollingShowtimes() {
        String today = LocalDate.now().toString();
        String tomorrow = LocalDate.now().plusDays(1).toString();
        String dayAfter = LocalDate.now().plusDays(2).toString();
        String[] dates = {today, tomorrow, dayAfter};

        // Clear all existing showtimes to regenerate cleanly
        int deleted = (int) showtimeRepository.count();
        showtimeRepository.deleteAll();
        showtimeRepository.flush();

        // Only generate if there are movies and halls
        List<Movie> showingMovies = new ArrayList<>(movieRepository.findByStatus("showing"));
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

        for (String date : dates) {
            for (int h = 0; h < hallLimit; h++) {
                Long hallId = halls.get(h).getId();
                String hallName = hallNames[h % hallNames.length];
                double priceStd = priceConfigs[h % priceConfigs.length][0];
                double priceVip = priceConfigs[h % priceConfigs.length][1];
                String[] slots = timeSlots[h % timeSlots.length];

                List<int[]> occupied = new ArrayList<>();
                List<Movie> moviesHere = new ArrayList<>(showingMovies);
                Collections.shuffle(moviesHere, rnd);

                // Round 1: each movie tries 1 slot
                Set<Long> scheduled = new HashSet<>();
                for (Movie movie : moviesHere) {
                    if (tryAssign(movie, slots, occupied, hallId, hallName, date, priceStd, priceVip, rnd)) {
                        scheduled.add(movie.getId());
                    }
                }

                // Round 2: unscheduled movies first, then any movie for remaining slots
                for (Movie movie : moviesHere) {
                    if (!scheduled.contains(movie.getId())) {
                        tryAssign(movie, slots, occupied, hallId, hallName, date, priceStd, priceVip, rnd);
                    }
                }
                for (Movie movie : moviesHere) {
                    tryAssign(movie, slots, occupied, hallId, hallName, date, priceStd, priceVip, rnd);
                }
            }
        }

        showtimeRepository.flush();
        return deleted + added;
    }

    private boolean tryAssign(Movie movie, String[] slots, List<int[]> occupied,
                              Long hallId, String hallName, String date,
                              double priceStd, double priceVip, Random rnd) {
        int movieDuration = movie.getDuration() != null ? movie.getDuration() : 120;
        List<Integer> order = new ArrayList<>();
        for (int i = 0; i < slots.length; i++) order.add(i);
        Collections.shuffle(order, rnd);
        for (int idx : order) {
            int newStart = parseTime(slots[idx]);
            int newEnd = newStart + movieDuration + BUFFER_MINUTES;
            boolean conflict = false;
            for (int[] occ : occupied) {
                if (newStart < occ[1] && newEnd > occ[0]) { conflict = true; break; }
            }
            if (conflict) continue;
            Showtime st = new Showtime();
            st.setMovieId(movie.getId());
            st.setHallId(hallId);
            st.setHallName(hallName);
            st.setShowDate(date);
            st.setShowTime(slots[idx]);
            st.setPriceStandard(priceStd);
            st.setPriceVip(priceVip);
            showtimeRepository.save(st);
            occupied.add(new int[]{newStart, newEnd});
            return true;
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
