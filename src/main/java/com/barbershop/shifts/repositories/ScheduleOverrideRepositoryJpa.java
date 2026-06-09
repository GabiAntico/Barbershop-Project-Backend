package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.ScheduleOverride;
import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleOverrideRepositoryJpa extends JpaRepository<ScheduleOverride, Long> {
    Optional<ScheduleOverride> findByDate(LocalDate date);
    List<ScheduleOverride> findByDateBetween(LocalDate startDate, LocalDate endDate);
    Optional<ScheduleOverride> findByOwnerAndDate(User owner, LocalDate date);
    List<ScheduleOverride> findByOwnerAndDateBetween(User owner, LocalDate startDate, LocalDate endDate);
    Optional<ScheduleOverride> findByBranchAndDate(Branch branch, LocalDate date);
    List<ScheduleOverride> findByBranchAndDateBetween(Branch branch, LocalDate startDate, LocalDate endDate);
}
