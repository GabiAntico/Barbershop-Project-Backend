package com.barbershop.shifts.controllers;

import com.barbershop.shifts.dtos.ClientRequest;
import com.barbershop.shifts.dtos.ClientResponse;
import com.barbershop.shifts.dtos.CreationClientRequest;
import com.barbershop.shifts.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.net.URI;
import java.util.List;

@RestController
@CrossOrigin("*")
@RequestMapping("/api/clients")
public class ClientController {

    @Autowired
    private ClientService clientService;

    @GetMapping
    public ResponseEntity<List<ClientResponse>> findAllClients(){
        return ResponseEntity.ok(clientService.getAllClients());
    }

    @GetMapping("/{id}")
    public ResponseEntity<ClientResponse> findClientById(@PathVariable Long id){
        return ResponseEntity.ok(clientService.getClientById(id));
    }

    @PostMapping
    public ResponseEntity<ClientResponse> createClient(@RequestBody CreationClientRequest clientRequest){
        ClientResponse clientSaved = clientService.createClient(clientRequest);

        URI location = URI.create("/api/clients/" + clientSaved.getId());

        return ResponseEntity.created(location).body(clientSaved);
    }

    @PutMapping
    public ResponseEntity<ClientResponse> updateClient(@RequestBody ClientRequest clientRequest){
        return ResponseEntity.ok(clientService.updateClient(clientRequest));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<ClientResponse> deleteClient(@PathVariable Long id){
        return ResponseEntity.ok(clientService.deleteClient(id));
    }
}
