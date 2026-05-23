package com.hotelvista.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomOccupancyReportDto {
    private String period;
    private int totalRooms;
    private int bookedRooms;
    private double occupancyRate;
    private double averageRate;
    private double totalRevenue;
}
