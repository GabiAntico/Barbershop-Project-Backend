package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Client;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ClientRepositoryJpa extends JpaRepository<Client, Long> {
    boolean existsByEmail(String email);
}
