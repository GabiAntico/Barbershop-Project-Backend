package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Barbershop;
import com.barbershop.shifts.entities.Branch;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface BranchRepositoryJpa extends JpaRepository<Branch, Long> {
    List<Branch> findAllByBarbershop(Barbershop barbershop);
    Optional<Branch> findByIdAndBarbershop(Long id, Barbershop barbershop);
}
