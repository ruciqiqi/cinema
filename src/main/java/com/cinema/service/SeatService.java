package com.cinema.service;

import com.cinema.entity.*;
import com.cinema.repository.*;
import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class SeatService {
    private final SeatRepository seatRepository;
    private final BookingSeatRepository bookingSeatRepository;
    private final BookingRepository bookingRepository;

    public SeatService(SeatRepository seatRepository, BookingSeatRepository bookingSeatRepository,
                       BookingRepository bookingRepository) {
        this.seatRepository = seatRepository;
        this.bookingSeatRepository = bookingSeatRepository;
        this.bookingRepository = bookingRepository;
    }

    public List<Seat> getSeatsByHall(Long hallId) {
        return seatRepository.findByHallId(hallId);
    }

    public Map<String, Object> getSeatMap(Long showtimeId, Long hallId) {
        List<Seat> allSeats = seatRepository.findByHallId(hallId);
        Set<Long> bookedSeatIds = getBookedSeatIds(showtimeId);

        List<Map<String, Object>> seatList = new ArrayList<>();
        for (Seat seat : allSeats) {
            Map<String, Object> s = new HashMap<>();
            s.put("id", seat.getId());
            s.put("rowLabel", seat.getRowLabel());
            s.put("seatNum", seat.getSeatNum());
            s.put("seatType", seat.getSeatType());
            s.put("booked", bookedSeatIds.contains(seat.getId()));
            seatList.add(s);
        }

        Map<String, Object> result = new HashMap<>();
        result.put("seats", seatList);
        result.put("bookedSeatIds", bookedSeatIds);
        return result;
    }

    public Set<Long> getBookedSeatIds(Long showtimeId) {
        Set<Long> booked = new HashSet<>();
        List<Booking> bookings = bookingRepository.findAll().stream()
                .filter(b -> b.getShowtimeId().equals(showtimeId) && "confirmed".equals(b.getStatus()))
                .collect(Collectors.toList());
        for (Booking b : bookings) {
            List<BookingSeat> bsList = bookingSeatRepository.findByBookingId(b.getId());
            for (BookingSeat bs : bsList) {
                booked.add(bs.getSeatId());
            }
        }
        return booked;
    }
}
