package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.visits.CreationVisitRequest;
import com.barbershop.shifts.dtos.visits.UpdateVisitRequest;
import com.barbershop.shifts.dtos.visits.VisitResponse;
import com.barbershop.shifts.services.VisitService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@RequestMapping("/api/visits")
@CrossOrigin("*")
public class VisitController {

    @Autowired
    private VisitService visitService;

    @GetMapping
    public ResponseEntity<List<VisitResponse>> getAllVisits(){
        return ResponseEntity.ok(visitService.getAllVisits());
    }

    @GetMapping("/{id}")
    public ResponseEntity<VisitResponse> getVisitById(@PathVariable Long id){
        return ResponseEntity.ok(visitService.getVisitById(id));
    }

    @PostMapping
    public ResponseEntity<VisitResponse> createVisit(@Valid @RequestBody CreationVisitRequest visitRequest){
        VisitResponse createdVisit = visitService.createVisit(visitRequest);

        URI location = URI.create("/api/visits/" + createdVisit.getId());

        return ResponseEntity.created(location).body(createdVisit);
    }

    @PutMapping("/{id}")
    public ResponseEntity<VisitResponse> updateVisit(
            @PathVariable Long id,
            @Valid @RequestBody UpdateVisitRequest visitRequest
    ){
        return ResponseEntity.ok(visitService.updateVisit(id, visitRequest));
    }
}
