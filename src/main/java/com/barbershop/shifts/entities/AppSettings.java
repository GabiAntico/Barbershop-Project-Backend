package com.barbershop.shifts.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.math.BigDecimal;

@Entity
@Table(name = "app_settings")
@Data
public class AppSettings {
    @Id
    private Long id;

    @Column(name = "default_estimated_amount", precision = 10, scale = 2)
    private BigDecimal defaultEstimatedAmount;
}
