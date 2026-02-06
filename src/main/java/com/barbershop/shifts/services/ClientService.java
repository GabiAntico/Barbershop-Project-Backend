package com.barbershop.shifts.services;

import com.barbershop.shifts.dtos.ClientRequest;
import com.barbershop.shifts.dtos.ClientResponse;
import com.barbershop.shifts.dtos.CreationClientRequest;
import com.barbershop.shifts.entities.Client;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public interface ClientService {
    ClientResponse createClient(CreationClientRequest clientRequest);
    ClientResponse updateClient(ClientRequest newClient);
    ClientResponse deleteClient(Long id);
    Client getClientByIdRaw(Long id);
    ClientResponse getClientById(Long id);
    List<ClientResponse> getAllClients();
}
