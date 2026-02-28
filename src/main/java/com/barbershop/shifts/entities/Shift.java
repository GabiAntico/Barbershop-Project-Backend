package com.barbershop.shifts.entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "shifts")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Shift {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "datetime")
    private LocalDateTime datetime;

    @ManyToOne
    @JoinColumn(name = "client_id")
    private Client client;

    @Column(name = "status")
    @Enumerated(EnumType.STRING)
    private ShiftStatus status;

    @OneToOne(mappedBy = "shift")
    private Visit visit;
}
