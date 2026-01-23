package com.example.booking.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "units")
public class Unit {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    private int rooms;

    @Enumerated(EnumType.STRING)
    private AccommodationType type;

    private int floor;

    private BigDecimal baseCost;

    private String description;

    @OneToMany(mappedBy = "unit")
    private List<Booking> bookings;
}
