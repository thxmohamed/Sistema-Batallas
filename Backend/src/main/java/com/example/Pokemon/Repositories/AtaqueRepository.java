package com.example.Pokemon.Repositories;

import com.example.Pokemon.Entities.Ataque;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AtaqueRepository extends JpaRepository<Ataque, Long> {
}
