package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Repositories.AtaqueRepository;
import com.example.Pokemon.Repositories.EfectoRepository;
import com.example.Pokemon.Repositories.PokemonRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.Random;

@Service
@Transactional
public class PokemonService {
    @Autowired
    PokemonRepository pokemonRepository;

    @Autowired
    AtaqueRepository ataqueRepository;

    @Autowired
    EfectoRepository efectoRepository;

    public List<Pokemon> getAllPokemon() {
        return pokemonRepository.findAll();
    }

    public Pokemon getPokemonById(Long id) {
        Optional<Pokemon> pokemon = pokemonRepository.findById(id);
        return pokemon.orElse(null);
    }

    public Pokemon createPokemon(Pokemon pokemon) {
        return pokemonRepository.save(pokemon);
    }

    public Long calcularDano(Long ataqueAgresor, Long defensaAgredido, double stab, double efectividad, int potencia) {

        Random random = new Random();
        int danoAleatorio = random.nextInt(84, 101);
        System.out.println("El daño aleatorio es: " + danoAleatorio);

        //double danoBase = (ataqueAgresor * potencia * stab * efectividad) /(1 + ((double) defensaAgredido * 2));
        //long danoTotal = (long) Math.floor(danoBase + danoAleatorio);
        //return Math.max(1,danoTotal);

        double danoBase = 0.01 * danoAleatorio * stab * efectividad * ((double) (11 * ataqueAgresor * potencia) /(25*defensaAgredido) + 2);
        long danoTotal = (long) Math.floor(danoBase);
        return Math.max(1, danoTotal);

    }

    public List<Ataque> getPokemonAtaques(Pokemon pokemon) {
        List<Ataque> ataques = new ArrayList<>();
        Optional<Ataque> ataque1 =  ataqueRepository.findById(pokemon.getIdAtaque1());
        Optional<Ataque> ataque2 =  ataqueRepository.findById(pokemon.getIdAtaque2());
        if(ataque1.isPresent() && ataque2.isPresent()){
            ataques.add(ataque1.get());
            ataques.add(ataque2.get());
            return ataques;
        }
        return ataques;
    }

    public Efecto getPokemonEfecto(Pokemon pokemon) {
        Optional<Efecto> efecto = efectoRepository.findById(pokemon.getIdEfecto());
        return efecto.orElse(null);
    }

    public Pokemon atacar(Pokemon usuario, Pokemon rival, Ataque ataque) {
        double stab = 1.0;
        double efectividad = 1.0;
        int potencia = ataque.getPotencia();
        String tipoUsuario = String.valueOf(usuario.getTipoPokemon());
        String tipoRival = String.valueOf(rival.getTipoPokemon());
        String tipoAtaque = String.valueOf(ataque.getTipoAtaque());

        if(tipoUsuario.equals(tipoAtaque)){
            stab = 1.5;
        }
        // Calcular efectividad
        switch (tipoAtaque) {
            case "AGUA":
                if (tipoRival.equals("FUEGO")) {
                    efectividad = 2.0;
                } else if (tipoRival.equals("ELECTRICO") || tipoRival.equals("AGUA")) {
                    efectividad = 0.5;
                }
                break;
            case "FUEGO":
                if (tipoRival.equals("PLANTA")) {
                    efectividad = 2.0;
                } else if (tipoRival.equals("AGUA") || tipoRival.equals("FUEGO")) {
                    System.out.println("holaaaaaaa");
                    efectividad = 0.5;
                }
                break;
            case "PLANTA":
                if (tipoRival.equals("TIERRA")) {
                    efectividad = 2.0;
                } else if (tipoRival.equals("FUEGO") || tipoRival.equals("PLANTA")) {
                    efectividad = 0.5;
                }
                break;
            case "TIERRA":
                if (tipoRival.equals("ELECTRICO")) {
                    efectividad = 2.0;
                } else if (tipoRival.equals("PLANTA") || tipoRival.equals("TIERRA")) {
                    efectividad = 0.5;
                }
                break;
            case "ELECTRICO":
                if (tipoRival.equals("AGUA")) {
                    efectividad = 2.0;
                } else if (tipoRival.equals("TIERRA") || tipoRival.equals("ELECTRICO")) {
                    efectividad = 0.5;
                }
                break;
        }
        Long dano = calcularDano(usuario.getAtaque(), rival.getDefensa(), stab, efectividad, potencia);
        System.out.println("########################");
        System.out.println("El daño es: " + dano);
        System.out.println("La efectividad es: " + efectividad);
        System.out.println("El stab es: " + stab);
        System.out.println("La potencia es: " + potencia);
        System.out.println("El ataque es: " + usuario.getAtaque());
        System.out.println("La defensa es: " + rival.getDefensa());

        rival.setVida(Math.max(0, rival.getVida() - dano));
        return rival;
    }

    public Pokemon aplicarEfecto(Pokemon usuario, Pokemon rival, Efecto efecto) {
        String tipoEfecto = String.valueOf(efecto.getTipoEfecto());
        Pokemon usuarioDB = getPokemonById(usuario.getId());
        Pokemon rivalDB = getPokemonById(rival.getId());
        Long vidaMaxRival = rivalDB.getVida();
        Long vidaMaxUsuario = usuarioDB.getVida();
        switch (tipoEfecto) {
            case "DANO_CONTINUO":
                long danoContinuo = (long) (vidaMaxRival * efecto.getMultiplicador());
                rival.setVida(Math.max(0, rival.getVida() - danoContinuo));
                return rival;

            case "SUBIR_ATAQUE_PROPIO":
                usuario.setAtaque((long) (usuario.getAtaque() * efecto.getMultiplicador()));
                return usuario;

            case "SUBIR_DEFENSA_PROPIO":
                usuario.setDefensa((long) (usuario.getDefensa() * efecto.getMultiplicador()));
                return usuario;

            case "SUBIR_VELOCIDAD_PROPIO":
                usuario.setVelocidad((long) (usuario.getVelocidad() * efecto.getMultiplicador()));
                return usuario;

            case "SUBIR_VIDA":
                long vidaNueva = Math.min(usuario.getVida() + 50, vidaMaxUsuario); // Valor fijo de 50 puntos
                usuario.setVida(vidaNueva);
                return usuario;

            case "BAJAR_DEFENSA_RIVAL":
                rival.setDefensa((long) (rival.getDefensa() * efecto.getMultiplicador()));
                return rival;

            case "BAJAR_ATAQUE_RIVAL":
                rival.setAtaque((long) (rival.getAtaque() * efecto.getMultiplicador()));
                return rival;

            case "BAJAR_VELOCIDAD_RIVAL":
                if (rival != null) {
                    rival.setVelocidad((long) (rival.getVelocidad() * efecto.getMultiplicador()));
                }
                return rival;

            default:
                throw new IllegalArgumentException("Efecto desconocido: " + efecto.getTipoEfecto());

        }
    }

    public List<Pokemon> obtenerPokemonPorTipo(Pokemon.TipoPokemon tipoPokemon) {
        return pokemonRepository.findByTipoPokemon(tipoPokemon);
    }

}
