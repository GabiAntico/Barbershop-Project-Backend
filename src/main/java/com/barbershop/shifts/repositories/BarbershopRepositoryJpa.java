package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BarbershopRepositoryJpa extends JpaRepository<Barbershop, Long> {
}
