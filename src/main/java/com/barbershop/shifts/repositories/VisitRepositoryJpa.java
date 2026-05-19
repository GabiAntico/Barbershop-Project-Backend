package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Visit;
import com.barbershop.shifts.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.time.LocalDateTime;

@Repository
public interface VisitRepositoryJpa extends JpaRepository<Visit, Long> {
    List<Visit> findAllByShiftOwner(User owner);
    Optional<Visit> findByIdAndShiftOwner(Long id, User owner);
    List<Visit> findByShiftDatetimeBetweenAndShiftOwner(LocalDateTime start, LocalDateTime end, User owner);
}
