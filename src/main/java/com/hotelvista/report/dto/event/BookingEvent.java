package com.hotelvista.report.dto.event;

import lombok.Data;

@Data
public class BookingEvent {
    private String eventType; // "CHECK_IN_SUCCESS", "BOOKING_CANCELLED", "CHECK_OUT_SUCCESS"
    private String bookingId;
    private Long roomId;
    private String roomNumber;
    private Double totalAmount;
    private Integer month;
    private Integer year;
}
