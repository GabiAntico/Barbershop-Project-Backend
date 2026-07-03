package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.ScheduleWeeklyDefault;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.DayOfWeek;
import java.util.List;
import java.util.Optional;

@Repository
public interface ScheduleWeeklyDefaultRepositoryJpa extends JpaRepository<ScheduleWeeklyDefault, Long> {
    List<ScheduleWeeklyDefault> findAllByBranch(Branch branch);
    Optional<ScheduleWeeklyDefault> findByBranchAndDayOfWeek(Branch branch, DayOfWeek dayOfWeek);
}
