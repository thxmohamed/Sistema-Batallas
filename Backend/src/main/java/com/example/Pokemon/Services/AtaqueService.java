package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Repositories.AtaqueRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class AtaqueService {
    @Autowired
    AtaqueRepository ataqueRepository;

    public Ataque createAtaque(Ataque ataque) {
        return ataqueRepository.save(ataque);
    }

    public List<Ataque> getAllAtaques() {
        return ataqueRepository.findAll();
    }

    public Ataque getAtaqueById(Long id) {
        Optional<Ataque> ataque = ataqueRepository.findById(id);
        return ataque.orElse(null);
    }


}
