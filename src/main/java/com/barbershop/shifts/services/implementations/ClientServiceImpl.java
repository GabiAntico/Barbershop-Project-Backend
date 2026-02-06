package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.ClientRequest;
import com.barbershop.shifts.dtos.ClientResponse;
import com.barbershop.shifts.dtos.CreationClientRequest;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.repositories.ClientRepositoryJpa;
import com.barbershop.shifts.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
public class ClientServiceImpl implements ClientService {

    @Autowired
    private ClientRepositoryJpa clientRepository;

    @Override
    public ClientResponse createClient(CreationClientRequest clientRequest) {
        if(clientRequest.getName() == null || clientRequest.getName() == ""){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Client client = new Client();
        client.setName(clientRequest.getName());

        Client clientSaved = clientRepository.save(client);

        return convertEntityToDto(clientSaved);
    }

    @Override
    public ClientResponse updateClient(ClientRequest newClient) {
        if(newClient.getId() <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Client actualClient = getClientByIdRaw(newClient.getId());

        if(actualClient.getName().equals(newClient.getName())){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new information is same at the older");
        }

        Client client = new Client();
        client.setId(newClient.getId());
        client.setName(newClient.getName());

        Client clientSaved = clientRepository.save(client);

        return convertEntityToDto(clientSaved);
    }

    @Override
    public ClientResponse deleteClient(Long id) {
        if(id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Client client = getClientByIdRaw(id);

        clientRepository.deleteById(id);

        return convertEntityToDto(client);
    }

    @Override
    public Client getClientByIdRaw(Long id) {
        if(id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        return clientRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }

    @Override
    public ClientResponse getClientById(Long id){
        if(id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Client client = clientRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));

        return convertEntityToDto(client);
    }

    @Override
    public List<ClientResponse> getAllClients() {
        List<Client> clients = clientRepository.findAll();

        List<ClientResponse> clientsDtos = new ArrayList();

        for(Client client : clients){
            ClientResponse clientResponse = convertEntityToDto(client);
            clientsDtos.add(clientResponse);
        }

        return clientsDtos;
    }

    private ClientResponse convertEntityToDto(Client client){
        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setId(client.getId());
        clientResponse.setName(client.getName());
        return clientResponse;
    }
}
