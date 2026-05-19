package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.AppSettings;
import com.barbershop.shifts.entities.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface AppSettingsRepositoryJpa extends JpaRepository<AppSettings, Long> {
    Optional<AppSettings> findByOwner(User owner);
}
