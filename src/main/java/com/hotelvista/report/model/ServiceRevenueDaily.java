package com.hotelvista.report.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Entity
@Table(
        name = "service_revenue_daily",
        uniqueConstraints = @UniqueConstraint(columnNames = {"date", "serviceId"})
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRevenueDaily {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private LocalDate date;
    private String serviceId;
    private String serviceName;
    private String serviceCategory;
    private Integer orderCount = 0;
    private Double revenue = 0.0;
}
