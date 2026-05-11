package com.hotelvista.report.dto.event;

import lombok.Data;

import java.time.LocalDate;

@Data
public class OrderEvent {
    private String eventType; // "ORDER_CREATED", "ORDER_CANCELLED"
    private String orderId;
    private Double amount;
    private LocalDate createdAt;
}
