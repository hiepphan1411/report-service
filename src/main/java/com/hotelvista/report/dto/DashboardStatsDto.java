package com.hotelvista.report.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class DashboardStatsDto {
    private double totalRevenue;
    private double revenueChange;
    private int totalBookings;
    private double bookingsChange;
    private double occupancyRate;
    private double occupancyChange;
    private int totalGuests;
    private double guestsChange;
    private int availableRooms;
    private int bookedRooms;
    private int maintenanceRooms;
    private int cleaningRooms;
    private double avgRating;
    private int totalReviews;
    private int pendingCheckIns;
    private int pendingCheckOuts;
    private List<DashboardRevenueDto> revenueData = new ArrayList<>();
    private List<NameCountDto> roomTypeData = new ArrayList<>();
    private List<StatusCountDto> bookingStatusData = new ArrayList<>();
    private List<DailyOccupancyDto> dailyOccupancy = new ArrayList<>();
    private List<PopularServiceDto> popularServices = new ArrayList<>();
}
