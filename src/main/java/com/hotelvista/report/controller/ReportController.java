package com.hotelvista.report.controller;

import com.hotelvista.report.dto.event.OrderEvent;
import com.hotelvista.report.dto.event.PaymentEvent;
import com.hotelvista.report.model.DashboardSummary;
import com.hotelvista.report.model.RevenueDaily;
import com.hotelvista.report.model.RoomStatistics;
import com.hotelvista.report.service.ReportQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping
@RequiredArgsConstructor
public class ReportController {

    private final ReportQueryService reportQueryService;
    private final KafkaTemplate<String, Object> kafkaTemplate;

    @GetMapping("/dashboard")
    public ResponseEntity<DashboardSummary> getDashboard(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
        
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        
        return ResponseEntity.ok(reportQueryService.getDashboardSummary(m, y));
    }

    @GetMapping("/revenue/daily")
    public ResponseEntity<RevenueDaily> getDailyRevenue(
            @RequestParam(required = false) String date) {
        
        LocalDate queryDate = date != null ? LocalDate.parse(date) : LocalDate.now();
        return ResponseEntity.ok(reportQueryService.getDailyRevenue(queryDate));
    }

    @GetMapping("/rooms/top")
    public ResponseEntity<List<RoomStatistics>> getTopRooms(
            @RequestParam(required = false) Integer month,
            @RequestParam(required = false) Integer year) {
            
        int m = month != null ? month : LocalDate.now().getMonthValue();
        int y = year != null ? year : LocalDate.now().getYear();
        
        return ResponseEntity.ok(reportQueryService.getTopRooms(m, y));
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
