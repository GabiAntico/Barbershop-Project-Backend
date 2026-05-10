package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepositoryJpa extends JpaRepository<Shift, Long> {
    Optional<Shift> findByDatetime(LocalDateTime dateTime);
    boolean existsByDatetimeAfterAndDatetimeBeforeAndIdNotAndStatusIn(
            LocalDateTime start,
            LocalDateTime end,
            Long id,
            Collection<ShiftStatus> statuses
    );
    boolean existsByDatetimeAfterAndDatetimeBeforeAndStatusIn(
            LocalDateTime start,
            LocalDateTime end,
            Collection<ShiftStatus> statuses
    );
    List<Shift> findByDatetimeBetweenAndStatusIn(
            LocalDateTime start,
            LocalDateTime end,
            Collection<ShiftStatus> statuses
    );


}
