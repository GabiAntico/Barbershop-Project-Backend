package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.clients.ClientResponse;
import com.barbershop.shifts.dtos.visits.CreationVisitRequest;
import com.barbershop.shifts.dtos.visits.UpdateVisitRequest;
import com.barbershop.shifts.dtos.visits.VisitResponse;
import com.barbershop.shifts.entities.PaymentMethod;
import com.barbershop.shifts.entities.PaymentStatus;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.entities.Visit;
import com.barbershop.shifts.repositories.VisitRepositoryJpa;
import com.barbershop.shifts.services.CurrentUserService;
import com.barbershop.shifts.services.ShiftService;
import com.barbershop.shifts.services.VisitService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class VisitServiceImpl implements VisitService {

    @Autowired
    private VisitRepositoryJpa visitRepository;

    @Autowired
    private ShiftService shiftService;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    public List<VisitResponse> getAllVisits() {
        Branch branch = currentUserService.getCurrentBranch();
        List<Visit> visits = visitRepository.findAllByShiftBranch(branch);

        List<VisitResponse> visitResponses = new ArrayList<>();
        for(Visit visit : visits){
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
        if(id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Branch branch = currentUserService.getCurrentBranch();

        return visitRepository.findByIdAndShiftBranch(id, branch).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }


    @Override
    public VisitResponse createVisit(CreationVisitRequest visitRequest) {


        Shift shift = shiftService.getShiftByIdRaw(visitRequest.getShiftId());
        User attendedBy = currentUserService.getCurrentUser();

        if(shift.getVisit() != null){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "This shift already has a visit associated");
        }

        if(shift.getStatus() != ShiftStatus.PENDING){
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "A visit can only be created for a completed shift"
            );
        }

        shiftService.completeShift(shift.getId());

        validatePaidAmount(visitRequest);

        validatePaymentConsistency(visitRequest);

        validatePaymentDates(visitRequest);

        Visit visitNew = new Visit();

        visitNew.setShift(shift);
        visitNew.setAttendedBy(attendedBy);
        visitNew.setCurrency(visitRequest.getCurrency());
        visitNew.setPaymentStatus(visitRequest.getPaymentStatus());
        visitNew.setPaymentMethod(visitRequest.getPaymentMethod());
        visitNew.setPaidAt(visitRequest.getPaidAt());
        visitNew.setTotalAmount(visitRequest.getTotalAmount());

        return convertEntityIntoDto(visitRepository.save(visitNew));
    }

    @Override
    public VisitResponse updateVisit(Long id, UpdateVisitRequest visitRequest) {
        Visit visit = getRawVisitById(id);

        validatePaidAmount(visitRequest.getPaymentStatus(), visitRequest.getTotalAmount());
        validatePaymentConsistency(visitRequest.getPaymentStatus(), visitRequest.getPaidAt(), visitRequest.getPaymentMethod());
        validatePaymentDates(visitRequest.getPaymentStatus(), visitRequest.getPaidAt());

        visit.setCurrency(visitRequest.getCurrency());
        visit.setPaymentStatus(visitRequest.getPaymentStatus());
        visit.setPaymentMethod(visitRequest.getPaymentMethod());
        visit.setPaidAt(visitRequest.getPaidAt());
        visit.setTotalAmount(visitRequest.getTotalAmount());

        return convertEntityIntoDto(visitRepository.save(visit));
    }

    private VisitResponse convertEntityIntoDto(Visit visit){
        VisitResponse visitResponse = new VisitResponse();

        visitResponse.setId(visit.getId());
        visitResponse.setCurrency(visit.getCurrency());
        visitResponse.setPaidAt(visit.getPaidAt());
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
        visitResponse.setPaymentStatus(visit.getPaymentStatus());
        visitResponse.setPaymentMethod(visit.getPaymentMethod());
        visitResponse.setTotalAmount(visit.getTotalAmount());

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
        return clientResponse;
    }

    private void validatePaidAmount(CreationVisitRequest request) {
        validatePaidAmount(request.getPaymentStatus(), request.getTotalAmount());
    }

    private void validatePaidAmount(PaymentStatus paymentStatus, BigDecimal totalAmount) {
        if (paymentStatus == PaymentStatus.PAID &&
                totalAmount.compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total amount must be greater than zero when payment status is PAID"
            );
        }
    }

    private void validatePaymentConsistency(CreationVisitRequest request){
        validatePaymentConsistency(request.getPaymentStatus(), request.getPaidAt(), request.getPaymentMethod());
    }

    private void validatePaymentConsistency(PaymentStatus paymentStatus, LocalDateTime paidAt, PaymentMethod paymentMethod){
        if(paymentStatus == PaymentStatus.PAID){

            if(paidAt == null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paidAt is required when payment status is PAID");
            }
            if(paymentMethod == null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentMethod is required when payment status is PAID");
            }
        }

        if (paymentStatus == PaymentStatus.PENDING) {
            if (paidAt != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "paidAt must be null when payment status is PENDING");
            }
            if (paymentMethod != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "paymentMethod must be null when payment status is PENDING");
            }
        }
    }

    private void validatePaymentDates(CreationVisitRequest request){
        validatePaymentDates(request.getPaymentStatus(), request.getPaidAt());
    }

    private void validatePaymentDates(PaymentStatus paymentStatus, LocalDateTime paidAt){
        if (paymentStatus == PaymentStatus.PAID) {
            if (paidAt.isAfter(LocalDateTime.now())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "paidAt cannot be in the future."
                );
            }
        }
    }
}
