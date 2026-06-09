package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Visit;
import com.barbershop.shifts.entities.Branch;
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
    List<Visit> findAllByShiftBranch(Branch branch);
    Optional<Visit> findByIdAndShiftBranch(Long id, Branch branch);
    List<Visit> findByShiftDatetimeBetweenAndShiftOwner(LocalDateTime start, LocalDateTime end, User owner);
    List<Visit> findByShiftDatetimeBetweenAndShiftBranch(LocalDateTime start, LocalDateTime end, Branch branch);
    List<Visit> findByShiftClientIdAndShiftDatetimeBetweenAndShiftOwner(
            Long clientId,
            LocalDateTime start,
            LocalDateTime end,
            User owner
    );
    List<Visit> findByShiftClientIdAndShiftOwner(Long clientId, User owner);
    List<Visit> findByShiftClientIdAndShiftDatetimeBetweenAndShiftBranch(
            Long clientId,
            LocalDateTime start,
            LocalDateTime end,
            Branch branch
    );
    List<Visit> findByShiftClientIdAndShiftBranch(Long clientId, Branch branch);
}
