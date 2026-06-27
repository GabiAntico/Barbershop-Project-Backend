package com.barbershop.shifts.dtos.settings;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class AppSettingsRequest {
    @DecimalMin(value = "0.00", message = "The default estimated amount can't be negative")
    private BigDecimal defaultEstimatedAmount;

    @Pattern(regexp = "^[A-Z]{3}$", message = "Currency must be 3 uppercase letters")
    private String defaultCurrency;
}
