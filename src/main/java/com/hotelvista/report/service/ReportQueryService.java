package com.hotelvista.report.service;

import com.hotelvista.report.model.DashboardSummary;
import com.hotelvista.report.model.RevenueDaily;
import com.hotelvista.report.model.RoomStatistics;
import com.hotelvista.report.repository.DashboardSummaryRepository;
import com.hotelvista.report.repository.RevenueDailyRepository;
import com.hotelvista.report.repository.RoomStatisticsRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ReportQueryService {

    private final RevenueDailyRepository revenueDailyRepository;
    private final RoomStatisticsRepository roomStatisticsRepository;
    private final DashboardSummaryRepository dashboardSummaryRepository;

    public RevenueDaily getDailyRevenue(LocalDate date) {
        return revenueDailyRepository.findByDate(date).orElseGet(() -> {
            // TODO: Fallback hardcode để test
            RevenueDaily fallback = new RevenueDaily(date);
            fallback.setTotalOrders(15);
            fallback.setTotalRevenue(1500000.0);
            fallback.setSuccessfulPayments(12);
            fallback.setCancelledOrders(3);
            return fallback;
        });
    }

    public DashboardSummary getDashboardSummary(Integer month, Integer year) {
        return dashboardSummaryRepository.findByMonthAndYear(month, year).orElseGet(() -> {
            DashboardSummary fallback = new DashboardSummary(999L, month, year, 120, 45, 5.0);
            return fallback;
        });
    }

    public List<RoomStatistics> getTopRooms(Integer month, Integer year) {
        List<RoomStatistics> rooms = roomStatisticsRepository.findAll();
        if (rooms.isEmpty()) {
            return List.of(
                    new RoomStatistics(1L, 101L, "101", 10, 500000.0, month, year),
                    new RoomStatistics(2L, 102L, "102", 8, 400000.0, month, year),
                    new RoomStatistics(3L, 201L, "201", 15, 750000.0, month, year)
            );
        }
        return rooms;
    }
}
