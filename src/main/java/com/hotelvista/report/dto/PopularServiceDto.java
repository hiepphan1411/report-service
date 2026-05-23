package com.hotelvista.report.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PopularServiceDto {
    private String name;
    private int orders;
    private double revenue;
}
