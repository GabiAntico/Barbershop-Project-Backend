package com.barbershop.shifts.dtos.visits;

import com.barbershop.shifts.entities.PaymentMethod;
import com.barbershop.shifts.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreationVisitRequest {
    private Long shiftId;
    private BigDecimal totalAmount;
    private String currency;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private PaymentMethod paymentMethod;
}
