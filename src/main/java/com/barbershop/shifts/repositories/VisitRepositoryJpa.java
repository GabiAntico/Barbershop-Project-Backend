package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Visit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface VisitRepositoryJpa extends JpaRepository<Visit, Long> {
}
