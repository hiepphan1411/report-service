package com.hotelvista.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RevenueReportDto {
    private String label;
    private Integer year;
    private Integer month;
    private Integer day;
    private Integer week;
    private Integer quarter;
    private Integer bookingCount;
    private Double roomRevenue;
    private Double serviceRevenue;
    private Double totalRevenue;
}
