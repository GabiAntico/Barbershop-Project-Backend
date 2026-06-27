package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Barbershop;
import com.barbershop.shifts.entities.ResponsibleContact;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ResponsibleContactRepositoryJpa extends JpaRepository<ResponsibleContact, Long> {
    Optional<ResponsibleContact> findFirstByPhoneNumberAndBarbershop(String phoneNumber, Barbershop barbershop);
}
