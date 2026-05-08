package com.cinema.entity;

import javax.persistence.*;

@Entity
@Table(name = "refund_rules")
public class RefundRule {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "hours_before_show")
    private Integer hoursBeforeShow;

    @Column(name = "refund_rate")
    private Double refundRate; // e.g. 1.0 = 100%, 0.8 = 80%

    @Column(length = 100)
    private String description;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public Integer getHoursBeforeShow() { return hoursBeforeShow; }
    public void setHoursBeforeShow(Integer hoursBeforeShow) { this.hoursBeforeShow = hoursBeforeShow; }
    public Double getRefundRate() { return refundRate; }
    public void setRefundRate(Double refundRate) { this.refundRate = refundRate; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
}
