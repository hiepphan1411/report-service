package com.hotelvista.report.model;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "room_statistics")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RoomStatistics {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long roomId;
    private String roomNumber;
    
    private Integer bookingCount = 0;
    private Double revenue = 0.0;
    
    private Integer month;
    private Integer year;

}
