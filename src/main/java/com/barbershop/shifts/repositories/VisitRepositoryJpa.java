package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Visit;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitRepositoryJpa extends JpaRepository<Visit, Long> {
}
