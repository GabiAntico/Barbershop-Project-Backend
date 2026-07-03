package com.barbershop.shifts.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.ZoneId;

@Entity
@Table(name = "branches")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Branch {
    public static final String DEFAULT_TIME_ZONE = "America/Argentina/Buenos_Aires";

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "address")
    private String address;

    @Column(name = "time_zone", nullable = false, length = 64)
    private String timeZone = DEFAULT_TIME_ZONE;

    @ManyToOne
    @JoinColumn(name = "barbershop_id", nullable = false)
    private Barbershop barbershop;

    public ZoneId resolveZoneId() {
        try {
            return ZoneId.of(timeZone == null || timeZone.isBlank() ? DEFAULT_TIME_ZONE : timeZone);
        } catch (RuntimeException e) {
            return ZoneId.of(DEFAULT_TIME_ZONE);
        }
    }
}
