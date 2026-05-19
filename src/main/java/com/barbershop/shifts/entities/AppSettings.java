package com.barbershop.shifts.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "app_settings")
@Data
public class AppSettings {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "default_estimated_amount", precision = 10, scale = 2)
    private BigDecimal defaultEstimatedAmount;

    @Column(name = "default_schedule_slots", length = 1000)
    private String defaultScheduleSlots;

    @OneToOne
    @JoinColumn(name = "owner_id")
    private User owner;
}
