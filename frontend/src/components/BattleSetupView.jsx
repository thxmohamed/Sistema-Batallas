import React, { useState, useEffect } from "react";
import { useNavigate } from "react-router-dom";
import entrenadorService from "../services/entrenador.service";
import pokemonService from "../Services/pokemon.service";
import batallaService from "../services/batalla.service";
import "../App.css";

const BattleSetupView = () => {
  const [entrenadores, setEntrenadores] = useState([]);
  const [selectedTrainer1, setSelectedTrainer1] = useState(null);
  const [selectedTrainer2, setSelectedTrainer2] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState("");
  const [battleMode, setBattleMode] = useState("trainers"); // "trainers" or "random"
  const [isCreatingRandomBattle, setIsCreatingRandomBattle] = useState(false);
  
  // Paginación
  const [currentPage, setCurrentPage] = useState(1);
  const [trainersPerPage] = useState(6); // Mostrar 6 entrenadores por página
  const [searchTerm, setSearchTerm] = useState("");
  
  // Modal de detalles
  const [showDetailsModal, setShowDetailsModal] = useState(false);
  const [selectedTrainerDetails, setSelectedTrainerDetails] = useState(null);
  const [trainerPokemonDetails, setTrainerPokemonDetails] = useState([]);
  const [loadingDetails, setLoadingDetails] = useState(false);
  
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

  const handleRandomBattle = async () => {
    try {
      setIsCreatingRandomBattle(true);
      const response = await batallaService.createRandomBattle();
      
      // Navigate to battle with random battle data
      navigate("/battle", {
        state: { 
          randomBattle: response.data,
          isRandomBattle: true 
        },
      });
    } catch (error) {
      console.error("Error al crear batalla aleatoria:", error);
      setError("Error al crear la batalla aleatoria. Por favor, intenta nuevamente.");
    } finally {
      setIsCreatingRandomBattle(false);
    }
  };

  // Funciones para paginación
  const filteredEntrenadores = entrenadores.filter(entrenador =>
    entrenador.nombre.toLowerCase().includes(searchTerm.toLowerCase())
  );

  const indexOfLastTrainer = currentPage * trainersPerPage;
  const indexOfFirstTrainer = indexOfLastTrainer - trainersPerPage;
  const currentTrainers = filteredEntrenadores.slice(indexOfFirstTrainer, indexOfLastTrainer);
  const totalPages = Math.ceil(filteredEntrenadores.length / trainersPerPage);

  const paginate = (pageNumber) => {
    setCurrentPage(pageNumber);
  };

  const handleSearchChange = (e) => {
    setSearchTerm(e.target.value);
    setCurrentPage(1); // Reset to first page when searching
  };

  // Funciones para modal de detalles
  const handleShowDetails = async (trainer) => {
    setSelectedTrainerDetails(trainer);
    setShowDetailsModal(true);
    setLoadingDetails(true);
    
    try {
      // Cargar detalles completos de todos los Pokémon del entrenador
      const pokemonPromises = [
        pokemonService.getById(trainer.idPokemon1),
        pokemonService.getById(trainer.idPokemon2),
        pokemonService.getById(trainer.idPokemon3)
      ];
      
      const pokemonResponses = await Promise.all(pokemonPromises);
      const pokemonData = pokemonResponses.map(response => response.data);
      
      // Cargar ataques y efectos para cada Pokémon
      const detailedPokemonData = await Promise.all(
        pokemonData.map(async (pokemon) => {
          try {
            const [attacksResponse, effectsResponse] = await Promise.all([
              pokemonService.getAtaques(pokemon.id),
              pokemonService.getEfecto(pokemon.id)
            ]);
            
            console.log(`Datos para ${pokemon.nombre}:`, {
              ataques: attacksResponse.data,
              efectos: effectsResponse.data,
              effectsType: typeof effectsResponse.data,
              effectsIsArray: Array.isArray(effectsResponse.data)
            });
            
            // Manejar diferentes formatos de respuesta para efectos
            let efectosProcessed = [];
            if (effectsResponse.data) {
              if (Array.isArray(effectsResponse.data)) {
                efectosProcessed = effectsResponse.data;
              } else if (typeof effectsResponse.data === 'object' && effectsResponse.data.id) {
                // Si es un solo objeto efecto
                efectosProcessed = [effectsResponse.data];
              } else if (effectsResponse.data.length !== undefined) {
                // Si tiene una propiedad length pero no es array
                efectosProcessed = Array.from(effectsResponse.data);
              }
            }
            
            return {
              ...pokemon,
              ataques: attacksResponse.data || [],
              efectos: efectosProcessed
            };
          } catch (error) {
            console.error(`Error al cargar datos para ${pokemon.nombre}:`, error);
            return {
              ...pokemon,
              ataques: [],
              efectos: []
            };
          }
        })
      );
      
      setTrainerPokemonDetails(detailedPokemonData);
    } catch (error) {
      console.error("Error al cargar detalles del entrenador:", error);
      setTrainerPokemonDetails([]);
    } finally {
      setLoadingDetails(false);
    }
  };

  const handleCloseDetails = () => {
    setShowDetailsModal(false);
    setSelectedTrainerDetails(null);
    setTrainerPokemonDetails([]);
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
          Elige tu modo de batalla preferido
        </p>
      </div>

      {/* Battle Mode Selection */}
      <div className="battle-mode-section">
        <h2 className="section-title">
          <span className="title-icon">🎯</span>
          Modo de Batalla
        </h2>
        
        <div className="battle-mode-grid">
          <div 
            className={`battle-mode-card ${battleMode === "trainers" ? "selected" : ""}`}
            onClick={() => setBattleMode("trainers")}
          >
            <div className="mode-icon">👥</div>
            <h3 className="mode-title">Batalla de Entrenadores</h3>
            <p className="mode-description">
              Selecciona dos entrenadores que se enfrentarán con sus equipos Pokémon
            </p>
            <div className="mode-features">
              <span className="feature">🎖️ Entrenadores personalizados</span>
              <span className="feature">🔥 Estrategia avanzada</span>
              <span className="feature">💎 Equipos únicos</span>
            </div>
          </div>
          
          <div 
            className={`battle-mode-card ${battleMode === "random" ? "selected" : ""}`}
            onClick={() => setBattleMode("random")}
          >
            <div className="mode-icon">🎲</div>
            <h3 className="mode-title">Batalla Aleatoria</h3>
            <p className="mode-description">
              El sistema genera automáticamente dos equipos con 3 Pokémon aleatorios cada uno
            </p>
            <div className="mode-features">
              <span className="feature">⚡ Acción instantánea</span>
              <span className="feature">🎲 Completamente aleatorio</span>
              <span className="feature">🚀 Sin configuración</span>
            </div>
          </div>
        </div>
      </div>

      {error && (
        <div className="alert alert-error">
          <span className="alert-icon">❌</span>
          {error}
        </div>
      )}

      {/* Trainer Mode Content */}
      {battleMode === "trainers" && !error && entrenadores.length >= 2 && (
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
            
            {/* Search Bar */}
            <div className="search-section">
              <div className="search-bar">
                <input
                  type="text"
                  placeholder="Buscar entrenador por nombre..."
                  value={searchTerm}
                  onChange={handleSearchChange}
                  className="search-input"
                />
                {searchTerm && (
                  <button
                    onClick={() => {
                      setSearchTerm("");
                      setCurrentPage(1);
                    }}
                    className="clear-search-btn"
                  >
                    ✕
                  </button>
                )}
              </div>
              <div className="search-results">
                {filteredEntrenadores.length} entrenador{filteredEntrenadores.length !== 1 ? 'es' : ''}
                {searchTerm && ' encontrado' + (filteredEntrenadores.length !== 1 ? 's' : '')}
              </div>
            </div>
            
            {/* No Results Message */}
            {filteredEntrenadores.length === 0 && searchTerm && (
              <div className="no-results">
                <p>No se encontraron entrenadores que coincidan con "{searchTerm}"</p>
                <button 
                  className="btn btn-secondary"
                  onClick={() => {
                    setSearchTerm("");
                    setCurrentPage(1);
                  }}
                >
                  Limpiar búsqueda
                </button>
              </div>
            )}
            
            {filteredEntrenadores.length > 0 && (
              <>
                <div className="trainers-grid">
              {currentTrainers.map((trainer) => {
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
                      <button
                        className="btn btn-info btn-sm"
                        onClick={() => handleShowDetails(trainer)}
                      >
                        <span className="btn-icon">📋</span>
                        Ver Detalles
                      </button>
                    </div>
                  </div>
                );
              })}
            </div>

            {/* Pagination Controls */}
            <div className="pagination-controls">
              <div className="pagination-info">
                Página {currentPage} de {totalPages} 
                ({indexOfFirstTrainer + 1}-{Math.min(indexOfLastTrainer, filteredEntrenadores.length)} de {filteredEntrenadores.length})
              </div>
              
              <div className="pagination-buttons">
                <button
                  className="btn btn-pagination"
                  onClick={() => paginate(currentPage - 1)}
                  disabled={currentPage === 1}
                >
                  ◀️ Anterior
                </button>
                
                {/* Page Numbers */}
                <div className="page-numbers">
                  {[...Array(totalPages)].map((_, index) => {
                    const pageNumber = index + 1;
                    const isCurrentPage = pageNumber === currentPage;
                    
                    // Show only a few page numbers around current page
                    if (
                      pageNumber === 1 || 
                      pageNumber === totalPages || 
                      (pageNumber >= currentPage - 1 && pageNumber <= currentPage + 1)
                    ) {
                      return (
                        <button
                          key={pageNumber}
                          onClick={() => paginate(pageNumber)}
                          className={`btn btn-pagination ${isCurrentPage ? 'active' : ''}`}
                        >
                          {pageNumber}
                        </button>
                      );
                    } else if (pageNumber === currentPage - 2 || pageNumber === currentPage + 2) {
                      return <span key={pageNumber} className="pagination-ellipsis">...</span>;
                    }
                    return null;
                  })}
                </div>
                
                <button
                  className="btn btn-pagination"
                  onClick={() => paginate(currentPage + 1)}
                  disabled={currentPage === totalPages}
                >
                  Siguiente ▶️
                </button>
              </div>
            </div>
              </>
            )}
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

      {/* Random Battle Mode Content */}
      {battleMode === "random" && (
        <div className="random-battle-section">
          <div className="random-battle-card">
            <div className="random-battle-header">
              <div className="random-icon">🎲</div>
              <h2 className="random-title">Batalla Aleatoria</h2>
              <p className="random-description">
                ¡Prepárate para una batalla completamente impredecible! El sistema seleccionará 
                automáticamente 6 Pokémon aleatorios y los dividirá en dos equipos de 3 cada uno.
              </p>
            </div>
            
            <div className="random-battle-features">
              <div className="feature-grid">
                <div className="feature-item">
                  <span className="feature-icon">⚡</span>
                  <span className="feature-text">Acción instantánea</span>
                </div>
                <div className="feature-item">
                  <span className="feature-icon">🎯</span>
                  <span className="feature-text">Sin configuración</span>
                </div>
                <div className="feature-item">
                  <span className="feature-icon">🔥</span>
                  <span className="feature-text">Pura emoción</span>
                </div>
                <div className="feature-item">
                  <span className="feature-icon">🎲</span>
                  <span className="feature-text">Completamente aleatorio</span>
                </div>
              </div>
            </div>

            <div className="random-battle-action">
              <button
                className="btn btn-primary btn-lg"
                onClick={handleRandomBattle}
                disabled={isCreatingRandomBattle}
              >
                {isCreatingRandomBattle ? (
                  <>
                    <div className="loading-spinner inline"></div>
                    <span>Generando batalla...</span>
                  </>
                ) : (
                  <>
                    <span className="btn-icon">🎲</span>
                    <span>¡Generar Batalla Aleatoria!</span>
                  </>
                )}
              </button>
              
              <div className="random-battle-hint">
                <small className="form-hint">
                  🎯 El sistema elegirá automáticamente 6 Pokémon de la base de datos
                </small>
              </div>
            </div>
          </div>
        </div>
      )}

      {/* No trainers available for trainer mode */}
      {battleMode === "trainers" && !error && entrenadores.length < 2 && (
        <div className="empty-state">
          <div className="empty-icon">👥</div>
          <h3>No hay suficientes entrenadores</h3>
          <p>Necesitas al menos 2 entrenadores para comenzar una batalla de entrenadores.</p>
          <div className="empty-state-actions">
            <button 
              className="btn btn-primary"
              onClick={() => navigate("/crear-entrenador")}
            >
              <span className="btn-icon">➕</span>
              Crear Entrenador
            </button>
            <button 
              className="btn btn-secondary"
              onClick={() => setBattleMode("random")}
            >
              <span className="btn-icon">🎲</span>
              Probar Batalla Aleatoria
            </button>
          </div>
        </div>
      )}

      {/* Modal de Detalles del Entrenador */}
      {showDetailsModal && (
        <div className="modal-overlay" onClick={handleCloseDetails}>
          <div className="modal-content trainer-details-modal" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">
                <span className="title-icon">👨‍💼</span>
                Detalles del Entrenador
              </h2>
              <button className="modal-close-btn" onClick={handleCloseDetails}>
                <span className="close-icon">✕</span>
              </button>
            </div>
            
            <div className="modal-body">
              {selectedTrainerDetails && (
                <div className="trainer-details-content">
                  <div className="trainer-info-header">
                    <h3 className="trainer-name-large">{selectedTrainerDetails.nombre}</h3>
                  </div>
                  
                  {loadingDetails ? (
                    <div className="loading-details">
                      <div className="loading-spinner"></div>
                      <p>Cargando detalles del equipo...</p>
                    </div>
                  ) : (
                    <div className="pokemon-details-grid">
                      {trainerPokemonDetails.map((pokemon, index) => (
                        <div key={pokemon.id} className="pokemon-detail-card">
                          <div className="pokemon-detail-header">
                            <div className="pokemon-sprite-large">
                              <img
                                src={`data:image/png;base64,${pokemon.sprite}`}
                                alt={pokemon.nombre}
                                className="pokemon-image-large"
                              />
                            </div>
                            <div className="pokemon-basic-info">
                              <h4 className="pokemon-name-large">{pokemon.nombre}</h4>
                              <div className={`type-badge-large type-${pokemon.tipoPokemon.toLowerCase()}`}>
                                {getTypeIcon(pokemon.tipoPokemon)} {pokemon.tipoPokemon}
                              </div>
                            </div>
                          </div>
                          
                          <div className="pokemon-stats-section">
                            <h5 className="stats-title">
                              <span className="stats-icon">📊</span>
                              Estadísticas
                            </h5>
                            <div className="stats-grid">
                              <div className="stat-item">
                                <span className="stat-label">❤️ Vida</span>
                                <span className="stat-value">{pokemon.vida}</span>
                              </div>
                              <div className="stat-item">
                                <span className="stat-label">⚔️ Ataque</span>
                                <span className="stat-value">{pokemon.ataque}</span>
                              </div>
                              <div className="stat-item">
                                <span className="stat-label">🛡️ Defensa</span>
                                <span className="stat-value">{pokemon.defensa}</span>
                              </div>
                            </div>
                          </div>
                          
                          <div className="pokemon-attacks-section">
                            <h5 className="attacks-title">
                              <span className="attacks-icon">⚡</span>
                              Ataques
                            </h5>
                            <div className="attacks-list">
                              {pokemon.ataques && pokemon.ataques.length > 0 ? (
                                pokemon.ataques.map(ataque => (
                                  <div key={ataque.id} className="attack-detail-item">
                                    <div className="attack-info">
                                      <span className="attack-name">{ataque.nombre}</span>
                                      <span className="attack-type">{ataque.tipoAtaque}</span>
                                    </div>
                                    <div className="attack-stats">
                                      <span className="attack-power">
                                        <span className="power-icon">⚡</span>
                                        {ataque.potencia}
                                      </span>
                                    </div>
                                  </div>
                                ))
                              ) : (
                                <div className="no-data">
                                  <span className="no-data-icon">❌</span>
                                  Sin ataques disponibles
                                </div>
                              )}
                            </div>
                          </div>
                          
                          <div className="pokemon-effects-section">
                            <h5 className="effects-title">
                              <span className="effects-icon">✨</span>
                              Efectos Especiales
                            </h5>
                            <div className="effects-list">
                              {pokemon.efectos && pokemon.efectos.length > 0 ? (
                                pokemon.efectos.map((efecto, efectoIndex) => (
                                  <div key={efecto.id || efectoIndex} className="effect-detail-item">
                                    <div className="effect-info">
                                      <span className="effect-type">{efecto.nombre || 'Tipo desconocido'}</span>
                                      <span className="effect-description">{efecto.descripcion || 'Sin descripción'}</span>
                                    </div>
                                  </div>
                                ))
                              ) : (
                                <div className="no-data">
                                  <span className="no-data-icon">❌</span>
                                  Sin efectos especiales
                                </div>
                              )}
                            </div>
                          </div>
                        </div>
                      ))}
                    </div>
                  )}
                </div>
              )}
            </div>
            
            <div className="modal-footer">
              <button className="btn btn-secondary" onClick={handleCloseDetails}>
                <span className="btn-icon">📝</span>
                Cerrar
              </button>
            </div>
          </div>
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
