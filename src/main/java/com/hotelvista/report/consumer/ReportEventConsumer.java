package com.hotelvista.report.consumer;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.hotelvista.report.dto.event.BookingEvent;
import com.hotelvista.report.dto.event.OrderEvent;
import com.hotelvista.report.dto.event.PaymentEvent;
import com.hotelvista.report.model.DashboardSummary;
import com.hotelvista.report.model.RevenueDaily;
import com.hotelvista.report.model.RoomStatistics;
import com.hotelvista.report.model.ServiceRevenueDaily;
import com.hotelvista.report.repository.DashboardSummaryRepository;
import com.hotelvista.report.repository.RevenueDailyRepository;
import com.hotelvista.report.repository.RoomStatisticsRepository;
import com.hotelvista.report.repository.ServiceRevenueDailyRepository;
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
    private final ServiceRevenueDailyRepository serviceRevenueDailyRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "order-events", groupId = "report-service-group")
    @Transactional
    public void consumeOrderEvent(String message) throws JsonProcessingException {
        OrderEvent event = objectMapper.readValue(message, OrderEvent.class);

        log.info("Received order event: {}", event);

        LocalDate date = event.getCreatedAt() != null ? event.getCreatedAt() : LocalDate.now();

        RevenueDaily revenue = revenueDailyRepository.findByDate(date)
                .orElse(new RevenueDaily(date));

        if ("ORDER_CREATED".equalsIgnoreCase(event.getEventType())) {
            revenue.setTotalOrders(totalOrders(revenue) + 1);
            revenue.setServiceRevenue(serviceRevenue(revenue) + safe(event.getAmount()));
            updateServiceRevenue(event, date, 1, safe(event.getAmount()));
        } else if ("ORDER_CANCELLED".equalsIgnoreCase(event.getEventType())) {
            revenue.setCancelledOrders(cancelledOrders(revenue) + 1);
            revenue.setServiceRevenue(Math.max(serviceRevenue(revenue) - safe(event.getAmount()), 0));
            updateServiceRevenue(event, date, -1, -safe(event.getAmount()));
        }

        revenue.setTotalRevenue(roomRevenue(revenue) + serviceRevenue(revenue));
        revenueDailyRepository.save(revenue);
    }

    private void updateServiceRevenue(OrderEvent event, LocalDate date, int orderDelta, double revenueDelta) {
        String serviceId = event.getServiceId();
        if (serviceId == null || serviceId.isBlank()) {
            serviceId = event.getServiceName();
        }
        if (serviceId == null || serviceId.isBlank()) {
            serviceId = "unknown-service";
        }
        final String serviceKey = serviceId;

        ServiceRevenueDaily serviceRevenue = serviceRevenueDailyRepository.findByDateAndServiceId(date, serviceKey)
                .orElseGet(() -> {
                    ServiceRevenueDaily created = new ServiceRevenueDaily();
                    created.setDate(date);
                    created.setServiceId(serviceKey);
                    created.setServiceName(event.getServiceName() != null && !event.getServiceName().isBlank()
                            ? event.getServiceName()
                            : serviceKey);
                    created.setServiceCategory(event.getServiceCategory());
                    return created;
                });

        if (event.getServiceName() != null && !event.getServiceName().isBlank()) {
            serviceRevenue.setServiceName(event.getServiceName());
        }
        if (event.getServiceCategory() != null && !event.getServiceCategory().isBlank()) {
            serviceRevenue.setServiceCategory(event.getServiceCategory());
        }

        serviceRevenue.setOrderCount(Math.max(totalOrders(serviceRevenue) + orderDelta, 0));
        serviceRevenue.setRevenue(Math.max(serviceRevenue(serviceRevenue) + revenueDelta, 0));
        serviceRevenueDailyRepository.save(serviceRevenue);
    }

    @KafkaListener(topics = "payment-events", groupId = "report-service-group")
    @Transactional
    public void consumePaymentEvent(String message) throws JsonProcessingException {
        PaymentEvent event = objectMapper.readValue(message, PaymentEvent.class);

        log.info("Received payment event: {}", event);

        LocalDate date = LocalDate.now();

        RevenueDaily revenue = revenueDailyRepository.findByDate(date)
                .orElse(new RevenueDaily(date));

        if ("PAYMENT_SUCCESS".equalsIgnoreCase(event.getEventType())) {
            revenue.setSuccessfulPayments(successfulPayments(revenue) + 1);
        }

        revenueDailyRepository.save(revenue);
    }

    @KafkaListener(topics = "booking-events", groupId = "report-service-group")
    @Transactional
    public void consumeBookingEvent(String message) throws JsonProcessingException {
        BookingEvent event = objectMapper.readValue(message, BookingEvent.class);

        log.info("Received booking event: {}", event);

        LocalDate eventDate = event.getEventDate() != null ? event.getEventDate() : LocalDate.now();
        Integer month = event.getMonth() != null ? event.getMonth() : eventDate.getMonthValue();
        Integer year = event.getYear() != null ? event.getYear() : eventDate.getYear();

        if (event.getRoomId() != null) {
            RoomStatistics roomStats = roomStatisticsRepository.findByRoomIdAndMonthAndYear(
                            event.getRoomId(),
                            month,
                            year
                    )
                    .orElse(new RoomStatistics(
                            null,
                            event.getRoomId(),
                            event.getRoomNumber(),
                            0,
                            0.0,
                            month,
                            year
                    ));

            if ("CHECK_OUT_SUCCESS".equalsIgnoreCase(event.getEventType())) {
                roomStats.setBookingCount(
                        (roomStats.getBookingCount() != null ? roomStats.getBookingCount() : 0) + 1
                );

                if (event.getTotalAmount() != null) {
                    roomStats.setRevenue(
                            (roomStats.getRevenue() != null ? roomStats.getRevenue() : 0.0)
                                    + event.getTotalAmount()
                    );
                }
            }

            roomStatisticsRepository.save(roomStats);
        }

        DashboardSummary dashboard = dashboardSummaryRepository.findByMonthAndYear(month, year)
                .orElse(new DashboardSummary(null, month, year, 0, 0, 0.0));

        if ("CHECK_IN_SUCCESS".equalsIgnoreCase(event.getEventType())) {
            dashboard.setTotalGuestsCheckin(
                    (dashboard.getTotalGuestsCheckin() != null ? dashboard.getTotalGuestsCheckin() : 0) + 1
            );
        } else if ("BOOKING_CREATED".equalsIgnoreCase(event.getEventType())) {
            dashboard.setTotalBookings(
                    (dashboard.getTotalBookings() != null ? dashboard.getTotalBookings() : 0) + 1
            );
        } else if ("BOOKING_CANCELLED".equalsIgnoreCase(event.getEventType())) {
            int currentBookings = dashboard.getTotalBookings() != null ? dashboard.getTotalBookings() : 0;

            if (currentBookings > 0) {
                double currentCancelled = (dashboard.getCancellationRate() / 100.0) * currentBookings;
                currentCancelled += 1;
                dashboard.setCancellationRate((currentCancelled / currentBookings) * 100.0);
            }
        }

        dashboardSummaryRepository.save(dashboard);

        if ("CHECK_OUT_SUCCESS".equalsIgnoreCase(event.getEventType())) {
            RevenueDaily revenue = revenueDailyRepository.findByDate(eventDate)
                    .orElse(new RevenueDaily(eventDate));

            revenue.setBookingCount(bookingCount(revenue) + 1);
            revenue.setRoomRevenue(roomRevenue(revenue) + safe(event.getTotalAmount()));
            revenue.setTotalRevenue(roomRevenue(revenue) + serviceRevenue(revenue));

            revenueDailyRepository.save(revenue);
        }
    }

    private double safe(Double value) {
        return value != null ? value : 0.0;
    }

    private int totalOrders(RevenueDaily revenue) {
        return revenue.getTotalOrders() != null ? revenue.getTotalOrders() : 0;
    }

    private int cancelledOrders(RevenueDaily revenue) {
        return revenue.getCancelledOrders() != null ? revenue.getCancelledOrders() : 0;
    }

    private int successfulPayments(RevenueDaily revenue) {
        return revenue.getSuccessfulPayments() != null ? revenue.getSuccessfulPayments() : 0;
    }

    private int bookingCount(RevenueDaily revenue) {
        return revenue.getBookingCount() != null ? revenue.getBookingCount() : 0;
    }

    private double roomRevenue(RevenueDaily revenue) {
        return safe(revenue.getRoomRevenue());
    }

    private double serviceRevenue(RevenueDaily revenue) {
        return safe(revenue.getServiceRevenue());
    }

    private int totalOrders(ServiceRevenueDaily revenue) {
        return revenue.getOrderCount() != null ? revenue.getOrderCount() : 0;
    }

    private double serviceRevenue(ServiceRevenueDaily revenue) {
        return safe(revenue.getRevenue());
    }
}
