package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.clients.ClientRequest;
import com.barbershop.shifts.dtos.clients.ClientResponse;
import com.barbershop.shifts.dtos.clients.CreationClientRequest;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.ClientRepositoryJpa;
import com.barbershop.shifts.services.ClientService;
import com.barbershop.shifts.services.CurrentUserService;
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

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    public ClientResponse createClient(CreationClientRequest clientRequest) {
        User owner = currentUserService.getCurrentUser();
        Client client = new Client();
        client.setEmail(normalize(clientRequest.getEmail()));
        client.setPhoneNumber(normalize(clientRequest.getPhoneNumber()));
        client.setFirstName(normalize(clientRequest.getFirstName()));
        client.setLastName(normalize(clientRequest.getLastName()));
        client.setDocumentNumber(normalize(clientRequest.getDocumentNumber()));
        client.setNotes(normalize(clientRequest.getNotes()));
        client.setOwner(owner);
        client.setBarbershop(owner.getBarbershop());

        if(client.getEmail() != null && clientRepository.existsByEmailAndBarbershop(client.getEmail(), owner.getBarbershop())) {
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
        User owner = currentUserService.getCurrentUser();

        Client client = new Client();
        client.setId(newClient.getId());
        client.setEmail(normalize(newClient.getEmail()));
        client.setPhoneNumber(normalize(newClient.getPhoneNumber()));
        client.setFirstName(normalize(newClient.getFirstName()));
        client.setLastName(normalize(newClient.getLastName()));
        client.setDocumentNumber(normalize(newClient.getDocumentNumber()));
        client.setNotes(normalize(newClient.getNotes()));
        client.setOwner(actualClient.getOwner());
        client.setBarbershop(owner.getBarbershop());

        if(client.getEmail() != null && clientRepository.existsByEmailAndIdNotAndBarbershop(client.getEmail(), client.getId(), owner.getBarbershop())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Email already exists");
        }

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

        User owner = currentUserService.getCurrentUser();

        return clientRepository.findByIdAndBarbershop(id, owner.getBarbershop()).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }

    @Override
    public ClientResponse getClientById(Long id){
        Client client = getClientByIdRaw(id);

        return convertEntityIntoDto(client);
    }

    @Override
    public List<ClientResponse> getAllClients() {
        User owner = currentUserService.getCurrentUser();
        List<Client> clients = clientRepository.findAllByBarbershop(owner.getBarbershop());

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
        clientResponse.setPhoneNumber(client.getPhoneNumber());
        clientResponse.setFirstName(client.getFirstName());
        clientResponse.setLastName(client.getLastName());
        clientResponse.setDocumentNumber(client.getDocumentNumber());
        clientResponse.setNotes(client.getNotes());

        return clientResponse;
    }

    private boolean areEquals(Client instance1, Client instance2){
        return Objects.equals(instance1.getId(), instance2.getId()) &&
                Objects.equals(instance1.getEmail(), instance2.getEmail()) &&
                Objects.equals(instance1.getPhoneNumber(), instance2.getPhoneNumber()) &&
                Objects.equals(instance1.getFirstName(), instance2.getFirstName()) &&
                Objects.equals(instance1.getLastName(), instance2.getLastName()) &&
                Objects.equals(instance1.getDocumentNumber(), instance2.getDocumentNumber()) &&
                Objects.equals(instance1.getNotes(), instance2.getNotes());
    }

    private String normalize(String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }
}
