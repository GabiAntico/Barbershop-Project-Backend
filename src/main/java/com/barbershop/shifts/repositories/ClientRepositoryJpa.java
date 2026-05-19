package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.Client;
import com.barbershop.shifts.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ClientRepositoryJpa extends JpaRepository<Client, Long> {
    List<Client> findAllByOwner(User owner);
    Optional<Client> findByIdAndOwner(Long id, User owner);
    boolean existsByEmailAndOwner(String email, User owner);
    boolean existsByEmailAndIdNotAndOwner(String email, Long id, User owner);
}
