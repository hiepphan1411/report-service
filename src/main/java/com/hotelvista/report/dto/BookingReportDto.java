package com.hotelvista.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BookingReportDto {
    private String period;
    private int totalBookings;
    private int completedBookings;
    private int cancelledBookings;
    private double cancellationRate;
    private double averageBookingValue;
    private double totalRevenue;
}
