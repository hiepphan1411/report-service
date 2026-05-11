package com.hotelvista.report.dto.event;

import lombok.Data;

@Data
public class PaymentEvent {
    private String eventType; // "PAYMENT_SUCCESS"
    private String paymentId;
    private String orderId;
    private Double amount;
}
