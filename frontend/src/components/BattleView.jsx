import React, { useState, useEffect } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import pokemonService from "../Services/pokemon.service";
import batallaService from "../services/batalla.service";
import "../App.css";

const BattleView = () => {
  const location = useLocation();
  const navigate = useNavigate();

  const { selectedTrainer1, selectedTrainer2 } = location.state || {};

  if (!selectedTrainer1 || !selectedTrainer2) {
    navigate("/setup");
  }

  const [pokemonDataTrainer1, setPokemonDataTrainer1] = useState([]);
  const [pokemonDataTrainer2, setPokemonDataTrainer2] = useState([]);
  const [attacksTrainer1, setAttacksTrainer1] = useState([]);
  const [attacksTrainer2, setAttacksTrainer2] = useState([]);

  const [livesTrainer1, setLivesTrainer1] = useState([0, 0, 0]);
  const [livesTrainer2, setLivesTrainer2] = useState([0, 0, 0]);

  const [vidaMaxE1, setVidaMaxE1] = useState([0, 0, 0]);
  const [vidaMaxE2, setVidaMaxE2] = useState([0, 0, 0]);

  const [selectedAttackerE1, setSelectedAttackerE1] = useState(null);
  const [selectedAttackerE2, setSelectedAttackerE2] = useState(null);
  const [selectedTargetE1, setSelectedTargetE1] = useState(null);
  const [selectedTargetE2, setSelectedTargetE2] = useState(null);

  const [turn, setTurn] = useState(1);
  const [DTO, setDTO] = useState({});

  useEffect(() => {
    Promise.all([
      pokemonService.getById(selectedTrainer1.idPokemon1),
      pokemonService.getById(selectedTrainer1.idPokemon2),
      pokemonService.getById(selectedTrainer1.idPokemon3),
    ]).then((responses) => {
      setPokemonDataTrainer1(responses.map((response) => response.data));

      const newLivesTrainer1 = responses.map((pokemon) => pokemon.data.vida);
      setLivesTrainer1(newLivesTrainer1);
      setVidaMaxE1(newLivesTrainer1);
    });

    Promise.all([
      pokemonService.getById(selectedTrainer2.idPokemon1),
      pokemonService.getById(selectedTrainer2.idPokemon2),
      pokemonService.getById(selectedTrainer2.idPokemon3),
    ]).then((responses) => {
      setPokemonDataTrainer2(responses.map((response) => response.data));

      const newLivesTrainer2 = responses.map((pokemon) => pokemon.data.vida);
      setLivesTrainer2(newLivesTrainer2);
      setVidaMaxE2(newLivesTrainer2);
    });
  }, [selectedTrainer1, selectedTrainer2]);

  useEffect(() => {
    if (pokemonDataTrainer1.length > 0) {
      Promise.all(
        pokemonDataTrainer1.map((pokemon) =>
          pokemonService.getAtaques(pokemon.id)
        )
      ).then((responses) => {
        setAttacksTrainer1(responses.map((response) => response.data));
      });
    }
  }, [pokemonDataTrainer1]);

  useEffect(() => {
    if (pokemonDataTrainer2.length > 0) {
      Promise.all(
        pokemonDataTrainer2.map((pokemon) =>
          pokemonService.getAtaques(pokemon.id)
        )
      ).then((responses) => {
        setAttacksTrainer2(responses.map((response) => response.data));
      });
    }
  }, [pokemonDataTrainer2]);

  const handleAttack = () => {
    if (
      selectedAttackerE1 !== null &&
      selectedAttackerE2 !== null &&
      selectedTargetE1 !== null &&
      selectedTargetE2 !== null
    ) {
      // Verificar si el Pokémon atacante tiene vida > 0
      if (livesTrainer1[selectedAttackerE1] <= 0) {
        alert(`¡El Pokémon ${pokemonDataTrainer1[selectedAttackerE1].nombre} está debilitado y no puede atacar!`);
        return;
      }

      if (livesTrainer2[selectedAttackerE2] <= 0) {
        alert(`¡El Pokémon ${pokemonDataTrainer2[selectedAttackerE2].nombre} está debilitado y no puede atacar!`);
        return;
      }

      // Verificar si el Pokémon objetivo tiene vida > 0
      if (livesTrainer1[selectedTargetE2] <= 0) {
        alert(`¡El Pokémon ${pokemonDataTrainer1[selectedTargetE2].nombre} ya está debilitado y no puede ser atacado!`);
        return;
      }

      if (livesTrainer2[selectedTargetE1] <= 0) {
        alert(`¡El Pokémon ${pokemonDataTrainer2[selectedTargetE1].nombre} ya está debilitado y no puede ser atacado!`);
        return;
      }

      // Actualizamos las vidas directamente dentro de los objetos de cada Pokémon
      const updatedEntrenador1 = pokemonDataTrainer1.map((pokemon, index) => ({
        ...pokemon,
        vida: livesTrainer1[index], // Actualizamos la vida de cada Pokémon de entrenador 1
      }));

      const updatedEntrenador2 = pokemonDataTrainer2.map((pokemon, index) => ({
        ...pokemon,
        vida: livesTrainer2[index], // Actualizamos la vida de cada Pokémon de entrenador 2
      }));

      // Construimos el objeto DTO para la batalla con los Pokémon y sus vidas actualizadas
      const batallaDTO = {
        entrenador1: updatedEntrenador1, // Entrenador 1 con las vidas actualizadas
        entrenador2: updatedEntrenador2, // Entrenador 2 con las vidas actualizadas
        ataqueE1: attacksTrainer1[selectedAttackerE1][0], // Primer ataque del Pokémon seleccionado
        ataqueE2: attacksTrainer2[selectedAttackerE2][0], // Primer ataque del Pokémon seleccionado
        turno: turn,
      };

      console.log("DTO de la batalla:", batallaDTO);

      // Actualizamos el estado con el DTO actualizado antes de enviar el combate
      setDTO(batallaDTO);

      // Llamamos al servicio con las posiciones y la vida actualizada
      batallaService
        .combatir(
          selectedAttackerE1,
          selectedAttackerE2,
          selectedTargetE2,
          selectedTargetE1,
          batallaDTO // Pasamos el DTO actualizado con las vidas
        )
        .then((response) => {
          setTurn((prevTurn) => prevTurn + 1);

          const newLivesTrainer1 = response.data.entrenador1.map((pokemon) => pokemon.vida);
          setLivesTrainer1(newLivesTrainer1);
          const newLivesTrainer2 = response.data.entrenador2.map((pokemon) => pokemon.vida);
          setLivesTrainer2(newLivesTrainer2);

          console.log("vidas actualizadas:", newLivesTrainer1, newLivesTrainer2);

          // Verificar si algún entrenador ha perdido (todos sus Pokémon tienen vida 0)
          const isTrainer1Lost = newLivesTrainer1.every((vida) => vida === 0);
          const isTrainer2Lost = newLivesTrainer2.every((vida) => vida === 0);

          if (isTrainer1Lost) {
            alert(`${selectedTrainer2.nombre} ha ganado el combate!`);
            navigate("/"); // O redirigir a la página de inicio o de configuración
          } else if (isTrainer2Lost) {
            alert(`${selectedTrainer1.nombre} ha ganado el combate!`);
            navigate("/"); // O redirigir a la página de inicio o de configuración
          }
        })
        .catch((error) => {
          console.error("Error al combatir:", error);
        });
    } else {
      alert("Por favor, selecciona todos los Pokémon para atacar.");
    }
  };

  if (pokemonDataTrainer1.length === 0 || pokemonDataTrainer2.length === 0) {
    return <p>Cargando información de Pokémon...</p>;
  }

  return (
    <div>
      <h1>Simulación de Combate - Turno {turn}</h1>

      <div className="battle-container">
        {/* Entrenador 1 */}
        <div className="trainer-container">
          <h2>{selectedTrainer1.nombre}</h2>
          <div className="pokemon-list">
            {pokemonDataTrainer1.map((pokemon, index) => (
              <div key={pokemon.id} className="pokemon-card">
                <img
                  src={`data:image/png;base64,${pokemon.sprite}`}
                  alt={pokemon.nombre}
                />
                <p>{pokemon.nombre} - Vida: {livesTrainer1[index]} / {vidaMaxE1[index]}</p>
                <div>
                  {attacksTrainer1[index]?.map((ataque) => (
                    <button
                      key={ataque.id}
                      onClick={() => {
                        setSelectedAttackerE1(index);
                      }}
                      style={{
                        backgroundColor:
                          selectedAttackerE1 === index ? "lightblue" : "",
                        margin: "5px",
                      }}
                    >
                      {ataque.nombre}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>

        {/* Entrenador 2 */}
        <div className="trainer-container">
          <h2>{selectedTrainer2.nombre}</h2>
          <div className="pokemon-list">
            {pokemonDataTrainer2.map((pokemon, index) => (
              <div key={pokemon.id} className="pokemon-card">
                <img
                  src={`data:image/png;base64,${pokemon.sprite}`}
                  alt={pokemon.nombre}
                />
                <p>{pokemon.nombre} - Vida: {livesTrainer2[index]} / {vidaMaxE2[index]}</p>
                <div>
                  {attacksTrainer2[index]?.map((ataque) => (
                    <button
                      key={ataque.id}
                      onClick={() => {
                        setSelectedAttackerE2(index);
                      }}
                      style={{
                        backgroundColor:
                          selectedAttackerE2 === index ? "lightblue" : "",
                        margin: "5px",
                      }}
                    >
                      {ataque.nombre}
                    </button>
                  ))}
                </div>
              </div>
            ))}
          </div>
        </div>
      </div>

      <div>
        <h2>Selecciona el objetivo del ataque:</h2>

        <div>
          {pokemonDataTrainer1.map((pokemon, index) => (
            <button
              key={pokemon.id}
              onClick={() => setSelectedTargetE2(index)}
              style={{
                backgroundColor: selectedTargetE2 === index ? "lightgreen" : "",
              }}
            >
              {pokemon.nombre}
            </button>
          ))}
        </div>

        <div>
          {pokemonDataTrainer2.map((pokemon, index) => (
            <button
              key={pokemon.id}
              onClick={() => setSelectedTargetE1(index)}
              style={{
                backgroundColor: selectedTargetE1 === index ? "lightgreen" : "",
              }}
            >
              {pokemon.nombre}
            </button>
          ))}
        </div>
      </div>

      <button onClick={handleAttack}>Realizar Ataque</button>
      <button onClick={() => navigate("/setup")}>Volver a la selección</button>
    </div>
  );
};

export default BattleView;
