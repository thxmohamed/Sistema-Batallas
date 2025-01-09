package com.example.Pokemon.Controllers;

import com.example.Pokemon.Entities.Ataque;
import com.example.Pokemon.Entities.Efecto;
import com.example.Pokemon.Entities.Pokemon;
import com.example.Pokemon.Services.PokemonService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;

@RestController
@RequestMapping("/pokemon")
@CrossOrigin("*")
public class PokemonController {
    @Autowired
    PokemonService pokemonService;

    @GetMapping("/")
    public ResponseEntity<List<Pokemon>> getAllPokemon() {
        List<Pokemon> pokemons = pokemonService.getAllPokemon();
        return new ResponseEntity<>(pokemons, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Pokemon> getPokemonById(@PathVariable Long id) {
        Pokemon pokemon = pokemonService.getPokemonById(id);
        return new ResponseEntity<>(pokemon, HttpStatus.OK);
    }

    @PostMapping("/create")
    public ResponseEntity<Pokemon> createPokemon(
            @RequestParam("nombre") String nombre,
            @RequestParam("tipoPokemon") String tipoPokemon,
            @RequestParam("vida") Long vida,
            @RequestParam("ataque") Long ataque,
            @RequestParam("defensa") Long defensa,
            @RequestParam("velocidad") Long velocidad,
            @RequestParam("idAtaque1") Long idAtaque1,
            @RequestParam("idAtaque2") Long idAtaque2,
            @RequestParam("idEfecto") Long idEfecto,
            @RequestParam("estado") int estado,
            @RequestParam("sprite") MultipartFile sprite) throws IOException {

        // Convertir la imagen (sprite) a byte[]
        byte[] spriteBytes = sprite.getBytes();

        // Crear el objeto Pokemon
        Pokemon pokemon = new Pokemon();
        pokemon.setNombre(nombre);
        pokemon.setTipoPokemon(Pokemon.TipoPokemon.valueOf(tipoPokemon));  // Si el tipoPokemon es un String, conviértelo
        pokemon.setVida(vida);
        pokemon.setAtaque(ataque);
        pokemon.setDefensa(defensa);
        pokemon.setVelocidad(velocidad);
        pokemon.setIdAtaque1(idAtaque1);
        pokemon.setIdAtaque2(idAtaque2);
        pokemon.setIdEfecto(idEfecto);
        pokemon.setEstado(estado);
        pokemon.setSprite(spriteBytes);  // Aquí asignas el sprite como byte[]

        // Guardar el Pokémon en la base de datos
        Pokemon createdPokemon = pokemonService.createPokemon(pokemon);

        return new ResponseEntity<>(createdPokemon, HttpStatus.CREATED);
    }

    @GetMapping("/download/{id}")
    public ResponseEntity<byte[]> downloadPokemonSprite(@PathVariable Long id) {
        Pokemon pokemon = pokemonService.getPokemonById(id);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.IMAGE_PNG);
        headers.setContentDisposition(ContentDisposition.attachment().build());

        return new ResponseEntity<>(pokemon.getSprite(), headers, HttpStatus.OK);
    }

    @GetMapping("/ataques/{id}")
    public ResponseEntity<List<Ataque>> getPokemonAtaques(@PathVariable Long id) {
        Pokemon pokemon = pokemonService.getPokemonById(id);
        List<Ataque> ataques = pokemonService.getPokemonAtaques(pokemon);
        if(!ataques.isEmpty()) {
            return new ResponseEntity<>(ataques, HttpStatus.OK);
        }
        return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
    }

    @GetMapping("/efecto/{id}")
    public ResponseEntity<Efecto> getPokemonEfecto(@PathVariable Long id) {
        Pokemon pokemon = pokemonService.getPokemonById(id);
        Efecto efecto = pokemonService.getPokemonEfecto(pokemon);
        if(efecto == null) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(efecto, HttpStatus.OK);
    }
}
