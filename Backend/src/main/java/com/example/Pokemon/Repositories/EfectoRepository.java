package com.example.Pokemon.Repositories;

import com.example.Pokemon.Entities.Efecto;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface EfectoRepository extends JpaRepository<Efecto, Long> {
    List<Efecto> findByTipoEfecto(Efecto.tipoEfecto tipoEfecto);
}
