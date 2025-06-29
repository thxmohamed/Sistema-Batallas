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

    @Autowired
    EfectoService efectoService;

    public List<Pokemon> getAllPokemon() {
        return pokemonRepository.findAll();
    }

    public Pokemon getPokemonById(Long id) {
        Optional<Pokemon> pokemon = pokemonRepository.findById(id);
        return pokemon.orElse(null);
    }

    public Pokemon createPokemon(Pokemon pokemon) {
        // Inicializar estadísticas base al crear el Pokémon
        pokemon.setVidaBase(pokemon.getVida());
        pokemon.setAtaqueBase(pokemon.getAtaque());
        pokemon.setDefensaBase(pokemon.getDefensa());
        
        // Inicializar efectos como inactivos
        pokemon.setTieneEfectoContinuo(false);
        pokemon.setTurnosEfectoContinuo(0);
        pokemon.setIdEfectoActivo(null);
        pokemon.setAtaqueModificado(null);
        pokemon.setDefensaModificada(null);
        
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
            case "NORMAL":
                // Los ataques normales tienen efectividad neutra (1.0) contra todos los tipos
                efectividad = 1.0;
                break;
        }
        // Usar estadísticas efectivas para el cálculo de daño
        Long ataqueEfectivo = usuario.getAtaqueEfectivo();
        Long defensaEfectiva = rival.getDefensaEfectiva();
        
        Long dano = calcularDano(ataqueEfectivo, defensaEfectiva, stab, efectividad, potencia);
        System.out.println("########################");
        System.out.println("El daño es: " + dano);
        System.out.println("La efectividad es: " + efectividad);
        System.out.println("El stab es: " + stab);
        System.out.println("La potencia es: " + potencia);
        System.out.println("El ataque efectivo es: " + ataqueEfectivo);
        System.out.println("La defensa efectiva es: " + defensaEfectiva);

        rival.setVida(Math.max(0, rival.getVida() - dano));
        return rival;
    }

    public Pokemon aplicarEfecto(Pokemon usuario, Pokemon rival, Efecto efecto) {
        String tipoEfecto = String.valueOf(efecto.getTipoEfecto());
        Pokemon usuarioDB = getPokemonById(usuario.getId());
        Pokemon rivalDB = getPokemonById(rival.getId());
        
        // Inicializar estadísticas base si no existen
        usuario.inicializarEstadisticasBase();
        rival.inicializarEstadisticasBase();
        
        Long vidaMaxRival = rivalDB.getVida();
        Long vidaMaxUsuario = usuarioDB.getVida();
        
        switch (tipoEfecto) {
            case "DANO_CONTINUO":
                // Aplicar daño inmediato
                long danoContinuo = (long) (vidaMaxRival * efecto.getMultiplicador());
                rival.setVida(Math.max(0, rival.getVida() - danoContinuo));
                
                // Configurar efecto continuo
                rival.setTieneEfectoContinuo(true);
                rival.setTurnosEfectoContinuo(4); // 4 turnos de duración
                rival.setIdEfectoActivo(efecto.getId());
                return rival;

            case "SUBIR_ATAQUE_PROPIO":
                // Calcular nuevo ataque basado en el ataque base
                long nuevoAtaque = (long) (usuario.getAtaqueBase() * (1.0 + efecto.getMultiplicador()));
                usuario.setAtaqueModificado(nuevoAtaque);
                usuario.setAtaque(nuevoAtaque); // También actualizar el ataque actual
                usuario.setIdEfectoActivo(efecto.getId());
                return usuario;

            case "SUBIR_DEFENSA_PROPIO":
                // Calcular nueva defensa basada en la defensa base
                long nuevaDefensa = (long) (usuario.getDefensaBase() * (1.0 + efecto.getMultiplicador()));
                usuario.setDefensaModificada(nuevaDefensa);
                usuario.setDefensa(nuevaDefensa); // También actualizar la defensa actual
                usuario.setIdEfectoActivo(efecto.getId());
                return usuario;

            case "SUBIR_VIDA":
                // Para efectos de curación, usar el multiplicador como porcentaje de vida máxima
                // Si multiplicador es 0, usar 50% por defecto (0.5)
                double porcentajeCuracion = efecto.getMultiplicador() == 0 ? 0.5 : efecto.getMultiplicador();
                long vidaRecuperada = (long) (vidaMaxUsuario * porcentajeCuracion);
                long vidaNueva = Math.min(usuario.getVida() + vidaRecuperada, vidaMaxUsuario);
                usuario.setVida(vidaNueva);
                
                System.out.println("=== EFECTO CURACIÓN ===");
                System.out.println("Vida máxima: " + vidaMaxUsuario);
                System.out.println("Vida actual: " + (usuario.getVida() - vidaRecuperada));
                System.out.println("Porcentaje curación: " + (porcentajeCuracion * 100) + "%");
                System.out.println("Vida recuperada: " + vidaRecuperada);
                System.out.println("Vida final: " + vidaNueva);
                
                return usuario;

            case "BAJAR_DEFENSA_RIVAL":
                // Calcular nueva defensa basada en la defensa base
                long defensaReducida = (long) (rival.getDefensaBase() * (1.0 - efecto.getMultiplicador()));
                rival.setDefensaModificada(defensaReducida);
                rival.setDefensa(defensaReducida); // También actualizar la defensa actual
                rival.setIdEfectoActivo(efecto.getId());
                return rival;

            case "BAJAR_ATAQUE_RIVAL":
                // Calcular nuevo ataque basado en el ataque base
                long ataqueReducido = (long) (rival.getAtaqueBase() * (1.0 - efecto.getMultiplicador()));
                rival.setAtaqueModificado(ataqueReducido);
                rival.setAtaque(ataqueReducido); // También actualizar el ataque actual
                rival.setIdEfectoActivo(efecto.getId());
                return rival;

            // Casos legacy de velocidad - convertir a efectos equivalentes
            case "SUBIR_VELOCIDAD_PROPIO":
                // Convertir a efecto de ataque (velocidad -> prioridad de ataque)
                long ataqueVelocidad = (long) (usuario.getAtaqueBase() * (1.0 + efecto.getMultiplicador()));
                usuario.setAtaqueModificado(ataqueVelocidad);
                usuario.setAtaque(ataqueVelocidad);
                usuario.setIdEfectoActivo(efecto.getId());
                return usuario;

            case "BAJAR_VELOCIDAD_RIVAL":
                // Convertir a efecto de defensa (velocidad -> capacidad de esquivar)
                long defensaVelocidad = (long) (rival.getDefensaBase() * (1.0 - efecto.getMultiplicador()));
                rival.setDefensaModificada(defensaVelocidad);
                rival.setDefensa(defensaVelocidad);
                rival.setIdEfectoActivo(efecto.getId());
                return rival;

            default:
                throw new IllegalArgumentException("Efecto desconocido: " + efecto.getTipoEfecto());
        }
    }

    public void procesarEfectosContinuos(Pokemon pokemon) {
        if (pokemon.isTieneEfectoContinuo() && pokemon.getTurnosEfectoContinuo() > 0) {
            // Reducir contador de turnos
            pokemon.setTurnosEfectoContinuo(pokemon.getTurnosEfectoContinuo() - 1);
            
            // Aplicar daño continuo si el efecto sigue activo
            if (pokemon.getTurnosEfectoContinuo() > 0 && pokemon.getIdEfectoActivo() != null) {
                Efecto efecto = efectoService.findEfectoById(pokemon.getIdEfectoActivo());
                if (efecto != null && efecto.getTipoEfecto() == Efecto.tipoEfecto.DANO_CONTINUO) {
                    Pokemon pokemonOriginal = getPokemonById(pokemon.getId());
                    long danoContinuo = (long) (pokemonOriginal.getVida() * efecto.getMultiplicador());
                    pokemon.setVida(Math.max(0, pokemon.getVida() - danoContinuo));
                }
            } else {
                // El efecto ha terminado
                pokemon.setTieneEfectoContinuo(false);
                pokemon.setIdEfectoActivo(null);
            }
        }
    }

    public List<Pokemon> obtenerPokemonPorTipo(Pokemon.TipoPokemon tipoPokemon) {
        return pokemonRepository.findByTipoPokemon(tipoPokemon);
    }

}
