package com.hotelvista.report.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(name = "revenue_daily")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueDaily {

    @Id
    private LocalDate date;

    private Integer totalOrders = 0;
    private Double totalRevenue = 0.0;
    private Double roomRevenue = 0.0;
    private Double serviceRevenue = 0.0;
    private Integer bookingCount = 0;
    private Integer successfulPayments = 0;
    private Integer cancelledOrders = 0;

    public RevenueDaily(LocalDate date) {
        this.date = date;
    }
}
