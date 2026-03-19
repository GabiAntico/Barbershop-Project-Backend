package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.visits.CreationVisitRequest;
import com.barbershop.shifts.dtos.visits.UpdateVisitRequest;
import com.barbershop.shifts.dtos.visits.VisitResponse;
import com.barbershop.shifts.entities.Visit;

public interface VisitService {
    VisitResponse getAllVisits();
    VisitResponse getVisitById(Long id);
    Visit getRawVisitById(Long id);
    VisitResponse createVisit(CreationVisitRequest visitRequest);
    VisitResponse updateVisit(Long id, UpdateVisitRequest visitRequest);
}
