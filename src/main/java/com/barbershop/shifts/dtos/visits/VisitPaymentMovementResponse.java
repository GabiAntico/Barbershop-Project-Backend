package com.barbershop.shifts.dtos.visits;

import com.barbershop.shifts.entities.PaymentMethod;
import com.barbershop.shifts.entities.VisitPaymentMovementType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitPaymentMovementResponse {
    private Long id;
    private VisitPaymentMovementType type;
    private BigDecimal amount;
    private LocalDateTime occurredAt;
    private PaymentMethod paymentMethod;
    private String notes;
}
