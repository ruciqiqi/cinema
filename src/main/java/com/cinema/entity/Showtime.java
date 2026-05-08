package com.cinema.entity;

import javax.persistence.*;

@Entity
@Table(name = "showtimes")
public class Showtime {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "movie_id")
    private Long movieId;

    @Column(name = "hall_id")
    private Long hallId;

    @Column(length = 20)
    private String showDate;

    @Column(length = 20)
    private String showTime;

    @Column(length = 50)
    private String hallName;

    private Double priceStandard = 39.9;
    private Double priceVip = 59.9;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getMovieId() { return movieId; }
    public void setMovieId(Long movieId) { this.movieId = movieId; }
    public Long getHallId() { return hallId; }
    public void setHallId(Long hallId) { this.hallId = hallId; }
    public String getShowDate() { return showDate; }
    public void setShowDate(String showDate) { this.showDate = showDate; }
    public String getShowTime() { return showTime; }
    public void setShowTime(String showTime) { this.showTime = showTime; }
    public String getHallName() { return hallName; }
    public void setHallName(String hallName) { this.hallName = hallName; }
    public Double getPriceStandard() { return priceStandard; }
    public void setPriceStandard(Double priceStandard) { this.priceStandard = priceStandard; }
    public Double getPriceVip() { return priceVip; }
    public void setPriceVip(Double priceVip) { this.priceVip = priceVip; }
}
