package com.cinema.service;

import com.cinema.entity.*;
import com.cinema.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeatServiceTests {

    @Mock private SeatRepository seatRepository;
    @Mock private BookingSeatRepository bookingSeatRepository;
    @Mock private BookingRepository bookingRepository;

    @InjectMocks
    private SeatService seatService;

    private Seat seat1;
    private Seat seat2;
    private Seat seatVip;

    @BeforeEach
    void setUp() {
        seat1 = new Seat();
        seat1.setId(101L);
        seat1.setHallId(1L);
        seat1.setRowLabel("A");
        seat1.setSeatNum(1);
        seat1.setSeatType("standard");

        seat2 = new Seat();
        seat2.setId(102L);
        seat2.setHallId(1L);
        seat2.setRowLabel("A");
        seat2.setSeatNum(2);
        seat2.setSeatType("standard");

        seatVip = new Seat();
        seatVip.setId(201L);
        seatVip.setHallId(1L);
        seatVip.setRowLabel("V");
        seatVip.setSeatNum(1);
        seatVip.setSeatType("vip");
    }

    // ==================== SEA-01 获取影厅所有座位 ====================
    @Test
    void testGetSeatsByHall() {
        when(seatRepository.findByHallId(1L))
                .thenReturn(Arrays.asList(seat1, seat2, seatVip));

        List<Seat> seats = seatService.getSeatsByHall(1L);

        assertEquals(3, seats.size());
        verify(seatRepository, times(1)).findByHallId(1L);
    }

    // ==================== SEA-02 获取座位图 - 标记已预订 ====================
    @Test
    void testGetSeatMapMarksBooked() {
        when(seatRepository.findByHallId(1L))
                .thenReturn(Arrays.asList(seat1, seat2, seatVip));

        // seat1 已被预订
        Booking confirmed = new Booking();
        confirmed.setId(1L);
        confirmed.setShowtimeId(1L);
        confirmed.setStatus("confirmed");
        when(bookingRepository.findConfirmedByShowtimeId(1L))
                .thenReturn(Arrays.asList(confirmed));

        BookingSeat bs = new BookingSeat();
        bs.setBookingId(1L);
        bs.setSeatId(101L);
        bs.setSeatLabel("A排1座");
        when(bookingSeatRepository.findByBookingId(1L))
                .thenReturn(Arrays.asList(bs));

        Map<String, Object> result = seatService.getSeatMap(1L, 1L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seatList = (List<Map<String, Object>>) result.get("seats");
        @SuppressWarnings("unchecked")
        Set<Long> bookedIds = (Set<Long>) result.get("bookedSeatIds");

        assertEquals(3, seatList.size());
        assertTrue(bookedIds.contains(101L));
        assertFalse(bookedIds.contains(102L));
    }

    // ==================== SEA-03 获取已预订座位ID集合 - 空场次 ====================
    @Test
    void testGetBookedSeatIdsEmpty() {
        when(bookingRepository.findConfirmedByShowtimeId(999L))
                .thenReturn(new ArrayList<>());

        Set<Long> booked = seatService.getBookedSeatIds(999L);

        assertTrue(booked.isEmpty());
    }

    // ==================== SEA-04 获取已预订座位ID集合 - 有预订 ====================
    @Test
    void testGetBookedSeatIdsWithBookings() {
        Booking confirmed = new Booking();
        confirmed.setId(1L);
        confirmed.setShowtimeId(1L);
        confirmed.setStatus("confirmed");
        when(bookingRepository.findConfirmedByShowtimeId(1L))
                .thenReturn(Arrays.asList(confirmed));

        BookingSeat bs1 = new BookingSeat();
        bs1.setBookingId(1L);
        bs1.setSeatId(101L);
        BookingSeat bs2 = new BookingSeat();
        bs2.setBookingId(1L);
        bs2.setSeatId(102L);
        when(bookingSeatRepository.findByBookingId(1L))
                .thenReturn(Arrays.asList(bs1, bs2));

        Set<Long> booked = seatService.getBookedSeatIds(1L);

        assertEquals(2, booked.size());
        assertTrue(booked.contains(101L));
        assertTrue(booked.contains(102L));
    }

    // ==================== SEA-05 已取消订单不影响座位占用 ====================
    @Test
    void testGetBookedSeatIdsIgnoresCancelled() {
        Booking cancelled = new Booking();
        cancelled.setId(2L);
        cancelled.setStatus("cancelled");
        when(bookingRepository.findConfirmedByShowtimeId(1L))
                .thenReturn(new ArrayList<>());

        Set<Long> booked = seatService.getBookedSeatIds(1L);

        assertTrue(booked.isEmpty());
    }

    // ==================== SEA-06 VIP座位与普通座位正确返回 ====================
    @Test
    void testGetSeatMapVipAndStandard() {
        when(seatRepository.findByHallId(1L))
                .thenReturn(Arrays.asList(seat1, seatVip));
        when(bookingRepository.findConfirmedByShowtimeId(1L))
                .thenReturn(new ArrayList<>());

        Map<String, Object> result = seatService.getSeatMap(1L, 1L);

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> seatList = (List<Map<String, Object>>) result.get("seats");

        assertEquals(2, seatList.size());
        Map<String, Object> vipSeat = seatList.stream()
                .filter(s -> "vip".equals(s.get("seatType")))
                .findFirst().orElse(null);
        assertNotNull(vipSeat);
        assertEquals("V", vipSeat.get("rowLabel"));
    }
}
