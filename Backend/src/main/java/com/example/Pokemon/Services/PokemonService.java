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
        
        // Inicializar modificadores con los valores actuales
        pokemon.setAtaqueModificado(pokemon.getAtaque());
        pokemon.setDefensaModificada(pokemon.getDefensa());
        
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
        
        // Intentar obtener de BD, si no existe usar los valores actuales
        Pokemon usuarioDB = null;
        
        try {
            usuarioDB = getPokemonById(usuario.getId());
        } catch (Exception e) {
            // En caso de error (ej. pruebas unitarias), usar los valores actuales
        }
        
        // Inicializar estadísticas base si no existen (sin tocar modificadores)
        usuario.inicializarSoloEstadisticasBase();
        rival.inicializarSoloEstadisticasBase();
        
        // Usar vida máxima de BD si está disponible, sino usar vida base actual
        Long vidaMaxUsuario = (usuarioDB != null) ? usuarioDB.getVida() : usuario.getVidaBase();
        
        switch (tipoEfecto) {
            case "DANO_CONTINUO":
                // IMPORTANTE: Para DANO_CONTINUO, solo retornamos el rival sin modificar
                // El efecto será manejado a nivel de equipo en BatallaService
                // Solo marcamos que el efecto se ha aplicado para logs
                System.out.println("=== EFECTO DANO_CONTINUO APLICADO ===");
                System.out.println("Efecto será aplicado a todo el equipo rival en futuros turnos");
                return rival;

            case "SUBIR_ATAQUE_PROPIO":
                // Inicializar estadísticas base si es necesario (sin tocar modificadores)
                usuario.inicializarSoloEstadisticasBase();
                
                // Aplicar el efecto sobre el ataque modificado actual para permitir acumulación
                long ataqueActual = usuario.getAtaqueEfectivo();
                long incrementoAtaque = (long) (usuario.getAtaqueBase() * (efecto.getMultiplicador() - 1.0));
                long nuevoAtaque = ataqueActual + incrementoAtaque;
                
                usuario.setAtaqueModificado(nuevoAtaque);
                // NO actualizar el ataque base
                usuario.setIdEfectoActivo(efecto.getId());
                
                System.out.println("=== EFECTO SUBIR ATAQUE ===");
                System.out.println("Ataque base: " + usuario.getAtaqueBase());
                System.out.println("Ataque actual antes: " + ataqueActual);
                System.out.println("Multiplicador: " + efecto.getMultiplicador());
                System.out.println("Incremento: " + incrementoAtaque);
                System.out.println("Nuevo ataque: " + nuevoAtaque);
                
                return usuario;

            case "SUBIR_DEFENSA_PROPIO":
                // Inicializar estadísticas base si es necesario (sin tocar modificadores)
                usuario.inicializarSoloEstadisticasBase();
                
                // Aplicar el efecto sobre la defensa modificada actual para permitir acumulación
                long defensaActual = usuario.getDefensaEfectiva();
                long incrementoDefensa = (long) (usuario.getDefensaBase() * (efecto.getMultiplicador() - 1.0));
                long nuevaDefensa = defensaActual + incrementoDefensa;
                
                usuario.setDefensaModificada(nuevaDefensa);
                // NO actualizar la defensa base
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
                // IMPORTANTE: Para BAJAR_DEFENSA_RIVAL, solo retornamos el rival sin modificar
                // El efecto será manejado a nivel de equipo en BatallaService
                // Solo marcamos que el efecto se ha aplicado para logs
                System.out.println("=== EFECTO BAJAR_DEFENSA_RIVAL APLICADO ===");
                System.out.println("Efecto será aplicado a todo el equipo rival inmediatamente");
                return rival;

            case "BAJAR_ATAQUE_RIVAL":
                // IMPORTANTE: Para BAJAR_ATAQUE_RIVAL, solo retornamos el rival sin modificar
                // El efecto será manejado a nivel de equipo en BatallaService
                // Solo marcamos que el efecto se ha aplicado para logs
                System.out.println("=== EFECTO BAJAR_ATAQUE_RIVAL APLICADO ===");
                System.out.println("Efecto será aplicado a todo el equipo rival inmediatamente");
                return rival;

            // Casos legacy de velocidad - convertir a efectos equivalentes
            case "SUBIR_VELOCIDAD_PROPIO":
                // Convertir a efecto de ataque (velocidad -> prioridad de ataque)
                usuario.inicializarSoloEstadisticasBase();
                long ataqueActualVel = usuario.getAtaqueEfectivo();
                long incrementoAtaqueVel = (long) (usuario.getAtaqueBase() * (efecto.getMultiplicador() - 1.0));
                long ataqueVelocidad = ataqueActualVel + incrementoAtaqueVel;
                usuario.setAtaqueModificado(ataqueVelocidad);
                usuario.setIdEfectoActivo(efecto.getId());
                return usuario;

            case "BAJAR_VELOCIDAD_RIVAL":
                // Convertir a efecto de defensa (velocidad -> capacidad de esquivar)
                rival.inicializarSoloEstadisticasBase();
                long defensaActualVel = rival.getDefensaEfectiva();
                long reduccionDefensaVel = (long) (rival.getDefensaBase() * (1.0 - efecto.getMultiplicador()));
                long defensaVelocidad = defensaActualVel - reduccionDefensaVel;
                rival.setDefensaModificada(Math.max(50, defensaVelocidad));
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

    /**
     * Aplica daño continuo a todo un equipo de pokémon
     * @param equipo Lista de pokémon del equipo afectado
     * @param efecto Efecto de daño continuo a aplicar
     */
    public void aplicarDanoContinuoEquipo(List<Pokemon> equipo, Efecto efecto) {
        if (efecto.getTipoEfecto() != Efecto.tipoEfecto.DANO_CONTINUO) {
            return;
        }
        
        System.out.println("=== APLICANDO DAÑO CONTINUO A TODO EL EQUIPO ===");
        System.out.println("Multiplicador: " + efecto.getMultiplicador());
        
        for (Pokemon pokemon : equipo) {
            if (pokemon.getVida() > 0) { // Solo aplicar a pokémon vivos
                try {
                    // Obtener la vida máxima original del pokémon
                    Pokemon pokemonOriginal = getPokemonById(pokemon.getId());
                    long vidaMaxima = pokemonOriginal.getVida();
                    
                    // Calcular daño como porcentaje de vida máxima
                    long danoContinuo = (long) (vidaMaxima * efecto.getMultiplicador());
                    long vidaAnterior = pokemon.getVida();
                    long vidaNueva = Math.max(0, pokemon.getVida() - danoContinuo);
                    
                    pokemon.setVida(vidaNueva);
                    
                    System.out.println("Pokémon: " + pokemon.getNombre() + 
                                     " | Vida anterior: " + vidaAnterior + 
                                     " | Daño: " + danoContinuo + 
                                     " | Vida nueva: " + vidaNueva);
                } catch (Exception e) {
                    System.err.println("Error aplicando daño continuo a " + pokemon.getNombre() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Aplica reducción de ataque a todo un equipo de pokémon rival
     * @param equipo Lista de pokémon del equipo rival afectado
     * @param efecto Efecto de reducción de ataque a aplicar
     */
    public void aplicarReduccionAtaqueEquipo(List<Pokemon> equipo, Efecto efecto) {
        if (efecto.getTipoEfecto() != Efecto.tipoEfecto.BAJAR_ATAQUE_RIVAL) {
            return;
        }
        
        System.out.println("=== APLICANDO REDUCCIÓN DE ATAQUE A TODO EL EQUIPO RIVAL ===");
        System.out.println("Multiplicador: " + efecto.getMultiplicador());
        
        for (Pokemon pokemon : equipo) {
            if (pokemon.getVida() > 0) { // Solo aplicar a pokémon vivos
                try {
                    // Inicializar estadísticas base si es necesario
                    pokemon.inicializarSoloEstadisticasBase();
                    
                    // Aplicar el efecto sobre el ataque efectivo actual
                    long ataqueActual = pokemon.getAtaqueEfectivo();
                    long ataqueReducido = (long) (ataqueActual * efecto.getMultiplicador());
                    
                    // Aplicar mínimo de 50
                    long nuevoAtaque = Math.max(50, ataqueReducido);
                    pokemon.setAtaqueModificado(nuevoAtaque);
                    pokemon.setIdEfectoActivo(efecto.getId());
                    
                    System.out.println("Pokémon: " + pokemon.getNombre() + 
                                     " | Ataque anterior: " + ataqueActual + 
                                     " | Multiplicador: " + efecto.getMultiplicador() + 
                                     " | Nuevo ataque: " + nuevoAtaque);
                } catch (Exception e) {
                    System.err.println("Error aplicando reducción de ataque a " + pokemon.getNombre() + ": " + e.getMessage());
                }
            }
        }
    }

    /**
     * Aplica reducción de defensa a todo un equipo de pokémon rival
     * @param equipo Lista de pokémon del equipo rival afectado
     * @param efecto Efecto de reducción de defensa a aplicar
     */
    public void aplicarReduccionDefensaEquipo(List<Pokemon> equipo, Efecto efecto) {
        if (efecto.getTipoEfecto() != Efecto.tipoEfecto.BAJAR_DEFENSA_RIVAL) {
            return;
        }
        
        System.out.println("=== APLICANDO REDUCCIÓN DE DEFENSA A TODO EL EQUIPO RIVAL ===");
        System.out.println("Multiplicador: " + efecto.getMultiplicador());
        
        for (Pokemon pokemon : equipo) {
            if (pokemon.getVida() > 0) { // Solo aplicar a pokémon vivos
                try {
                    // Inicializar estadísticas base si es necesario
                    pokemon.inicializarSoloEstadisticasBase();
                    
                    // Aplicar el efecto sobre la defensa efectiva actual
                    long defensaActual = pokemon.getDefensaEfectiva();
                    long defensaReducida = (long) (defensaActual * efecto.getMultiplicador());
                    
                    // Aplicar mínimo de 50
                    long nuevaDefensa = Math.max(50, defensaReducida);
                    pokemon.setDefensaModificada(nuevaDefensa);
                    pokemon.setIdEfectoActivo(efecto.getId());
                    
                    System.out.println("Pokémon: " + pokemon.getNombre() + 
                                     " | Defensa anterior: " + defensaActual + 
                                     " | Multiplicador: " + efecto.getMultiplicador() + 
                                     " | Nueva defensa: " + nuevaDefensa);
                } catch (Exception e) {
                    System.err.println("Error aplicando reducción de defensa a " + pokemon.getNombre() + ": " + e.getMessage());
                }
            }
        }
    }

    public List<Pokemon> obtenerPokemonPorTipo(Pokemon.TipoPokemon tipoPokemon) {
        return pokemonRepository.findByTipoPokemon(tipoPokemon);
    }

}
