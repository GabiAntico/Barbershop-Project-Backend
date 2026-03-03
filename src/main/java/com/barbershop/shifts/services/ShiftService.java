package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.shifts.CreationShiftRequest;
import com.barbershop.shifts.dtos.shifts.ShiftCompleteResponse;
import com.barbershop.shifts.dtos.shifts.ShiftResponse;
import com.barbershop.shifts.entities.Shift;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ShiftService {

    List<ShiftResponse> getAllShifts();
    List<ShiftCompleteResponse> getAllCompleteShifts();
    ShiftResponse getShiftById(Long id);
    Shift getShiftByIdRaw(Long id);
    ShiftResponse createShift(CreationShiftRequest shiftRequest);
    ShiftResponse updateShift(Long id, CreationShiftRequest shiftRequest);
}
