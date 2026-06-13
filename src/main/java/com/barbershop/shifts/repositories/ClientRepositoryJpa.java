package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.Barbershop;
import com.barbershop.shifts.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepositoryJpa extends JpaRepository<Client, Long> {
    List<Client> findAllByOwner(User owner);
    Optional<Client> findByIdAndOwner(Long id, User owner);
    List<Client> findAllByBarbershop(Barbershop barbershop);
    Optional<Client> findByIdAndBarbershop(Long id, Barbershop barbershop);
    boolean existsByPhoneNumberAndBarbershop(String phoneNumber, Barbershop barbershop);
    boolean existsByPhoneNumberAndIdNotAndBarbershop(String phoneNumber, Long id, Barbershop barbershop);
}
