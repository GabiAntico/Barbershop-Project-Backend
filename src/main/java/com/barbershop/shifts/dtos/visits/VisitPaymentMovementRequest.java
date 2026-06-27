package com.barbershop.shifts.dtos.visits;

import com.barbershop.shifts.entities.PaymentMethod;
import com.barbershop.shifts.entities.VisitPaymentMovementType;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitPaymentMovementRequest {

    @NotNull
    private VisitPaymentMovementType type;

    @NotNull
    @DecimalMin(value = "0.0", inclusive = false)
    private BigDecimal amount;

    @NotNull
    private LocalDateTime occurredAt;

    private PaymentMethod paymentMethod;

    private String notes;

    public void setNotes(String notes) {
        this.notes = (notes != null) ? notes.trim() : null;
    }
}
