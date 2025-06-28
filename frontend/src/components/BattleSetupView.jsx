import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import entrenadorService from "../services/entrenador.service";
import pokemonService from "../Services/pokemon.service";
import "../App.css";

const BattleSetupView = () => {
  const [entrenadores, setEntrenadores] = useState([]);
  const [selectedTrainer1, setSelectedTrainer1] = useState(null);
  const [selectedTrainer2, setSelectedTrainer2] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const navigate = useNavigate();

  useEffect(() => {
    const fetchEntrenadores = async () => {
      try {
        setLoading(true);
        const response = await entrenadorService.getAll();
        setEntrenadores(response.data);
        if (response.data.length < 2) {
          setError("Se necesitan al menos 2 entrenadores para una batalla. Crea más entrenadores primero.");
        }
      } catch (error) {
        console.error("Error al obtener entrenadores:", error);
        setError("Error al cargar los entrenadores. Por favor, intenta nuevamente.");
      } finally {
        setLoading(false);
      }
    };

    fetchEntrenadores();
  }, []);

  const handleSelectTrainer = (trainer, slot) => {
    if (slot === 1) {
      setSelectedTrainer1(trainer);
      // If trainer 2 is the same, clear it
      if (selectedTrainer2 && selectedTrainer2.id === trainer.id) {
        setSelectedTrainer2(null);
      }
    }
    if (slot === 2) {
      setSelectedTrainer2(trainer);
      // If trainer 1 is the same, clear it
      if (selectedTrainer1 && selectedTrainer1.id === trainer.id) {
        setSelectedTrainer1(null);
      }
    }
  };

  const isSelectionComplete = selectedTrainer1 && selectedTrainer2 && selectedTrainer1.id !== selectedTrainer2.id;

  const handleStartBattle = () => {
    if (isSelectionComplete) {
      navigate("/battle", {
        state: { selectedTrainer1, selectedTrainer2 },
      });
    }
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Cargando entrenadores...</p>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container">
      <div className="header-section">
        <h1 className="page-title">
          <span className="title-icon">⚔️</span>
          Configurar Batalla
        </h1>
        <p className="page-description">
          Selecciona dos entrenadores que se enfrentarán en una épica batalla Pokémon
        </p>
      </div>

      {error && (
        <div className="alert alert-error">
          <span className="alert-icon">❌</span>
          {error}
        </div>
      )}

      {!error && entrenadores.length >= 2 && (
        <>
          {/* Selection Summary */}
          <div className="selection-summary">
            <div className="battle-matchup">
              <div className={`trainer-slot ${selectedTrainer1 ? 'selected' : 'empty'}`}>
                <div className="slot-header">
                  <span className="slot-icon">👨‍💼</span>
                  <span className="slot-title">Entrenador 1</span>
                </div>
                {selectedTrainer1 ? (
                  <div className="selected-trainer">
                    <h3 className="trainer-name">{selectedTrainer1.nombre}</h3>
                    <div className="trainer-pokemon-preview">
                      <PokemonPreview id={selectedTrainer1.idPokemon1} />
                      <PokemonPreview id={selectedTrainer1.idPokemon2} />
                      <PokemonPreview id={selectedTrainer1.idPokemon3} />
                    </div>
                  </div>
                ) : (
                  <div className="empty-slot">
                    <div className="empty-icon">❓</div>
                    <p>Selecciona un entrenador</p>
                  </div>
                )}
              </div>

              <div className="vs-indicator">
                <span className="vs-text">VS</span>
                <div className="battle-icon">⚡</div>
              </div>

              <div className={`trainer-slot ${selectedTrainer2 ? 'selected' : 'empty'}`}>
                <div className="slot-header">
                  <span className="slot-icon">👩‍💼</span>
                  <span className="slot-title">Entrenador 2</span>
                </div>
                {selectedTrainer2 ? (
                  <div className="selected-trainer">
                    <h3 className="trainer-name">{selectedTrainer2.nombre}</h3>
                    <div className="trainer-pokemon-preview">
                      <PokemonPreview id={selectedTrainer2.idPokemon1} />
                      <PokemonPreview id={selectedTrainer2.idPokemon2} />
                      <PokemonPreview id={selectedTrainer2.idPokemon3} />
                    </div>
                  </div>
                ) : (
                  <div className="empty-slot">
                    <div className="empty-icon">❓</div>
                    <p>Selecciona un entrenador</p>
                  </div>
                )}
              </div>
            </div>
          </div>

          {/* Trainers Grid */}
          <div className="trainers-section">
            <h2 className="section-title">
              <span className="title-icon">👥</span>
              Entrenadores Disponibles
            </h2>
            
            <div className="trainers-grid">
              {entrenadores.map((trainer) => {
                const isSelected = (selectedTrainer1 && selectedTrainer1.id === trainer.id) || 
                                 (selectedTrainer2 && selectedTrainer2.id === trainer.id);
                const canSelect = !isSelected;
                
                return (
                  <div 
                    key={trainer.id} 
                    className={`trainer-card ${isSelected ? 'selected' : ''}`}
                  >
                    <div className="trainer-header">
                      <h3 className="trainer-name">{trainer.nombre}</h3>
                      {isSelected && (
                        <div className="selection-badge">
                          <span className="badge-icon">✅</span>
                          Seleccionado
                        </div>
                      )}
                    </div>
                    
                    <div className="pokemon-team">
                      <PokemonDetails id={trainer.idPokemon1} />
                      <PokemonDetails id={trainer.idPokemon2} />
                      <PokemonDetails id={trainer.idPokemon3} />
                    </div>
                    
                    <div className="trainer-actions">
                      {canSelect && (
                        <>
                          <button
                            className="btn btn-primary btn-sm"
                            onClick={() => handleSelectTrainer(trainer, 1)}
                            disabled={selectedTrainer1 && selectedTrainer1.id === trainer.id}
                          >
                            <span className="btn-icon">👨‍💼</span>
                            Entrenador 1
                          </button>
                          <button
                            className="btn btn-secondary btn-sm"
                            onClick={() => handleSelectTrainer(trainer, 2)}
                            disabled={selectedTrainer2 && selectedTrainer2.id === trainer.id}
                          >
                            <span className="btn-icon">👩‍💼</span>
                            Entrenador 2
                          </button>
                        </>
                      )}
                      {isSelected && (
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => {
                            if (selectedTrainer1 && selectedTrainer1.id === trainer.id) {
                              setSelectedTrainer1(null);
                            }
                            if (selectedTrainer2 && selectedTrainer2.id === trainer.id) {
                              setSelectedTrainer2(null);
                            }
                          }}
                        >
                          <span className="btn-icon">✖️</span>
                          Deseleccionar
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          </div>

          {/* Start Battle Button */}
          <div className="action-section">
            <button
              disabled={!isSelectionComplete}
              onClick={handleStartBattle}
              className={`btn btn-primary btn-lg ${!isSelectionComplete ? 'btn-disabled' : ''}`}
            >
              <span className="btn-icon">⚔️</span>
              {isSelectionComplete ? "¡Iniciar Batalla!" : "Selecciona ambos entrenadores"}
            </button>
            
            {isSelectionComplete && (
              <div className="battle-preview">
                <p className="battle-text">
                  <strong>{selectedTrainer1.nombre}</strong> vs <strong>{selectedTrainer2.nombre}</strong>
                </p>
                <small className="form-hint">
                  ¡Que comience la batalla épica!
                </small>
              </div>
            )}
          </div>
        </>
      )}

      {!error && entrenadores.length < 2 && (
        <div className="empty-state">
          <div className="empty-icon">👥</div>
          <h3>No hay suficientes entrenadores</h3>
          <p>Necesitas al menos 2 entrenadores para comenzar una batalla.</p>
          <button 
            className="btn btn-primary"
            onClick={() => navigate("/crear-entrenador")}
          >
            <span className="btn-icon">➕</span>
            Crear Entrenador
          </button>
        </div>
      )}
    </div>
  );
};

const PokemonDetails = ({ id }) => {
  const [pokemon, setPokemon] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    const fetchPokemon = async () => {
      try {
        const response = await pokemonService.getById(id);
        setPokemon(response.data);
      } catch (error) {
        console.error("Error al obtener Pokémon:", error);
      } finally {
        setLoading(false);
      }
    };

    fetchPokemon();
  }, [id]);

  if (loading) {
    return (
      <div className="pokemon-card loading">
        <div className="loading-spinner small"></div>
      </div>
    );
  }

  if (!pokemon) {
    return (
      <div className="pokemon-card error">
        <div className="error-icon">❌</div>
        <p>Error</p>
      </div>
    );
  }

  return (
    <div className="pokemon-card mini">
      <div className="pokemon-image">
        <img
          src={`data:image/png;base64,${pokemon.sprite}`}
          alt={pokemon.nombre}
          className="pokemon-sprite"
        />
      </div>
      <div className="pokemon-info">
        <h4 className="pokemon-name">{pokemon.nombre}</h4>
        <div className={`type-badge type-${pokemon.tipoPokemon.toLowerCase()}`}>
          {getTypeIcon(pokemon.tipoPokemon)}
        </div>
        <div className="pokemon-stats-mini">
          <span className="stat-mini">❤️{pokemon.vida}</span>
          <span className="stat-mini">⚔️{pokemon.ataque}</span>
          <span className="stat-mini">🛡️{pokemon.defensa}</span>
        </div>
      </div>
    </div>
  );
};

const PokemonPreview = ({ id }) => {
  const [pokemon, setPokemon] = useState(null);

  useEffect(() => {
    const fetchPokemon = async () => {
      try {
        const response = await pokemonService.getById(id);
        setPokemon(response.data);
      } catch (error) {
        console.error("Error al obtener Pokémon:", error);
      }
    };

    fetchPokemon();
  }, [id]);

  if (!pokemon) {
    return (
      <div className="pokemon-preview loading">
        <div className="loading-dot"></div>
      </div>
    );
  }

  return (
    <div className="pokemon-preview">
      <img
        src={`data:image/png;base64,${pokemon.sprite}`}
        alt={pokemon.nombre}
        className="preview-sprite"
        title={`${pokemon.nombre} (${pokemon.tipoPokemon})`}
      />
    </div>
  );
};

// Helper function for type icons
const getTypeIcon = (tipo) => {
  const icons = {
    AGUA: "💧",
    FUEGO: "🔥",
    PLANTA: "🌿",
    TIERRA: "🌍",
    ELECTRICO: "⚡"
  };
  return icons[tipo] || "⭐";
};

export default BattleSetupView;
