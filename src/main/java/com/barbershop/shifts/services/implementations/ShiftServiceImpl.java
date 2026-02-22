package com.barbershop.shifts.services.implementations;

import com.barbershop.shifts.dtos.ClientResponse;
import com.barbershop.shifts.dtos.CreationShiftRequest;
import com.barbershop.shifts.dtos.ShiftCompleteResponse;
import com.barbershop.shifts.dtos.ShiftResponse;
import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.repositories.ShiftRepositoryJpa;
import com.barbershop.shifts.services.ClientService;
import com.barbershop.shifts.services.ShiftService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Service
public class ShiftServiceImpl implements ShiftService {

    @Autowired
    private ShiftRepositoryJpa shiftRepository;

    @Autowired
    private ClientService clientService;

    @Override
    public List<ShiftResponse> getAllShifts() {
        List<Shift> shifts = shiftRepository.findAll();

        List<ShiftResponse> shiftsDtos = new ArrayList();
        for(Shift shift : shifts){
            shiftsDtos.add(convertEntityIntoDto(shift));
        }

        return shiftsDtos;
    }

    @Override
    public List<ShiftCompleteResponse> getAllCompleteShifts(){

        List<Shift> shifts = shiftRepository.findAll();

        List<ShiftCompleteResponse> shiftsConverted = new ArrayList();
        for(Shift shift : shifts){
            ClientResponse clientResponse = new ClientResponse();
            clientResponse.setId(shift.getClient().getId());
            clientResponse.setFirstName(shift.getClient().getFirstName());
            clientResponse.setLastName(shift.getClient().getLastName());
            clientResponse.setDocumentNumber(shift.getClient().getDocumentNumber());
            clientResponse.setEmail(shift.getClient().getEmail());

            ShiftCompleteResponse shiftCompleteResponse = new ShiftCompleteResponse();
            shiftCompleteResponse.setId(shift.getId());
            shiftCompleteResponse.setDatetime(shift.getDatetime());
            shiftCompleteResponse.setClient(clientResponse);
            shiftsConverted.add(shiftCompleteResponse);
        }

        return shiftsConverted;
    }

    @Override
    public ShiftResponse getShiftById(Long id) {
        Shift shift = getShiftByIdRaw(id);

        return convertEntityIntoDto(shift);
    }

    @Override
    public Shift getShiftByIdRaw(Long id) {
        if(id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        return shiftRepository.findById(id).orElseThrow(() ->
                new ResponseStatusException(HttpStatus.NOT_FOUND, "Entity not found"));
    }

    @Override
    public ShiftResponse createShift(CreationShiftRequest shiftRequest) {

        LocalDateTime dt = shiftRequest.getDatetime().withSecond(0).withNano(0);
        LocalDateTime start = dt.minusMinutes(30);
        LocalDateTime end = dt.plusMinutes(30);

        Client client = clientService.getClientByIdRaw(shiftRequest.getClientId());

        if (shiftRepository.existsByDatetimeAfterAndDatetimeBefore(start, end)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is already a shift within 30 minutes of this time"
            );
        }

        Shift shift = new Shift();
        shift.setDatetime(dt);
        shift.setClient(client);

        Shift shiftSaved = shiftRepository.save(shift);

        return convertEntityIntoDto(shiftSaved);
    }

    @Override
    public ShiftResponse updateShift(Long id, CreationShiftRequest shiftRequest){

        if(id == null || id <= 0){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid parameter");
        }

        Shift actualShift = getShiftByIdRaw(id);
        Client newClient = clientService.getClientByIdRaw(shiftRequest.getClientId());

        LocalDateTime dt = shiftRequest.getDatetime().withSecond(0).withNano(0);
        LocalDateTime start = dt.minusMinutes(30);
        LocalDateTime end = dt.plusMinutes(30);

        if (shiftRepository.existsByDatetimeAfterAndDatetimeBeforeAndIdNot(start, end, id)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "There is already a shift within 30 minutes of this time"
            );
        }

        if (Objects.equals(actualShift.getDatetime(), dt) &&
                Objects.equals(actualShift.getClient().getId(), shiftRequest.getClientId())
        ) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The new information is the same as the previous one");
        }

        actualShift.setDatetime(dt);
        actualShift.setClient(newClient);

        Shift shiftSaved = shiftRepository.save(actualShift);

        return convertEntityIntoDto(shiftSaved);
    }

    private ShiftResponse convertEntityIntoDto(Shift shift){
        ShiftResponse shiftResponse = new ShiftResponse();
        shiftResponse.setId(shift.getId());
        shiftResponse.setDatetime(shift.getDatetime());
        shiftResponse.setClientId(shift.getClient().getId());

        return shiftResponse;
    }
}
