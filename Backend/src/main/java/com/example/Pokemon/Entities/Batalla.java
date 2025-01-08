package com.example.Pokemon.Entities;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "batalla")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Batalla {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long idEntrenador1;
    private Long idEntrenador2;
    private int turno;
}
