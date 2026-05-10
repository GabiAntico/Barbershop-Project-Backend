package com.barbershop.shifts.dtos.settings;

import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppSettingsResponse {
    private BigDecimal defaultEstimatedAmount;
}
