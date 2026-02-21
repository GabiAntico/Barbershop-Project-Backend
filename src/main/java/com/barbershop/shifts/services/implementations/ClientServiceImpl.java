package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.ClientRequest;
import com.barbershop.shifts.dtos.ClientResponse;
import com.barbershop.shifts.dtos.CreationClientRequest;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.repositories.ClientRepositoryJpa;
import com.barbershop.shifts.services.ClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
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
        Client client = new Client();
        client.setEmail(clientRequest.getEmail());
        client.setFirstName(clientRequest.getFirstName());
        client.setLastName(clientRequest.getLastName());
        client.setDocumentNumber(clientRequest.getDocumentNumber());

        if(clientRepository.existsByEmail(clientRequest.getEmail())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

        Client clientSaved = clientRepository.save(client);

        return convertEntityIntoDto(clientSaved);
    }

    @Override
    public ClientResponse updateClient(ClientRequest newClient) {

        if(newClient.getId() == null || newClient.getId() <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Client actualClient = getClientByIdRaw(newClient.getId());

        Client client = new Client();
        client.setId(newClient.getId());
        client.setEmail(newClient.getEmail());
        client.setFirstName(newClient.getFirstName());
        client.setLastName(newClient.getLastName());
        client.setDocumentNumber(newClient.getDocumentNumber());

        if(areEquals(actualClient, client)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new information is the same as the previous one");
        }

        Client clientSaved = clientRepository.save(client);

        return convertEntityIntoDto(clientSaved);
    }

    @Override
    public ClientResponse deleteClient(Long id) {
        if(id == null || id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Client client = getClientByIdRaw(id);

        clientRepository.deleteById(id);

        return convertEntityIntoDto(client);
    }

    @Override
    public Client getClientByIdRaw(Long id) {
        if(id == null || id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        return clientRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }

    @Override
    public ClientResponse getClientById(Long id){
        Client client = getClientByIdRaw(id);

        return convertEntityIntoDto(client);
    }

    @Override
    public List<ClientResponse> getAllClients() {
        List<Client> clients = clientRepository.findAll();

        List<ClientResponse> clientsDtos = new ArrayList();

        for(Client client : clients){
            ClientResponse clientResponse = convertEntityIntoDto(client);
            clientsDtos.add(clientResponse);
        }

        return clientsDtos;
    }

    private ClientResponse convertEntityIntoDto(Client client){
        ClientResponse clientResponse = new ClientResponse();
        clientResponse.setId(client.getId());
        clientResponse.setEmail(client.getEmail());
        clientResponse.setFirstName(client.getFirstName());
        clientResponse.setLastName(client.getLastName());
        clientResponse.setDocumentNumber(client.getDocumentNumber());

        return clientResponse;
    }

    private boolean areEquals(Client instance1, Client instance2){
        return Objects.equals(instance1.getId(), instance2.getId()) &&
                Objects.equals(instance1.getEmail(), instance2.getEmail()) &&
                Objects.equals(instance1.getFirstName(), instance2.getFirstName()) &&
                Objects.equals(instance1.getLastName(), instance2.getLastName()) &&
                Objects.equals(instance1.getDocumentNumber(), instance2.getDocumentNumber());
    }
}
