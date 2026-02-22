package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Shift;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
public interface ShiftRepositoryJpa extends JpaRepository<Shift, Long> {
    Optional<Shift> findByDatetime(LocalDateTime dateTime);
    boolean existsByDatetimeAfterAndDatetimeBeforeAndIdNot(LocalDateTime start, LocalDateTime end, Long id);
    boolean existsByDatetimeAfterAndDatetimeBefore(LocalDateTime start, LocalDateTime end);

}
