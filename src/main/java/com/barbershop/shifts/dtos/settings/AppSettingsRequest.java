package com.barbershop.shifts.dtos.settings;

import jakarta.validation.constraints.DecimalMin;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppSettingsRequest {
    @DecimalMin(value = "0.00", message = "The default estimated amount can't be negative")
    private BigDecimal defaultEstimatedAmount;
}
