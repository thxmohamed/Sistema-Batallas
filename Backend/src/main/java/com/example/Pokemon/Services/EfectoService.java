package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Repositories.EfectoRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class EfectoService {
    @Autowired
    EfectoRepository efectoRepository;

    public Efecto createEfecto(Efecto efecto) {
        return efectoRepository.save(efecto);
    }
    public List<Efecto> getAllEfectos() {
        return efectoRepository.findAll();
    }
    public Efecto findEfectoById(Long id) {
        Optional<Efecto> efecto = efectoRepository.findById(id);
        return efecto.orElse(null);
    }
}
