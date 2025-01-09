package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Entrenador;
import com.example.Pokemon.Repositories.EntrenadorRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EntrenadorService {
    @Autowired
    EntrenadorRepository entrenadorRepository;

    public List<Entrenador> findAll() {
        return entrenadorRepository.findAll();
    }
    public Entrenador findById(Long id) {
        Optional<Entrenador> entrenador = entrenadorRepository.findById(id);
        return entrenador.orElse(null);
    }

    public Entrenador createEntrenador(Entrenador entrenador) {
        return entrenadorRepository.save(entrenador);
    }
}
