package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Batalla;
import com.example.Pokemon.Repositories.BatallaRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class BatallaService {
    @Autowired
    BatallaRepository batallaRepository;

    public List<Batalla> getBatallas() {
        return batallaRepository.findAll();
    }

    public Batalla getBatallaById(Long id) {
        Optional<Batalla> batalla = batallaRepository.findById(id);
        return batalla.orElse(null);
    }

    public Batalla createBatalla(Batalla batalla) {
        return batallaRepository.save(batalla);
    }

}
