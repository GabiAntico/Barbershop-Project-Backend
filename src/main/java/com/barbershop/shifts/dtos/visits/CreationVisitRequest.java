package com.barbershop.shifts.dtos.visits;

import com.barbershop.shifts.entities.PaymentMethod;
import com.barbershop.shifts.entities.PaymentStatus;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreationVisitRequest {

    @NotNull
    private Long shiftId;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal totalAmount;

    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be 3 letters")
    private String currency;

    @NotNull
    private PaymentStatus paymentStatus;

    private LocalDateTime paidAt;

    private PaymentMethod paymentMethod;

    public void setCurrency(String currency) {
        this.currency = (currency != null) ? currency.trim().toUpperCase() : null;
    }
}
