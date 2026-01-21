package com.example.booking.entity;

import jakarta.persistence.*;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "units")
public class Unit {

    @Id
    @GeneratedValue
    private UUID id;

    private int rooms;

    @Enumerated(EnumType.STRING)
    private AccommodationType type;

    private int floor;

    private BigDecimal baseCost;

    private String description;

    private boolean active = true;
}
