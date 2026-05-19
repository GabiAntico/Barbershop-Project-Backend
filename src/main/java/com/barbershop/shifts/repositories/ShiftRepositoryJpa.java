package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Shift;
import com.barbershop.shifts.entities.ShiftStatus;
import com.barbershop.shifts.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

@Repository
public interface ShiftRepositoryJpa extends JpaRepository<Shift, Long> {
    Optional<Shift> findByDatetime(LocalDateTime dateTime);
    List<Shift> findAllByOwner(User owner);
    Optional<Shift> findByIdAndOwner(Long id, User owner);
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
    boolean existsByDatetimeAfterAndDatetimeBeforeAndStatusInAndOwner(
            LocalDateTime start,
            LocalDateTime end,
            Collection<ShiftStatus> statuses,
            User owner
    );
    boolean existsByDatetimeAfterAndDatetimeBeforeAndIdNotAndStatusInAndOwner(
            LocalDateTime start,
            LocalDateTime end,
            Long id,
            Collection<ShiftStatus> statuses,
            User owner
    );
    List<Shift> findByDatetimeBetweenAndStatusIn(
            LocalDateTime start,
            LocalDateTime end,
            Collection<ShiftStatus> statuses
    );
    List<Shift> findByDatetimeBetweenAndStatusInAndOwner(
            LocalDateTime start,
            LocalDateTime end,
            Collection<ShiftStatus> statuses,
            User owner
    );
    List<Shift> findByDatetimeBetweenAndOwner(
            LocalDateTime start,
            LocalDateTime end,
            User owner
    );
    boolean existsByDatetimeBetweenAndStatusIn(
            LocalDateTime start,
            LocalDateTime end,
            Collection<ShiftStatus> statuses
    );
    boolean existsByDatetimeBetweenAndStatusInAndOwner(
            LocalDateTime start,
            LocalDateTime end,
            Collection<ShiftStatus> statuses,
            User owner
    );


}
