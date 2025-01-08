package com.example.Pokemon.Repositories;

import com.example.Pokemon.Entities.Entrenador;
import org.springframework.data.jpa.repository.JpaRepository;

public interface EntrenadorRepository extends JpaRepository<Entrenador, Long> {
}
