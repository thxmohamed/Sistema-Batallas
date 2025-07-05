package com.example.Pokemon.Repositories;

import com.example.Pokemon.Entities.Ataque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AtaqueRepository extends JpaRepository<Ataque, Long> {
    List<Ataque> findByTipoAtaque(Ataque.TipoAtaque tipoAtaque);
}
