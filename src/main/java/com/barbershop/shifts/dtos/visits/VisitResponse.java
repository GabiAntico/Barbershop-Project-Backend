package com.barbershop.shifts.dtos.visits;

import com.barbershop.shifts.dtos.clients.ClientResponse;
import com.barbershop.shifts.entities.PaymentMethod;
import com.barbershop.shifts.entities.PaymentStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class VisitResponse {
    private Long id;
    private Long shiftId;
    private ClientResponse client;
    private Long attendedByUserId;
    private String attendedByName;
    private String attendedByEmail;
    private BigDecimal totalAmount;
    private String currency;
    private PaymentStatus paymentStatus;
    private LocalDateTime paidAt;
    private PaymentMethod paymentMethod;
    private BigDecimal grossPaidAmount;
    private BigDecimal refundedAmount;
    private BigDecimal bonifiedAmount;
    private BigDecimal netPaidAmount;
    private BigDecimal coveredAmount;
    private BigDecimal pendingAmount;
    private List<VisitPaymentMovementResponse> paymentMovements;
}
