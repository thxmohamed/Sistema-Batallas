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

    // Método para migrar efectos de velocidad obsoletos
    public void migrateVelocityEffects() {
        List<Efecto> efectos = efectoRepository.findAll();
        boolean needsUpdate = false;

        for (Efecto efecto : efectos) {
            if (efecto.getTipoEfecto() == Efecto.tipoEfecto.SUBIR_VELOCIDAD_PROPIO) {
                efecto.setTipoEfecto(Efecto.tipoEfecto.SUBIR_ATAQUE_PROPIO);
                efecto.setNombre(efecto.getNombre().replace("Velocidad", "Ataque"));
                efecto.setDescripcion(efecto.getDescripcion().replace("velocidad", "ataque"));
                efectoRepository.save(efecto);
                needsUpdate = true;
                System.out.println("Migrated effect: " + efecto.getNombre() + " from SUBIR_VELOCIDAD_PROPIO to SUBIR_ATAQUE_PROPIO");
            }

            if (efecto.getTipoEfecto() == Efecto.tipoEfecto.BAJAR_VELOCIDAD_RIVAL) {
                efecto.setTipoEfecto(Efecto.tipoEfecto.BAJAR_DEFENSA_RIVAL);
                efecto.setNombre(efecto.getNombre().replace("Velocidad", "Defensa"));
                efecto.setDescripcion(efecto.getDescripcion().replace("velocidad", "defensa"));
                efectoRepository.save(efecto);
                needsUpdate = true;
                System.out.println("Migrated effect: " + efecto.getNombre() + " from BAJAR_VELOCIDAD_RIVAL to BAJAR_DEFENSA_RIVAL");
            }
        }

        if (needsUpdate) {
            System.out.println("Effect migration completed successfully!");
        }
    }
}
