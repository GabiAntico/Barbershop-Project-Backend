package com.barbershop.shifts.dtos.visits;

import com.barbershop.shifts.entities.PaymentMethod;
import com.barbershop.shifts.entities.PaymentStatus;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateVisitRequest {
    @NotNull
    @DecimalMin(value = "0.0", inclusive = true)
    private BigDecimal totalAmount;

    @Pattern(regexp = "^[A-Za-z]{3}$", message = "Currency must be 3 letters")
    private String currency;

    private PaymentStatus paymentStatus;

    private LocalDateTime paidAt;

    private PaymentMethod paymentMethod;

    private List<@Valid VisitPaymentMovementRequest> paymentMovements = new ArrayList<>();

    public void setCurrency(String currency) {
        this.currency = (currency != null) ? currency.trim().toUpperCase() : null;
    }
}
