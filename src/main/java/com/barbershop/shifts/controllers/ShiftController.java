package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.CreationShiftRequest;
import com.barbershop.shifts.dtos.ShiftCompleteResponse;
import com.barbershop.shifts.dtos.ShiftResponse;
import com.barbershop.shifts.repositories.ShiftRepositoryJpa;
import com.barbershop.shifts.services.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
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

    @GetMapping("/{id}")
    public ResponseEntity<ShiftResponse> findShiftById(@PathVariable Long id){
        return ResponseEntity.ok(shiftService.getShiftById(id));
    }

    @PostMapping
    public ResponseEntity<ShiftResponse> createShift(@RequestBody CreationShiftRequest shiftRequest){

        ShiftResponse shiftSaved = shiftService.createShift(shiftRequest);

        URI location = URI.create("/api/shifts" + shiftSaved.getId());

        return ResponseEntity.created(location).body(shiftSaved);
    }
}
