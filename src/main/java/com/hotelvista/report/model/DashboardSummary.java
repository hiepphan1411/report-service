package com.hotelvista.report.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "dashboard_summary")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Integer month;
    private Integer year;
    
    private Integer totalGuestsCheckin = 0;
    private Integer totalBookings = 0;

    private Double cancellationRate = 0.0;

}
