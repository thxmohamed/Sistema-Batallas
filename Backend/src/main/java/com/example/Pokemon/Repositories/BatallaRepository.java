package com.example.Pokemon.Repositories;

import com.example.Pokemon.Entities.Batalla;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface BatallaRepository extends JpaRepository<Batalla, Long> {
}
