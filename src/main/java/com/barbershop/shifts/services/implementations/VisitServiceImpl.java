package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.clients.ClientResponse;
import com.barbershop.shifts.dtos.visits.CreationVisitRequest;
import com.barbershop.shifts.dtos.visits.UpdateVisitRequest;
import com.barbershop.shifts.dtos.visits.VisitPaymentMovementRequest;
import com.barbershop.shifts.dtos.visits.VisitPaymentMovementResponse;
import com.barbershop.shifts.dtos.visits.VisitResponse;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.PaymentMethod;
import com.barbershop.shifts.entities.PaymentStatus;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.entities.Visit;
import com.barbershop.shifts.entities.VisitPaymentMovement;
import com.barbershop.shifts.entities.VisitPaymentMovementType;
import com.barbershop.shifts.repositories.VisitRepositoryJpa;
import com.barbershop.shifts.services.AppSettingsService;
import com.barbershop.shifts.services.CurrentUserService;
import com.barbershop.shifts.services.ShiftService;
import com.barbershop.shifts.services.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

@Service
@Transactional(readOnly = true)
public class VisitServiceImpl implements VisitService {

    private static final BigDecimal ZERO = BigDecimal.ZERO.setScale(2, RoundingMode.HALF_UP);

    @Autowired
    private VisitRepositoryJpa visitRepository;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private CurrentUserService currentUserService;

    @Autowired
    private AppSettingsService appSettingsService;

    @Override
    public List<VisitResponse> getAllVisits() {
        Branch branch = currentUserService.getCurrentBranch();
        List<Visit> visits = visitRepository.findAllByShiftBranch(branch);

        List<VisitResponse> visitResponses = new ArrayList<>();
        for (Visit visit : visits) {
            visitResponses.add(convertEntityIntoDto(visit));
        }

        return visitResponses;
    }

    @Override
    public VisitResponse getVisitById(Long id) {
        Visit visit = getRawVisitById(id);

        return convertEntityIntoDto(visit);
    }

    @Override
    public Visit getRawVisitById(Long id) {
        if (id <= 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Branch branch = currentUserService.getCurrentBranch();

        return visitRepository.findByIdAndShiftBranch(id, branch).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }

    @Override
    @Transactional
    public VisitResponse createVisit(CreationVisitRequest visitRequest) {
        Shift shift = shiftService.getShiftByIdRaw(visitRequest.getShiftId());
        User attendedBy = currentUserService.getCurrentUser();

        if (shift.getVisit() != null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This shift already has a visit associated");
        }

        if (shift.getStatus() != ShiftStatus.PENDING) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A visit can only be created for a completed shift"
            );
        }

        shiftService.completeShift(shift.getId());

        BigDecimal totalAmount = normalizeMoney(visitRequest.getTotalAmount());
        List<VisitPaymentMovementRequest> movements = resolveMovementRequests(
                visitRequest.getPaymentMovements(),
                visitRequest.getPaymentStatus(),
                totalAmount,
                visitRequest.getPaidAt(),
                visitRequest.getPaymentMethod(),
                shift.getBranch()
        );

        validateTotalAmount(totalAmount);
        validatePaymentMovements(totalAmount, movements, shift.getBranch());

        Visit visitNew = new Visit();
        visitNew.setShift(shift);
        visitNew.setAttendedBy(attendedBy);
        visitNew.setCurrency(appSettingsService.getDefaultCurrency());
        visitNew.setTotalAmount(totalAmount);

        replacePaymentMovements(visitNew, movements);
        syncPaymentSummary(visitNew);

