package com.hotelvista.report.consumer;

import com.hotelvista.report.dto.event.BookingEvent;
import com.hotelvista.report.dto.event.OrderEvent;
import com.hotelvista.report.dto.event.PaymentEvent;
import com.hotelvista.report.model.DashboardSummary;
import com.hotelvista.report.model.RevenueDaily;
import com.hotelvista.report.model.RoomStatistics;
import com.hotelvista.report.repository.DashboardSummaryRepository;
import com.hotelvista.report.repository.RevenueDailyRepository;
import com.hotelvista.report.repository.RoomStatisticsRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;

@Service
@RequiredArgsConstructor
@Slf4j
public class ReportEventConsumer {

    private final RevenueDailyRepository revenueDailyRepository;
    private final RoomStatisticsRepository roomStatisticsRepository;
    private final DashboardSummaryRepository dashboardSummaryRepository;

    @KafkaListener(topics = "order-events", groupId = "report-service-group")
    @Transactional
    public void consumeOrderEvent(OrderEvent event) {
        log.info("Received order event: {}", event);
        LocalDate date = event.getCreatedAt() != null ? event.getCreatedAt() : LocalDate.now();

        RevenueDaily revenue = revenueDailyRepository.findByDate(date)
                .orElse(new RevenueDaily(date));

        if ("ORDER_CREATED".equalsIgnoreCase(event.getEventType())) {
            revenue.setTotalOrders(revenue.getTotalOrders() + 1);
        } else if ("ORDER_CANCELLED".equalsIgnoreCase(event.getEventType())) {
            revenue.setCancelledOrders(revenue.getCancelledOrders() + 1);
        }

        revenueDailyRepository.save(revenue);
    }

    @KafkaListener(topics = "payment-events", groupId = "report-service-group")
    @Transactional
    public void consumePaymentEvent(PaymentEvent event) {
        log.info("Received payment event: {}", event);
        LocalDate date = LocalDate.now();

        RevenueDaily revenue = revenueDailyRepository.findByDate(date)
                .orElse(new RevenueDaily(date));

        if ("PAYMENT_SUCCESS".equalsIgnoreCase(event.getEventType())) {
            revenue.setSuccessfulPayments(revenue.getSuccessfulPayments() + 1);
            revenue.setTotalRevenue(revenue.getTotalRevenue() + event.getAmount());
        }

        revenueDailyRepository.save(revenue);
    }

    @KafkaListener(topics = "booking-events", groupId = "report-service-group")
    @Transactional
    public void consumeBookingEvent(BookingEvent event) {
        log.info("Received booking event: {}", event);
        
        Integer month = event.getMonth() != null ? event.getMonth() : LocalDate.now().getMonthValue();
        Integer year = event.getYear() != null ? event.getYear() : LocalDate.now().getYear();

        // Update Room Statistics
        if (event.getRoomId() != null) {
            RoomStatistics roomStats = roomStatisticsRepository.findByRoomIdAndMonthAndYear(event.getRoomId(), month, year)
                    .orElse(new RoomStatistics(null, event.getRoomId(), event.getRoomNumber(), 0, 0.0, month, year));
            
            if ("CHECK_OUT_SUCCESS".equalsIgnoreCase(event.getEventType())) {
                roomStats.setBookingCount(roomStats.getBookingCount() + 1);
                if (event.getTotalAmount() != null) {
                    roomStats.setRevenue(roomStats.getRevenue() + event.getTotalAmount());
                }
            }
            roomStatisticsRepository.save(roomStats);
        }

        // Update Dashboard Summary
        DashboardSummary dashboard = dashboardSummaryRepository.findByMonthAndYear(month, year)
                .orElse(new DashboardSummary(null, month, year, 0, 0, 0.0));

        if ("CHECK_IN_SUCCESS".equalsIgnoreCase(event.getEventType())) {
            dashboard.setTotalGuestsCheckin(dashboard.getTotalGuestsCheckin() + 1);
        } else if ("BOOKING_CREATED".equalsIgnoreCase(event.getEventType())) {
            dashboard.setTotalBookings(dashboard.getTotalBookings() + 1);
        } else if ("BOOKING_CANCELLED".equalsIgnoreCase(event.getEventType())) {
            int currentBookings = dashboard.getTotalBookings();
            if (currentBookings > 0) {
                double currentCancelled = (dashboard.getCancellationRate() / 100.0) * currentBookings;
                currentCancelled += 1;
                dashboard.setCancellationRate((currentCancelled / currentBookings) * 100.0);
            }
        }
        
        dashboardSummaryRepository.save(dashboard);
    }
}
