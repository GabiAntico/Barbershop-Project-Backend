package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Branch;
import com.barbershop.shifts.entities.EmployeeSchedule;
import com.barbershop.shifts.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;

public interface EmployeeScheduleRepository extends JpaRepository<EmployeeSchedule, Long> {
    List<EmployeeSchedule> findAllByEmployeeAndBranchOrderByDayOfWeekAscStartTimeAsc(User employee, Branch branch);
    List<EmployeeSchedule> findAllByEmployeeInAndBranch(Collection<User> employees, Branch branch);
    void deleteAllByEmployeeAndBranch(User employee, Branch branch);
}
