package com.hotelvista.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ServiceRevenueItemDto {
    private String serviceId;
    private String serviceName;
    private String serviceCategory;
    private int orders;
    private double revenue;
}
