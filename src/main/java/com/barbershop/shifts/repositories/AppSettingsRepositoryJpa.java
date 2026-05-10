package com.barbershop.shifts.repositories;

import com.barbershop.shifts.entities.AppSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AppSettingsRepositoryJpa extends JpaRepository<AppSettings, Long> {
}
