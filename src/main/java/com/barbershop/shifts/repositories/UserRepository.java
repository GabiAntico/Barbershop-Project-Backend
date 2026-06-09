package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.User;
import com.barbershop.shifts.entities.Barbershop;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    List<User> findAllByBarbershop(Barbershop barbershop);
    Optional<User> findByIdAndBarbershop(Long id, Barbershop barbershop);
}
