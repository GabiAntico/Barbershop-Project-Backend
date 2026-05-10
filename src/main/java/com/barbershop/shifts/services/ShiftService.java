package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.shifts.AgendaSlotResponse;
import com.barbershop.shifts.dtos.shifts.CreationShiftRequest;
import com.barbershop.shifts.dtos.shifts.ShiftCompleteResponse;
import com.barbershop.shifts.dtos.shifts.ShiftResponse;
import com.barbershop.shifts.dtos.shifts.TimeSlotAvailabilityResponse;
import com.barbershop.shifts.dtos.shifts.UpdateShiftRequest;
import com.barbershop.shifts.entities.Shift;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public interface ShiftService {

    List<ShiftResponse> getAllShifts();
    List<ShiftCompleteResponse> getAllCompleteShifts();
    ShiftResponse getShiftById(Long id);
    Shift getShiftByIdRaw(Long id);
    List<TimeSlotAvailabilityResponse> getAvailabilityByDate(LocalDate date, Long excludeShiftId);
    List<AgendaSlotResponse> getAgendaByDate(LocalDate date);
    ShiftResponse createShift(CreationShiftRequest shiftRequest);
    ShiftResponse updateShift(Long id, UpdateShiftRequest shiftRequest);
    ShiftResponse completeShift(Long shiftId);
}
