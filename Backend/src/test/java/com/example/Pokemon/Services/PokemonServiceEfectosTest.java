package com.example.Pokemon.Services;

import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(MockitoExtension.class)
public class PokemonServiceEfectosTest {

    @InjectMocks
    private PokemonService pokemonService;

    @Mock
    private com.example.Pokemon.Repositories.PokemonRepository pokemonRepository;
    
    @Mock
    private com.example.Pokemon.Repositories.AtaqueRepository ataqueRepository;
    
    @Mock
    private com.example.Pokemon.Repositories.EfectoRepository efectoRepository;
    
    @Mock
    private EfectoService efectoService;

    private Pokemon pokemon;
    private Pokemon rival;
    private Efecto danzaEspada;

    @BeforeEach
    void setUp() {
        // Crear Pokémon de prueba
        pokemon = new Pokemon();
        pokemon.setId(1L);
        pokemon.setNombre("Charizard");
        pokemon.setTipoPokemon(Pokemon.TipoPokemon.FUEGO);
        pokemon.setVida(100L);
        pokemon.setAtaque(90L);
        pokemon.setDefensa(70L);
        pokemon.setVidaBase(100L);
        pokemon.setAtaqueBase(90L);
        pokemon.setDefensaBase(70L);
        pokemon.setAtaqueModificado(90L);
        pokemon.setDefensaModificada(70L);

        // Crear rival de prueba
        rival = new Pokemon();
        rival.setId(2L);
        rival.setNombre("Blastoise");
        rival.setTipoPokemon(Pokemon.TipoPokemon.AGUA);
        rival.setVida(100L);
        rival.setAtaque(80L);
        rival.setDefensa(90L);
        rival.setVidaBase(100L);
        rival.setAtaqueBase(80L);
        rival.setDefensaBase(90L);
        rival.setAtaqueModificado(80L);
        rival.setDefensaModificada(90L);

        // Crear efecto Danza Espada
        danzaEspada = new Efecto();
        danzaEspada.setId(1L);
        danzaEspada.setNombre("Danza Espada");
        danzaEspada.setTipoEfecto(Efecto.tipoEfecto.SUBIR_ATAQUE_PROPIO);
        danzaEspada.setMultiplicador(1.5); // 50% de incremento
    }

    @Test
    public void testAplicarDanzaEspadaUnaVez() {
        System.out.println("=== TEST: Aplicar Danza Espada una vez ===");
        
        // Estado inicial
        Long ataqueInicial = pokemon.getAtaqueEfectivo();
        Long ataqueBase = pokemon.getAtaqueBase();
        
        System.out.println("Ataque inicial: " + ataqueInicial);
        System.out.println("Ataque base: " + ataqueBase);
        
        // Aplicar efecto
        Pokemon pokemonActualizado = pokemonService.aplicarEfecto(pokemon, rival, danzaEspada);
        
        // Verificar resultado
        Long ataqueEsperado = ataqueInicial + (long)(ataqueBase * (danzaEspada.getMultiplicador() - 1.0));
        Long ataqueObtenido = pokemonActualizado.getAtaqueEfectivo();
        
        System.out.println("Ataque esperado: " + ataqueEsperado);
        System.out.println("Ataque obtenido: " + ataqueObtenido);
        
        assertEquals(ataqueEsperado, ataqueObtenido, 
            "El ataque después de aplicar Danza Espada debe ser " + ataqueEsperado + " pero fue " + ataqueObtenido);
        
        // Verificar que el ataque base no haya cambiado
        assertEquals(ataqueBase, pokemonActualizado.getAtaqueBase(), 
            "El ataque base no debe cambiar");
        
        System.out.println("✓ Primera aplicación funcionó correctamente");
    }

