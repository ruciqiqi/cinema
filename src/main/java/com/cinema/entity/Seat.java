package com.cinema.entity;

import javax.persistence.*;

@Entity
@Table(name = "seats")
public class Seat {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hall_id")
    private Long hallId;

    @Column(name = "row_label", length = 5)
    private String rowLabel;

    @Column(name = "seat_num")
    private Integer seatNum;

    @Column(name = "seat_type", length = 20)
    private String seatType = "standard";

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Long getHallId() { return hallId; }
    public void setHallId(Long hallId) { this.hallId = hallId; }
    public String getRowLabel() { return rowLabel; }
    public void setRowLabel(String rowLabel) { this.rowLabel = rowLabel; }
    public Integer getSeatNum() { return seatNum; }
    public void setSeatNum(Integer seatNum) { this.seatNum = seatNum; }
    public String getSeatType() { return seatType; }
    public void setSeatType(String seatType) { this.seatType = seatType; }
}
