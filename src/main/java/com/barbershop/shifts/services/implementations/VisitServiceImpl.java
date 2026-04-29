package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.visits.CreationVisitRequest;
import com.barbershop.shifts.dtos.visits.UpdateVisitRequest;
import com.barbershop.shifts.dtos.visits.VisitResponse;
import com.barbershop.shifts.entities.PaymentStatus;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.entities.Visit;
import com.barbershop.shifts.repositories.VisitRepositoryJpa;
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

    @Override
    public List<VisitResponse> getAllVisits() {
        List<Visit> visits = visitRepository.findAll();

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

        return visitRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }


    @Override
    public VisitResponse createVisit(CreationVisitRequest visitRequest) {


        Shift shift = shiftService.getShiftByIdRaw(visitRequest.getShiftId());

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
        visitNew.setCurrency(visitRequest.getCurrency());
        visitNew.setPaymentStatus(visitRequest.getPaymentStatus());
        visitNew.setPaymentMethod(visitRequest.getPaymentMethod());
        visitNew.setPaidAt(visitRequest.getPaidAt());
        visitNew.setTotalAmount(visitRequest.getTotalAmount());

        return convertEntityIntoDto(visitRepository.save(visitNew));
    }

    //TODO: Finish update visit
    @Override
    public VisitResponse updateVisit(Long id, UpdateVisitRequest visitRequest) {
        return null;
    }

    private VisitResponse convertEntityIntoDto(Visit visit){
        VisitResponse visitResponse = new VisitResponse();

        visitResponse.setId(visit.getId());
        visitResponse.setCurrency(visit.getCurrency());
        visitResponse.setPaidAt(visit.getPaidAt());
        visitResponse.setShiftId(visit.getShift().getId());
        visitResponse.setPaymentStatus(visit.getPaymentStatus());
        visitResponse.setPaymentMethod(visit.getPaymentMethod());
        visitResponse.setTotalAmount(visit.getTotalAmount());

        return visitResponse;
    }

    private void validatePaidAmount(CreationVisitRequest request) {
        if (request.getPaymentStatus() == PaymentStatus.PAID &&
                request.getTotalAmount().compareTo(BigDecimal.ZERO) <= 0) {

            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Total amount must be greater than zero when payment status is PAID"
            );
        }
    }

    private void validatePaymentConsistency(CreationVisitRequest request){
        if(request.getPaymentStatus() == PaymentStatus.PAID){

            if(request.getPaidAt() == null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paidAt is required when payment status is PAID");
            }
            if(request.getPaymentMethod() == null){
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "paymentMethod is required when payment status is PAID");
            }
        }

        if (request.getPaymentStatus() == PaymentStatus.PENDING) {
            if (request.getPaidAt() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "paidAt must be null when payment status is PENDING");
            }
            if (request.getPaymentMethod() != null) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "paymentMethod must be null when payment status is PENDING");
            }
        }
    }

    private void validatePaymentDates(CreationVisitRequest request){
        if (request.getPaymentStatus() == PaymentStatus.PAID) {
            if (request.getPaidAt().isAfter(LocalDateTime.now())) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "paidAt cannot be in the future."
                );
            }
        }
    }
}
