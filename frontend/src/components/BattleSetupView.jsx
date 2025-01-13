import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";  // Importamos useNavigate
import entrenadorService from "../services/entrenador.service";
import pokemonService from "../Services/pokemon.service";
import "../App.css";

const BattleSetupView = () => {
  const [entrenadores, setEntrenadores] = useState([]);
  const [selectedTrainer1, setSelectedTrainer1] = useState(null);
  const [selectedTrainer2, setSelectedTrainer2] = useState(null);
  const navigate = useNavigate();  // Usamos useNavigate para navegar

  useEffect(() => {
    entrenadorService.getAll().then((response) => {
      setEntrenadores(response.data);
    });
  }, []);

  const handleSelectTrainer = (trainer, slot) => {
    if (slot === 1) setSelectedTrainer1(trainer);
    if (slot === 2) setSelectedTrainer2(trainer);
  };

  const isSelectionComplete = selectedTrainer1 && selectedTrainer2;

  const handleStartBattle = () => {
    if (isSelectionComplete) {
      navigate("/battle", {
        state: { selectedTrainer1, selectedTrainer2 },  // Pasamos los entrenadores seleccionados
      });
    }
  };

  return (
    <div>
      <h1>Selecciona Dos Entrenadores</h1>
      <div className="trainers-container">
        {entrenadores.map((trainer) => (
          <div className="trainer-card" key={trainer.id}>
            <h2>{trainer.nombre}</h2>
            <div className="pokemon-team">
              {[trainer.idPokemon1, trainer.idPokemon2, trainer.idPokemon3].map((pokemonId) => (
                <PokemonDetails key={pokemonId} id={pokemonId} />
              ))}
            </div>
            <button
              disabled={selectedTrainer1 === trainer || selectedTrainer2 === trainer}
              onClick={() => {
                if (!selectedTrainer1) handleSelectTrainer(trainer, 1);
                else handleSelectTrainer(trainer, 2);
              }}
            >
              Seleccionar
            </button>
          </div>
        ))}
      </div>
      <div className="selected-trainers">
        <h2>Entrenadores Seleccionados</h2>
        <div>
          <strong>Entrenador 1:</strong>{" "}
          {selectedTrainer1 ? selectedTrainer1.nombre : "Ninguno"}
        </div>
        <div>
          <strong>Entrenador 2:</strong>{" "}
          {selectedTrainer2 ? selectedTrainer2.nombre : "Ninguno"}
        </div>
      </div>
      <button
        disabled={!isSelectionComplete}
        onClick={handleStartBattle}  // Ahora usa navigate
        className="start-battle-button"
      >
        Simular Combate
      </button>
    </div>
  );
};

const PokemonDetails = ({ id }) => {
  const [pokemon, setPokemon] = useState(null);

  useEffect(() => {
    pokemonService.getById(id).then((response) => setPokemon(response.data));
  }, [id]);

  if (!pokemon) return <p>Cargando...</p>;

  return (
    <div>
      <img
        src={`data:image/png;base64,${pokemon.sprite}`}
        alt={pokemon.nombre}
      />
      <p>{pokemon.nombre}</p>
    </div>
  );
};

export default BattleSetupView;
