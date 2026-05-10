package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.shifts.AgendaSlotResponse;
import com.barbershop.shifts.dtos.shifts.CreationShiftRequest;
import com.barbershop.shifts.dtos.shifts.ShiftCompleteResponse;
import com.barbershop.shifts.dtos.shifts.ShiftResponse;
import com.barbershop.shifts.dtos.shifts.TimeSlotAvailabilityResponse;
import com.barbershop.shifts.dtos.shifts.UpdateShiftRequest;
import com.barbershop.shifts.services.ShiftService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/shifts")
@CrossOrigin("*")
public class ShiftController {
    @Autowired
    private ShiftService shiftService;

    @GetMapping
    public ResponseEntity<List<ShiftResponse>> findAllShifts(){
        return ResponseEntity.ok(shiftService.getAllShifts());
    }

    @GetMapping("/complete")
    public ResponseEntity<List<ShiftCompleteResponse>> findAllCompleteShifts(){
        return ResponseEntity.ok(shiftService.getAllCompleteShifts());
    }

    @GetMapping("/availability")
    public ResponseEntity<List<TimeSlotAvailabilityResponse>> findAvailabilityByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date,
            @RequestParam(required = false) Long excludeShiftId
    ){
        return ResponseEntity.ok(shiftService.getAvailabilityByDate(date, excludeShiftId));
    }

    @GetMapping("/agenda")
    public ResponseEntity<List<AgendaSlotResponse>> findAgendaByDate(
            @RequestParam @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate date
    ){
        return ResponseEntity.ok(shiftService.getAgendaByDate(date));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponse> findShiftById(@PathVariable Long id){
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @PostMapping
    public ResponseEntity<ShiftResponse> createShift(@Valid @RequestBody CreationShiftRequest shiftRequest){

        ShiftResponse shiftSaved = shiftService.createShift(shiftRequest);

        URI location = URI.create("/api/shifts/" + shiftSaved.getId());

        return ResponseEntity.created(location).body(shiftSaved);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ShiftResponse> updateShift(@PathVariable Long id, @Valid @RequestBody UpdateShiftRequest shiftRequest){
        return ResponseEntity.ok(shiftService.updateShift(id, shiftRequest));
    }
}