    @Test
    public void testAplicarDanzaEspadaMultiplesVeces() {
        System.out.println("=== TEST: Aplicar Danza Espada múltiples veces ===");
        
        Long ataqueBase = pokemon.getAtaqueBase();
        Long incrementoPorUso = (long)(ataqueBase * (danzaEspada.getMultiplicador() - 1.0));
        
        System.out.println("Ataque base: " + ataqueBase);
        System.out.println("Incremento por uso: " + incrementoPorUso);
        
        // Primera aplicación
        pokemon = pokemonService.aplicarEfecto(pokemon, rival, danzaEspada);
        Long ataqueEsperado1 = ataqueBase + incrementoPorUso;
        
        System.out.println("Después de 1ra aplicación:");
        System.out.println("  - Esperado: " + ataqueEsperado1);
        System.out.println("  - Obtenido: " + pokemon.getAtaqueEfectivo());
        
        assertEquals(ataqueEsperado1, pokemon.getAtaqueEfectivo(), 
            "Primera aplicación falló");
        
        // Segunda aplicación
        pokemon = pokemonService.aplicarEfecto(pokemon, rival, danzaEspada);
        Long ataqueEsperado2 = ataqueEsperado1 + incrementoPorUso;
        
        System.out.println("Después de 2da aplicación:");
        System.out.println("  - Esperado: " + ataqueEsperado2);
        System.out.println("  - Obtenido: " + pokemon.getAtaqueEfectivo());
        
        assertEquals(ataqueEsperado2, pokemon.getAtaqueEfectivo(), 
            "Segunda aplicación falló");
        
        // Tercera aplicación
        pokemon = pokemonService.aplicarEfecto(pokemon, rival, danzaEspada);
        Long ataqueEsperado3 = ataqueEsperado2 + incrementoPorUso;
        
        System.out.println("Después de 3ra aplicación:");
        System.out.println("  - Esperado: " + ataqueEsperado3);
        System.out.println("  - Obtenido: " + pokemon.getAtaqueEfectivo());
        
        assertEquals(ataqueEsperado3, pokemon.getAtaqueEfectivo(), 
            "Tercera aplicación falló");
        
        // Verificar que el patrón es correcto
        Long incrementoTotal = ataqueEsperado3 - ataqueBase;
        Long incrementoEsperadoTotal = incrementoPorUso * 3;
        
        assertEquals(incrementoEsperadoTotal, incrementoTotal, 
            "El incremento total no es correcto");
        
        System.out.println("✓ Acumulación múltiple funcionó correctamente");
        System.out.println("  - Incremento total: " + incrementoTotal);
        System.out.println("  - Incremento esperado: " + incrementoEsperadoTotal);
    }

    @Test
    public void testEfectoSubirDefensa() {
        System.out.println("=== TEST: Efecto de subir defensa ===");
        
        // Crear efecto de defensa
        Efecto fortaleza = new Efecto();
        fortaleza.setId(2L);
        fortaleza.setNombre("Fortaleza");
        fortaleza.setTipoEfecto(Efecto.tipoEfecto.SUBIR_DEFENSA_PROPIO);
        fortaleza.setMultiplicador(1.3); // 30% de incremento
        
        Long defensaInicial = pokemon.getDefensaEfectiva();
        Long defensaBase = pokemon.getDefensaBase();
        
        System.out.println("Defensa inicial: " + defensaInicial);
        System.out.println("Defensa base: " + defensaBase);
        
        // Aplicar efecto
        Pokemon pokemonActualizado = pokemonService.aplicarEfecto(pokemon, rival, fortaleza);
        
        // Verificar resultado
        Long defensaEsperada = defensaInicial + (long)(defensaBase * (fortaleza.getMultiplicador() - 1.0));
        Long defensaObtenida = pokemonActualizado.getDefensaEfectiva();
        
        System.out.println("Defensa esperada: " + defensaEsperada);
        System.out.println("Defensa obtenida: " + defensaObtenida);
        
        assertEquals(defensaEsperada, defensaObtenida, 
            "La defensa después de aplicar Fortaleza debe ser " + defensaEsperada + " pero fue " + defensaObtenida);
        
        System.out.println("✓ Efecto de defensa funcionó correctamente");
    }

    @Test
    public void testCalculoDano() {
        System.out.println("=== TEST: Cálculo de daño con estadísticas efectivas ===");
        
        // Aplicar Danza Espada para aumentar ataque
        pokemon = pokemonService.aplicarEfecto(pokemon, rival, danzaEspada);
        
        Long ataqueEfectivo = pokemon.getAtaqueEfectivo();
        Long defensaEfectiva = rival.getDefensaEfectiva();
        
        System.out.println("Ataque efectivo del atacante: " + ataqueEfectivo);
        System.out.println("Defensa efectiva del defensor: " + defensaEfectiva);
        
        // Verificar que getAtaqueEfectivo devuelve el valor modificado
        assertTrue(ataqueEfectivo > pokemon.getAtaqueBase(), 
            "El ataque efectivo debe ser mayor que el ataque base después de Danza Espada");
        
        // Simular cálculo de daño
        Long dano = pokemonService.calcularDano(ataqueEfectivo, defensaEfectiva, 1.0, 1.0, 100);
        
        System.out.println("Daño calculado: " + dano);
        
        assertNotNull(dano, "El daño no debe ser nulo");
        assertTrue(dano > 0, "El daño debe ser positivo");
        
        System.out.println("✓ Cálculo de daño usa estadísticas efectivas correctamente");
    }
}
