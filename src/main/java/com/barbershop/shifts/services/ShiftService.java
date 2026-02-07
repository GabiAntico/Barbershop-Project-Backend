package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.CreationShiftRequest;
import com.barbershop.shifts.dtos.ShiftResponse;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.repositories.ShiftRepositoryJpa;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShiftService {

    List<ShiftResponse> getAllShifts();
    ShiftResponse getShiftById(Long id);
    Shift getShiftByIdRaw(Long id);
    ShiftResponse createShift(CreationShiftRequest shiftRequest);
}
