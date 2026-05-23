package com.hotelvista.report.controller;

import com.hotelvista.report.dto.*;
import com.hotelvista.report.dto.event.OrderEvent;
import com.hotelvista.report.dto.event.PaymentEvent;
import com.hotelvista.report.model.RevenueDaily;
import com.hotelvista.report.model.RoomStatistics;
import com.hotelvista.report.service.ReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardStatsDto> getDashboard(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(reportQueryService.getDashboardStats(month, year));
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<RevenueDaily> getDailyRevenue(@RequestParam(required = false) String date) {
        LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(reportQueryService.getDailyRevenue(queryDate));
    }

    @GetMapping("/revenue/by-date-range")
    public ResponseEntity<List<RevenueReportDto>> getRevenueByDateRange(
            @RequestParam String fromDate,
            @RequestParam String toDate) {
        return ResponseEntity.ok(reportQueryService.getRevenueByDateRange(
                LocalDate.parse(fromDate), LocalDate.parse(toDate)));
    }

    @GetMapping("/revenue/daily-current-month")
    public ResponseEntity<List<RevenueReportDto>> getDailyCurrentMonth() {
        return ResponseEntity.ok(reportQueryService.getDailyCurrentMonth());
    }

    @GetMapping("/revenue/weekly-current-month")
    public ResponseEntity<List<RevenueReportDto>> getWeeklyCurrentMonth() {
        return ResponseEntity.ok(reportQueryService.getWeeklyCurrentMonth());
    }

    @GetMapping("/revenue/monthly")
    public ResponseEntity<List<RevenueReportDto>> getMonthlyInYear(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(reportQueryService.getMonthlyInYear(
                year != null ? year : LocalDate.now().getYear()));
    }

    @GetMapping("/revenue/quarterly")
    public ResponseEntity<List<RevenueReportDto>> getQuarterlyInYear(
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(reportQueryService.getQuarterlyInYear(
                year != null ? year : LocalDate.now().getYear()));
    }

    @GetMapping("/revenue/yearly")
    public ResponseEntity<List<RevenueReportDto>> getYearlyRevenue() {
        return ResponseEntity.ok(reportQueryService.getYearlyRevenue());
    }

    @GetMapping("/rooms/top")
    public ResponseEntity<List<RoomStatistics>> getTopRooms(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        return ResponseEntity.ok(reportQueryService.getTopRooms(month, year));
    }

    @GetMapping("/booking")
    public ResponseEntity<List<BookingReportDto>> getBookingReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "MONTHLY") String period) {
        return ResponseEntity.ok(reportQueryService.getBookingReport(
                LocalDate.parse(startDate), LocalDate.parse(endDate), period));
    }

    @GetMapping("/services")
    public ResponseEntity<List<ServiceReportDto>> getServiceReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "monthly") String period) {
        return ResponseEntity.ok(reportQueryService.getServiceReport(
                LocalDate.parse(startDate), LocalDate.parse(endDate), period));
    }

    @GetMapping("/services/chart")
    public ResponseEntity<List<ServiceReportDto>> getServiceReportChart(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "monthly") String period) {
        return getServiceReport(startDate, endDate, period);
    }

    @GetMapping("/room-occupancy")
    public ResponseEntity<List<RoomOccupancyReportDto>> getRoomOccupancyReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "MONTHLY") String period) {
        return ResponseEntity.ok(reportQueryService.getRoomOccupancyReport(
                LocalDate.parse(startDate), LocalDate.parse(endDate), period));
    }

    @GetMapping("/occupancy")
    public ResponseEntity<List<RoomOccupancyReportDto>> getOccupancyReport(
            @RequestParam String startDate,
            @RequestParam String endDate,
            @RequestParam(defaultValue = "MONTHLY") String period) {
        return getRoomOccupancyReport(startDate, endDate, period);
    }

    @GetMapping("/loyalty")
    public ResponseEntity<List<Map<String, Object>>> getLoyaltyReport() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/reviews")
    public ResponseEntity<List<Map<String, Object>>> getReviewReport() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/reviews/ratings/trend")
    public ResponseEntity<List<Map<String, Object>>> getRatingTrend() {
        return ResponseEntity.ok(List.of());
    }

    @GetMapping("/reviews/ratings/category")
    public ResponseEntity<Map<String, Object>> getCategoryRatings() {
        return ResponseEntity.ok(Map.of(
                "location", 0,
                "service", 0,
                "roomQuality", 0,
                "value", 0
        ));
    }

    @GetMapping("/reviews/ratings/sentiment")
    public ResponseEntity<Map<String, Object>> getSentimentStats() {
        return ResponseEntity.ok(Map.of(
                "positive", 0,
                "neutral", 0,
                "negative", 0,
                "positivePercent", 0,
                "neutralPercent", 0,
                "negativePercent", 0
        ));
    }

    @PostMapping("/test-event")
    public ResponseEntity<String> sendTestEvent(@RequestBody OrderEvent event) {
        kafkaTemplate.send("order-events", event);
        return ResponseEntity.ok("Event sent to Kafka topic: order-events");
    }

    @PostMapping("/test-payment")
    public ResponseEntity<String> sendTestPaymentEvent(@RequestBody PaymentEvent event) {
        kafkaTemplate.send("payment-events", event);
        return ResponseEntity.ok("Event sent to Kafka topic: payment-events");
    }
}
