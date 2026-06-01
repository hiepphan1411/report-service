package com.hotelvista.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceReportDto {
    private String date;
    private double foodBeverage;
    private double laundry;
    private double spa;
    private double transport;
    private double tour;
    private double others;
    private int totalOrders;
    private double avgOrderValue;
    private java.util.List<ServiceRevenueItemDto> services = new java.util.ArrayList<>();
}
