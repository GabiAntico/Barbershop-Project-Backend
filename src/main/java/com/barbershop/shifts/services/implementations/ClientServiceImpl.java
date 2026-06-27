package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.clients.ClientRequest;
import com.barbershop.shifts.dtos.clients.ClientResponse;
import com.barbershop.shifts.dtos.clients.CreationClientRequest;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.ResponsibleContact;
import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.repositories.ClientRepositoryJpa;
import com.barbershop.shifts.repositories.ResponsibleContactRepositoryJpa;
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
    private ResponsibleContactRepositoryJpa responsibleContactRepository;

    @Autowired
    private CurrentUserService currentUserService;

    @Override
    public ClientResponse createClient(CreationClientRequest clientRequest) {
        User owner = currentUserService.getCurrentUser();
        Client client = new Client();
        client.setEmail(normalize(clientRequest.getEmail()));
        client.setPhoneNumber(normalizePhoneNumber(clientRequest.getPhoneNumber()));
        client.setFirstName(normalize(clientRequest.getFirstName()));
        client.setLastName(normalize(clientRequest.getLastName()));
        client.setDocumentNumber(normalize(clientRequest.getDocumentNumber()));
        client.setNotes(normalize(clientRequest.getNotes()));
        client.setSelfResponsible(isSelfResponsible(clientRequest.getSelfResponsible()));
        client.setOwner(owner);
        client.setBarbershop(owner.getBarbershop());

        String responsibleContactName = resolveResponsibleContactName(
                client.getSelfResponsible(),
                client.getFirstName(),
                client.getLastName(),
                clientRequest.getResponsibleContactName()
        );

        validateResponsibleRules(client.getSelfResponsible(), client.getFirstName(), responsibleContactName);

        client.setResponsibleContact(resolveResponsibleContact(
                client.getPhoneNumber(),
                client.getEmail(),
                responsibleContactName,
                owner
        ));

        if(clientRepository.existsByPhoneNumberAndFirstNameAndLastNameAndBarbershop(
                client.getPhoneNumber(),
                client.getFirstName(),
                client.getLastName(),
                owner.getBarbershop()
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client already exists");
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
        client.setPhoneNumber(normalizePhoneNumber(newClient.getPhoneNumber()));
        client.setFirstName(normalize(newClient.getFirstName()));
        client.setLastName(normalize(newClient.getLastName()));
        client.setDocumentNumber(normalize(newClient.getDocumentNumber()));
        client.setNotes(normalize(newClient.getNotes()));
        client.setSelfResponsible(isSelfResponsible(newClient.getSelfResponsible()));
        client.setOwner(actualClient.getOwner());
        client.setBarbershop(owner.getBarbershop());
        String responsibleContactName = resolveResponsibleContactName(
                client.getSelfResponsible(),
                client.getFirstName(),
                client.getLastName(),
                newClient.getResponsibleContactName()
        );

        validateResponsibleRules(client.getSelfResponsible(), client.getFirstName(), responsibleContactName);

        if(clientRepository.existsByPhoneNumberAndFirstNameAndLastNameAndIdNotAndBarbershop(
                client.getPhoneNumber(),
                client.getFirstName(),
                client.getLastName(),
                client.getId(),
                owner.getBarbershop()
        )) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Client already exists");
        }

        if(areEquals(actualClient, client, responsibleContactName)){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new information is the same as the previous one");
        }

        client.setResponsibleContact(resolveResponsibleContact(
                client.getPhoneNumber(),
                client.getEmail(),
                responsibleContactName,
                owner
        ));

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
        clientResponse.setSelfResponsible(!Boolean.FALSE.equals(client.getSelfResponsible()));
        clientResponse.setResponsibleContactName(
                client.getResponsibleContact() == null ? null : client.getResponsibleContact().getFullName()
        );
        clientResponse.setFirstName(client.getFirstName());
        clientResponse.setLastName(client.getLastName());
        clientResponse.setDocumentNumber(client.getDocumentNumber());
        clientResponse.setNotes(client.getNotes());

        return clientResponse;
    }

    private boolean areEquals(Client instance1, Client instance2, String responsibleContactName){
        return Objects.equals(instance1.getId(), instance2.getId()) &&
                Objects.equals(instance1.getEmail(), instance2.getEmail()) &&
                Objects.equals(instance1.getPhoneNumber(), instance2.getPhoneNumber()) &&
                Objects.equals(instance1.getFirstName(), instance2.getFirstName()) &&
                Objects.equals(instance1.getLastName(), instance2.getLastName()) &&
                Objects.equals(instance1.getDocumentNumber(), instance2.getDocumentNumber()) &&
                Objects.equals(instance1.getNotes(), instance2.getNotes()) &&
                Objects.equals(!Boolean.FALSE.equals(instance1.getSelfResponsible()), !Boolean.FALSE.equals(instance2.getSelfResponsible())) &&
                Objects.equals(
                        instance1.getResponsibleContact() == null ? null : instance1.getResponsibleContact().getFullName(),
                        responsibleContactName
                );
    }

    private String normalize(String value) {
        if (value == null) return null;

        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    private String normalizePhoneNumber(String value) {
        String normalized = normalize(value);

        if (normalized == null || !normalized.matches("\\d{8,15}")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid phone number");
        }

        return normalized;
    }

    private boolean isSelfResponsible(Boolean value) {
        return value == null || value;
    }

    private String resolveResponsibleContactName(
            Boolean selfResponsible,
            String firstName,
            String lastName,
            String requestResponsibleContactName
    ) {
        if (Boolean.FALSE.equals(selfResponsible)) {
            return normalize(requestResponsibleContactName);
        }

        return buildFullName(firstName, lastName);
    }

    private void validateResponsibleRules(Boolean selfResponsible, String firstName, String responsibleContactName) {
        if (!Boolean.FALSE.equals(selfResponsible)) {
            return;
        }

        if (firstName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Client first name is required");
        }

        if (responsibleContactName == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Responsible contact name is required");
        }
    }

    private ResponsibleContact resolveResponsibleContact(String phoneNumber, String email, String fullName, User owner) {
        ResponsibleContact contact = responsibleContactRepository
                .findFirstByPhoneNumberAndBarbershop(phoneNumber, owner.getBarbershop())
                .orElseGet(ResponsibleContact::new);

        contact.setPhoneNumber(phoneNumber);
        contact.setEmail(email);
        contact.setFullName(fullName);
        contact.setBarbershop(owner.getBarbershop());

        return responsibleContactRepository.save(contact);
    }

    private String buildFullName(String firstName, String lastName) {
        String fullName = String.join(" ",
                firstName == null ? "" : firstName,
                lastName == null ? "" : lastName
        ).trim();

        return fullName.isEmpty() ? null : fullName;
    }

}
