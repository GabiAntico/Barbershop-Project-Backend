package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.ScheduleOverride;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleOverrideRepositoryJpa extends JpaRepository<ScheduleOverride, Long> {
    Optional<ScheduleOverride> findByDate(LocalDate date);
    List<ScheduleOverride> findByDateBetween(LocalDate startDate, LocalDate endDate);
}
