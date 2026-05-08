package com.cinema.entity;

import javax.persistence.*;

@Entity
@Table(name = "halls")
public class Hall {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String name;

    @Column(name = "cinema_id")
    private Long cinemaId;

    @Column(name = "hall_type", length = 30)
    private String hallType = "STANDARD"; // STANDARD, IMAX, DOLBY, COUPLE

    @Column(name = "hall_rows")
    private Integer rows;

    @Column(name = "seat_cols")
    private Integer seatCols;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public Long getCinemaId() { return cinemaId; }
    public void setCinemaId(Long cinemaId) { this.cinemaId = cinemaId; }
    public String getHallType() { return hallType; }
    public void setHallType(String hallType) { this.hallType = hallType; }
    public Integer getRows() { return rows; }
    public void setRows(Integer rows) { this.rows = rows; }
    public Integer getSeatCols() { return seatCols; }
    public void setSeatCols(Integer seatCols) { this.seatCols = seatCols; }
}