        return convertEntityIntoDto(visitRepository.save(visitNew));
    }

    @Override
    @Transactional
    public VisitResponse updateVisit(Long id, UpdateVisitRequest visitRequest) {
        Visit visit = getRawVisitById(id);

        BigDecimal totalAmount = normalizeMoney(visitRequest.getTotalAmount());
        List<VisitPaymentMovementRequest> movements = resolveMovementRequests(
                visitRequest.getPaymentMovements(),
                visitRequest.getPaymentStatus(),
                totalAmount,
                visitRequest.getPaidAt(),
                visitRequest.getPaymentMethod(),
                visit.getShift().getBranch()
        );

        validateTotalAmount(totalAmount);
        validatePaymentMovements(totalAmount, movements, visit.getShift().getBranch());

        visit.setTotalAmount(totalAmount);
        replacePaymentMovements(visit, movements);
        syncPaymentSummary(visit);

        return convertEntityIntoDto(visitRepository.save(visit));
    }

    private VisitResponse convertEntityIntoDto(Visit visit) {
        VisitFinancialSummary summary = buildFinancialSummary(visit.getTotalAmount(), visit.getPaymentMovements());

        VisitResponse visitResponse = new VisitResponse();
        visitResponse.setId(visit.getId());
        visitResponse.setCurrency(visit.getCurrency());
        visitResponse.setPaidAt(getLastPayment(visit.getPaymentMovements()).map(VisitPaymentMovement::getOccurredAt).orElse(null));
        visitResponse.setShiftId(visit.getShift().getId());
        visitResponse.setClient(convertClientIntoDto(visit.getShift().getClient()));
        User attendedBy = visit.getAttendedBy() != null
                ? visit.getAttendedBy()
                : visit.getShift().getOwner();

        if (attendedBy != null) {
            visitResponse.setAttendedByUserId(attendedBy.getId());
            visitResponse.setAttendedByName(attendedBy.getDisplayName());
            visitResponse.setAttendedByEmail(attendedBy.getEmail());
        }
        visitResponse.setPaymentStatus(summary.paymentStatus());
        visitResponse.setPaymentMethod(getLastPayment(visit.getPaymentMovements()).map(VisitPaymentMovement::getPaymentMethod).orElse(null));
        visitResponse.setTotalAmount(visit.getTotalAmount());
        visitResponse.setGrossPaidAmount(summary.grossPaidAmount());
        visitResponse.setRefundedAmount(summary.refundedAmount());
        visitResponse.setBonifiedAmount(summary.bonifiedAmount());
        visitResponse.setNetPaidAmount(summary.netPaidAmount());
        visitResponse.setCoveredAmount(summary.coveredAmount());
        visitResponse.setPendingAmount(summary.pendingAmount());
        visitResponse.setPaymentMovements(visit.getPaymentMovements().stream()
                .map(this::convertPaymentMovementIntoDto)
                .toList());

        return visitResponse;
    }

    private ClientResponse convertClientIntoDto(com.barbershop.shifts.entities.Client client) {
        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setId(client.getId());
        clientResponse.setEmail(client.getEmail());
        clientResponse.setPhoneNumber(client.getPhoneNumber());
        clientResponse.setFirstName(client.getFirstName());
        clientResponse.setLastName(client.getLastName());
        clientResponse.setDocumentNumber(client.getDocumentNumber());
        clientResponse.setNotes(client.getNotes());
        clientResponse.setSelfResponsible(client.getSelfResponsible());
        clientResponse.setResponsibleContactName(
                client.getResponsibleContact() != null ? client.getResponsibleContact().getFullName() : null
        );
        return clientResponse;
    }

    private VisitPaymentMovementResponse convertPaymentMovementIntoDto(VisitPaymentMovement movement) {
        return new VisitPaymentMovementResponse(
                movement.getId(),
                movement.getType(),
                movement.getAmount(),
                movement.getOccurredAt(),
                movement.getPaymentMethod(),
                movement.getNotes()
        );
    }

    private List<VisitPaymentMovementRequest> resolveMovementRequests(
            List<VisitPaymentMovementRequest> paymentMovements,
            PaymentStatus legacyPaymentStatus,
            BigDecimal totalAmount,
            LocalDateTime legacyPaidAt,
            PaymentMethod legacyPaymentMethod,
            Branch branch
    ) {
        if (paymentMovements != null && !paymentMovements.isEmpty()) {
            return paymentMovements;
        }

        if (legacyPaymentStatus == null || legacyPaymentStatus == PaymentStatus.PENDING) {
            return List.of();
        }

        if (legacyPaymentStatus == PaymentStatus.PAID) {
            return List.of(new VisitPaymentMovementRequest(
                    VisitPaymentMovementType.PAYMENT,
                    totalAmount,
                    legacyPaidAt,
                    legacyPaymentMethod,
                    null
            ));
        }

        if (legacyPaymentStatus == PaymentStatus.REFUNDED) {
            return List.of(
                    new VisitPaymentMovementRequest(
                            VisitPaymentMovementType.PAYMENT,
                            totalAmount,
                            legacyPaidAt,
                            legacyPaymentMethod,
                            null
                    ),
                    new VisitPaymentMovementRequest(
                            VisitPaymentMovementType.REFUND,
                            totalAmount,
                            legacyPaidAt,
                            legacyPaymentMethod,
                            null
                    )
            );
        }

        if (legacyPaymentStatus == PaymentStatus.BONIFIED) {
            return List.of(new VisitPaymentMovementRequest(
                    VisitPaymentMovementType.BONIFICATION,
                    totalAmount,
                    LocalDateTime.now(branch.resolveZoneId()),
                    null,
                    null
            ));
        }

        return List.of();
    }

    private void validateTotalAmount(BigDecimal totalAmount) {
        if (totalAmount == null || totalAmount.compareTo(ZERO) < 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Total amount must be zero or greater");
        }
    }

    private void validatePaymentMovements(BigDecimal totalAmount, List<VisitPaymentMovementRequest> movements, Branch branch) {
        BigDecimal grossPaid = ZERO;
        BigDecimal refunded = ZERO;
        BigDecimal bonified = ZERO;
        LocalDateTime now = LocalDateTime.now(branch.resolveZoneId());

        for (VisitPaymentMovementRequest movement : movements) {
            if (movement.getType() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Movement type is required");
            }

            if (movement.getAmount() == null || movement.getAmount().compareTo(BigDecimal.ZERO) <= 0) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Movement amount must be greater than zero");
            }

            if (movement.getOccurredAt() == null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Movement date is required");
            }

            if (movement.getOccurredAt().isAfter(now)) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Movement date cannot be in the future");
            }

            BigDecimal amount = normalizeMoney(movement.getAmount());

            if (movement.getType() == VisitPaymentMovementType.PAYMENT) {
                if (movement.getPaymentMethod() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method is required for payments");
                }
                grossPaid = grossPaid.add(amount);
            }

            if (movement.getType() == VisitPaymentMovementType.REFUND) {
                if (movement.getPaymentMethod() == null) {
                    throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payment method is required for refunds");
                }
                refunded = refunded.add(amount);
            }

            if (movement.getType() == VisitPaymentMovementType.BONIFICATION) {
                bonified = bonified.add(amount);
            }
        }

        if (refunded.compareTo(grossPaid) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Refunds cannot exceed received payments");
        }

        BigDecimal netPaid = grossPaid.subtract(refunded);
        BigDecimal covered = netPaid.add(bonified);

        if (covered.compareTo(totalAmount) > 0) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Payments and bonuses cannot exceed total amount");
        }
    }

    private void replacePaymentMovements(Visit visit, List<VisitPaymentMovementRequest> movements) {
        visit.getPaymentMovements().clear();

        for (VisitPaymentMovementRequest request : movements) {
            VisitPaymentMovement movement = new VisitPaymentMovement();
            movement.setVisit(visit);
            movement.setType(request.getType());
            movement.setAmount(normalizeMoney(request.getAmount()));
            movement.setOccurredAt(request.getOccurredAt());
            movement.setPaymentMethod(request.getType() == VisitPaymentMovementType.BONIFICATION ? null : request.getPaymentMethod());
            movement.setNotes(normalizeNotes(request.getNotes()));

            visit.getPaymentMovements().add(movement);
        }
    }

    private void syncPaymentSummary(Visit visit) {
        VisitFinancialSummary summary = buildFinancialSummary(visit.getTotalAmount(), visit.getPaymentMovements());

        visit.setPaymentStatus(summary.paymentStatus());
        visit.setPaidAt(getLastPayment(visit.getPaymentMovements()).map(VisitPaymentMovement::getOccurredAt).orElse(null));
        visit.setPaymentMethod(getLastPayment(visit.getPaymentMovements()).map(VisitPaymentMovement::getPaymentMethod).orElse(null));
    }

    private VisitFinancialSummary buildFinancialSummary(BigDecimal totalAmount, List<VisitPaymentMovement> movements) {
        BigDecimal grossPaid = sumMovements(movements, VisitPaymentMovementType.PAYMENT);
        BigDecimal refunded = sumMovements(movements, VisitPaymentMovementType.REFUND);
        BigDecimal bonified = sumMovements(movements, VisitPaymentMovementType.BONIFICATION);
        BigDecimal netPaid = grossPaid.subtract(refunded);
        if (netPaid.compareTo(ZERO) < 0) {
            netPaid = ZERO;
        }

        BigDecimal covered = netPaid.add(bonified);
        BigDecimal pending = normalizeMoney(totalAmount).subtract(covered);
        if (pending.compareTo(ZERO) < 0) {
            pending = ZERO;
        }

        return new VisitFinancialSummary(
                normalizeMoney(grossPaid),
                normalizeMoney(refunded),
                normalizeMoney(bonified),
                normalizeMoney(netPaid),
                normalizeMoney(covered),
                normalizeMoney(pending),
                resolvePaymentStatus(normalizeMoney(totalAmount), grossPaid, refunded, bonified, netPaid, covered, pending)
        );
    }

    private PaymentStatus resolvePaymentStatus(
            BigDecimal totalAmount,
            BigDecimal grossPaid,
            BigDecimal refunded,
            BigDecimal bonified,
            BigDecimal netPaid,
            BigDecimal covered,
            BigDecimal pending
    ) {
        boolean hasRefund = refunded.compareTo(ZERO) > 0;
        boolean hasBonus = bonified.compareTo(ZERO) > 0;

        if (covered.compareTo(ZERO) <= 0) {
            return hasRefund && grossPaid.compareTo(ZERO) > 0
                    ? PaymentStatus.REFUNDED
                    : PaymentStatus.PENDING;
        }

        if (hasRefund && hasBonus) {
            return pending.compareTo(ZERO) > 0
                    ? PaymentStatus.PARTIAL_WITH_ADJUSTMENT
                    : PaymentStatus.PAID_WITH_ADJUSTMENT;
        }

        if (pending.compareTo(ZERO) > 0) {
            return hasRefund
                    ? PaymentStatus.PARTIALLY_REFUNDED
                    : PaymentStatus.PARTIAL;
        }

        if (hasRefund && netPaid.compareTo(ZERO) <= 0) {
            return PaymentStatus.REFUNDED;
        }

        if (hasRefund && netPaid.compareTo(ZERO) > 0) {
            return PaymentStatus.PARTIALLY_REFUNDED;
        }

        if (hasBonus && netPaid.compareTo(ZERO) <= 0) {
            return PaymentStatus.BONIFIED;
        }

        if (hasBonus) {
            return PaymentStatus.PAID_WITH_BONIFICATION;
        }

        return totalAmount.compareTo(ZERO) == 0 ? PaymentStatus.PENDING : PaymentStatus.PAID;
    }

    private BigDecimal sumMovements(List<VisitPaymentMovement> movements, VisitPaymentMovementType type) {
        return movements.stream()
                .filter(movement -> movement.getType() == type)
                .map(VisitPaymentMovement::getAmount)
                .map(this::normalizeMoney)
                .reduce(ZERO, BigDecimal::add);
    }

    private java.util.Optional<VisitPaymentMovement> getLastPayment(List<VisitPaymentMovement> movements) {
        return movements.stream()
                .filter(movement -> movement.getType() == VisitPaymentMovementType.PAYMENT)
                .max(Comparator.comparing(VisitPaymentMovement::getOccurredAt));
    }

    private BigDecimal normalizeMoney(BigDecimal value) {
        return (value == null ? ZERO : value).setScale(2, RoundingMode.HALF_UP);
    }

    private String normalizeNotes(String notes) {
        if (notes == null || notes.isBlank()) {
            return null;
        }

        return notes.trim();
    }

    private record VisitFinancialSummary(
            BigDecimal grossPaidAmount,
            BigDecimal refundedAmount,
            BigDecimal bonifiedAmount,
            BigDecimal netPaidAmount,
            BigDecimal coveredAmount,
            BigDecimal pendingAmount,
            PaymentStatus paymentStatus
    ) {
    }
}
