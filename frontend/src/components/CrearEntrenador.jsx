import React, { useState, useEffect } from "react";
import pokemonService from "../Services/pokemon.service";
import entrenadorService from "../services/entrenador.service";
import "../App.css";

const CrearEntrenador = () => {
  const [pokemons, setPokemons] = useState([]);
  const [selectedPokemons, setSelectedPokemons] = useState([]);
  const [trainerName, setTrainerName] = useState("");
  const [pokemonDetails, setPokemonDetails] = useState(null);

  // Fetch all Pokémon on component mount
  useEffect(() => {
    const fetchPokemons = async () => {
      try {
        const response = await pokemonService.getAll();
        setPokemons(response.data);
      } catch (error) {
        console.error("Error al obtener los Pokémon:", error);
      }
    };

    fetchPokemons();
  }, []);

  // Handle Pokémon selection
  const togglePokemonSelection = (pokemonId) => {
    if (selectedPokemons.includes(pokemonId)) {
      setSelectedPokemons(selectedPokemons.filter((id) => id !== pokemonId));
    } else if (selectedPokemons.length < 3) {
      setSelectedPokemons([...selectedPokemons, pokemonId]);
    } else {
      alert("Solo puedes seleccionar hasta 3 Pokémon.");
    }
  };

  // Show Pokémon details
  const showDetails = async (id) => {
    try {
      const response = await pokemonService.getById(id);
      const attacks = await pokemonService.getAtaques(id);
      setPokemonDetails({
        ...response.data,
        attacks: attacks.data,
      });
    } catch (error) {
      console.error("Error al obtener los detalles del Pokémon:", error);
    }
  };

  // Handle trainer creation
  const handleCreateTrainer = async () => {
    if (trainerName === "") {
      alert("Debes ingresar un nombre para el entrenador.");
      return;
    }

    if (selectedPokemons.length !== 3) {
      alert("Debes seleccionar exactamente 3 Pokémon.");
      return;
    }

    const trainerData = {
      nombre: trainerName,
      idPokemon1: selectedPokemons[0],
      idPokemon2: selectedPokemons[1],
      idPokemon3: selectedPokemons[2],
    };

    try {
      const response = await entrenadorService.create(trainerData);
      alert(`Entrenador creado exitosamente: ${response.data.nombre}`);
      setTrainerName("");
      setSelectedPokemons([]);
    } catch (error) {
      console.error("Error al crear el entrenador:", error);
      alert("Error al crear el entrenador.");
    }
  };

  return (
    <div className="crear-entrenador">
      <h1>Crear Entrenador</h1>
      <div>
        <label>Nombre del Entrenador:</label>
        <input
          type="text"
          value={trainerName}
          onChange={(e) => setTrainerName(e.target.value)}
          required
        />
      </div>

      <h2>Selecciona 3 Pokémon</h2>
      <div className="pokemon-list">
        {pokemons.map((pokemon) => (
          <div key={pokemon.id} className="pokemon-card">
            <img
              src={`data:image/png;base64,${pokemon.sprite}`}
              alt={pokemon.nombre}
              className="pokemon-sprite"
            />
            <h3>{pokemon.nombre}</h3>
            <p>Tipo: {pokemon.tipoPokemon}</p>
            <button className="button" onClick={() => showDetails(pokemon.id)}>Ver Detalles</button>
            <button
              onClick={() => togglePokemonSelection(pokemon.id)}
              className={
                selectedPokemons.includes(pokemon.id) ? "selected" : ""
              }
            >
              {selectedPokemons.includes(pokemon.id) ? "Seleccionado" : "Seleccionar"}
            </button>
          </div>
        ))}
      </div>

      {pokemonDetails && (
        <div className="pokemon-details">
          <h2>Detalles de {pokemonDetails.nombre}</h2>
          <p>Vida: {pokemonDetails.vida}</p>
          <p>Ataque: {pokemonDetails.ataque}</p>
          <p>Defensa: {pokemonDetails.defensa}</p>
          <p>Velocidad: {pokemonDetails.velocidad}</p>
          <h3>Ataques:</h3>
          <ul>
            {pokemonDetails.attacks.map((attack) => (
              <li key={attack.id}>{attack.nombre} - {attack.tipo}</li>
            ))}
          </ul>
          <button onClick={() => setPokemonDetails(null)}>Cerrar</button>
        </div>
      )}

      <button onClick={handleCreateTrainer} className="submit-button">
        Crear Entrenador
      </button>
    </div>
  );
};

export default CrearEntrenador;
