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
  const [effectsTrainer1, setEffectsTrainer1] = useState([]);
  const [effectsTrainer2, setEffectsTrainer2] = useState([]);

  const [livesTrainer1, setLivesTrainer1] = useState([0, 0, 0]);
  const [livesTrainer2, setLivesTrainer2] = useState([0, 0, 0]);
  const [vidaMaxE1, setVidaMaxE1] = useState([0, 0, 0]);
  const [vidaMaxE2, setVidaMaxE2] = useState([0, 0, 0]);

  const [selectedAttackerE1, setSelectedAttackerE1] = useState(null);
  const [selectedAttackerE2, setSelectedAttackerE2] = useState(null);
  const [selectedTargetE1, setSelectedTargetE1] = useState(null);
  const [selectedTargetE2, setSelectedTargetE2] = useState(null);

  // Estados para ataques seleccionados
  const [selectedAttackE1, setSelectedAttackE1] = useState(null);
  const [selectedAttackE2, setSelectedAttackE2] = useState(null);

  const [useEffectE1, setUseEffectE1] = useState(false);
  const [useEffectE2, setUseEffectE2] = useState(false);

  const [turn, setTurn] = useState(1);
  const [loading, setLoading] = useState(true);
  const [battleInProgress, setBattleInProgress] = useState(false);
  const [battleLog, setBattleLog] = useState([]);
  const [winner, setWinner] = useState(null);

  // Current team based on turn
  const currentTeam = turn % 2 === 1 ? 1 : 2;
  const isTeam1Turn = currentTeam === 1;

  useEffect(() => {
    const initializeBattle = async () => {
      try {
        // Load Pokemon data for trainer 1
        const responses1 = await Promise.all([
          pokemonService.getById(selectedTrainer1.idPokemon1),
          pokemonService.getById(selectedTrainer1.idPokemon2),
          pokemonService.getById(selectedTrainer1.idPokemon3),
        ]);
        
        const pokemonData1 = responses1.map((response) => response.data);
        setPokemonDataTrainer1(pokemonData1);
        
        const newLivesTrainer1 = pokemonData1.map((pokemon) => pokemon.vida);
        setLivesTrainer1(newLivesTrainer1);
        setVidaMaxE1(newLivesTrainer1);

        // Load Pokemon data for trainer 2
        const responses2 = await Promise.all([
          pokemonService.getById(selectedTrainer2.idPokemon1),
          pokemonService.getById(selectedTrainer2.idPokemon2),
          pokemonService.getById(selectedTrainer2.idPokemon3),
        ]);
        
        const pokemonData2 = responses2.map((response) => response.data);
        setPokemonDataTrainer2(pokemonData2);
        
        const newLivesTrainer2 = pokemonData2.map((pokemon) => pokemon.vida);
        setLivesTrainer2(newLivesTrainer2);
        setVidaMaxE2(newLivesTrainer2);

        // Load attacks for trainer 1
        const attackResponses1 = await Promise.all(
          pokemonData1.map((pokemon) => pokemonService.getAtaques(pokemon.id))
        );
        setAttacksTrainer1(attackResponses1.map((response) => response.data));

        // Load attacks for trainer 2
        const attackResponses2 = await Promise.all(
          pokemonData2.map((pokemon) => pokemonService.getAtaques(pokemon.id))
        );
        setAttacksTrainer2(attackResponses2.map((response) => response.data));

        // Load effects for trainer 1
        const effectResponses1 = await Promise.all(
          pokemonData1.map((pokemon) => pokemonService.getEfecto(pokemon.id))
        );
        setEffectsTrainer1(effectResponses1.map((response) => response.data));

        // Load effects for trainer 2
        const effectResponses2 = await Promise.all(
          pokemonData2.map((pokemon) => pokemonService.getEfecto(pokemon.id))
        );
        setEffectsTrainer2(effectResponses2.map((response) => response.data));

        setBattleLog([
          `¡La batalla entre ${selectedTrainer1.nombre} y ${selectedTrainer2.nombre} ha comenzado!`,
          `${selectedTrainer1.nombre} inicia el combate.`
        ]);
        
      } catch (error) {
        console.error("Error al inicializar la batalla:", error);
      } finally {
        setLoading(false);
      }
    };

    initializeBattle();
  }, [selectedTrainer1, selectedTrainer2]);

  const handlePokemonAction = (trainerTeam, pokemonIndex, useEffect) => {
    if (trainerTeam === 1) {
      setSelectedAttackerE1(pokemonIndex);
      setUseEffectE1(useEffect);
      setSelectedAttackE1(null); // Reset attack selection
      if (useEffect !== useEffectE1) {
        setSelectedTargetE1(null);
      }
    } else {
      setSelectedAttackerE2(pokemonIndex);
      setUseEffectE2(useEffect);
      setSelectedAttackE2(null); // Reset attack selection
      if (useEffect !== useEffectE2) {
        setSelectedTargetE2(null);
      }
    }
  };

  const handleAttackSelection = (trainerTeam, attackIndex) => {
    if (trainerTeam === 1) {
      setSelectedAttackE1(attackIndex);
    } else {
      setSelectedAttackE2(attackIndex);
    }
  };

  const handleTargetSelection = (targetTeam, targetIndex) => {
    if (isTeam1Turn) {
      setSelectedTargetE1(targetIndex);
    } else {
      setSelectedTargetE2(targetIndex);
    }
  };

  const canExecuteAction = () => {
    if (isTeam1Turn) {
      return selectedAttackerE1 !== null && 
             (useEffectE1 || (selectedTargetE1 !== null && selectedAttackE1 !== null)) &&
             livesTrainer1[selectedAttackerE1] > 0;
    } else {
      return selectedAttackerE2 !== null && 
             (useEffectE2 || (selectedTargetE2 !== null && selectedAttackE2 !== null)) &&
             livesTrainer2[selectedAttackerE2] > 0;
    }
  };

  const executeAction = async () => {
    if (!canExecuteAction()) {
      return;
    }

    setBattleInProgress(true);

    try {
      // Update Pokemon lives in the data
      // Prepare batalla data maintaining all pokemon properties (including modified stats)
      const updatedEntrenador1 = pokemonDataTrainer1.map((pokemon, index) => ({
        ...pokemon,
        vida: livesTrainer1[index],
        // Ensure modified stats are preserved if they exist
        ataqueModificado: pokemon.ataqueModificado || pokemon.ataque,
        defensaModificada: pokemon.defensaModificada || pokemon.defensa,
      }));

      const updatedEntrenador2 = pokemonDataTrainer2.map((pokemon, index) => ({
        ...pokemon,
        vida: livesTrainer2[index],
        // Ensure modified stats are preserved if they exist
        ataqueModificado: pokemon.ataqueModificado || pokemon.ataque,
        defensaModificada: pokemon.defensaModificada || pokemon.defensa,
      }));

      // Determinar atacante y receptor basado en el turno actual
      const posicionAtacante = isTeam1Turn ? selectedAttackerE1 : selectedAttackerE2;
      const posicionReceptor = isTeam1Turn ? selectedTargetE1 : selectedTargetE2;
      const useEffect = isTeam1Turn ? useEffectE1 : useEffectE2;
      
      // Obtener ataque seleccionado o usar el primero por defecto
      const ataqueSeleccionado = isTeam1Turn ? 
        (selectedAttackE1 !== null ? selectedAttackE1 : 0) : 
        (selectedAttackE2 !== null ? selectedAttackE2 : 0);
      
      // Obtener el ataque y efecto correspondiente al equipo que está atacando
      const ataque = isTeam1Turn ? 
        (attacksTrainer1[selectedAttackerE1] ? attacksTrainer1[selectedAttackerE1][ataqueSeleccionado] : null) :
        (attacksTrainer2[selectedAttackerE2] ? attacksTrainer2[selectedAttackerE2][ataqueSeleccionado] : null);
        
      const efecto = isTeam1Turn ? 
        effectsTrainer1[selectedAttackerE1] : 
        effectsTrainer2[selectedAttackerE2];

      const batallaDTO = {
        entrenador1: updatedEntrenador1,
        entrenador2: updatedEntrenador2,
        ataqueE1: isTeam1Turn && !useEffect ? ataque : null,
        ataqueE2: !isTeam1Turn && !useEffect ? ataque : null,
        efectoE1: isTeam1Turn && useEffect ? efecto : null,
        efectoE2: !isTeam1Turn && useEffect ? efecto : null,
        usarEfectoE1: isTeam1Turn && useEffect,
        usarEfectoE2: !isTeam1Turn && useEffect,
        turno: turn,
      };

      const response = await batallaService.combatir(
        posicionAtacante || 0,
        posicionReceptor || 0,
        batallaDTO
      );

      // Update the pokemon data with response from backend (includes modified stats)
      setPokemonDataTrainer1(response.data.entrenador1);
      setPokemonDataTrainer2(response.data.entrenador2);

      // Update lives
      const newLivesTrainer1 = response.data.entrenador1.map((pokemon) => pokemon.vida);
      const newLivesTrainer2 = response.data.entrenador2.map((pokemon) => pokemon.vida);
      
      setLivesTrainer1(newLivesTrainer1);
      setLivesTrainer2(newLivesTrainer2);

      // Add to battle log
      const currentTrainer = isTeam1Turn ? selectedTrainer1.nombre : selectedTrainer2.nombre;
      const action = isTeam1Turn ? 
        (useEffectE1 ? `usó ${effectsTrainer1[selectedAttackerE1]?.nombre}` : `atacó con ${ataque?.nombre || 'Ataque'}`) :
        (useEffectE2 ? `usó ${effectsTrainer2[selectedAttackerE2]?.nombre}` : `atacó con ${ataque?.nombre || 'Ataque'}`);
      
      setBattleLog(prev => [...prev, `Turno ${turn}: ${currentTrainer} ${action}`]);

      // Check for winner
      const isTrainer1Lost = newLivesTrainer1.every((vida) => vida <= 0);
      const isTrainer2Lost = newLivesTrainer2.every((vida) => vida <= 0);

      if (isTrainer1Lost || isTrainer2Lost) {
        const winnerTrainer = isTrainer1Lost ? selectedTrainer2.nombre : selectedTrainer1.nombre;
        setWinner(winnerTrainer);
        setBattleLog(prev => [...prev, `¡${winnerTrainer} ha ganado la batalla!`]);
      } else {
        setTurn(turn + 1);
      }

      // Reset selections
      setSelectedAttackerE1(null);
      setSelectedAttackerE2(null);
      setSelectedTargetE1(null);
      setSelectedTargetE2(null);
      setUseEffectE1(false);
      setUseEffectE2(false);

    } catch (error) {
      console.error("Error durante la batalla:", error);
      setBattleLog(prev => [...prev, "Error durante la batalla"]);
    } finally {
      setBattleInProgress(false);
    }
  };

  const resetBattle = () => {
    navigate("/setup");
  };

  if (loading) {
    return (
      <div className="page-container">
        <div className="loading-container">
          <div className="loading-spinner"></div>
          <p>Preparando la batalla...</p>
        </div>
      </div>
    );
  }

  if (winner) {
    return (
      <div className="page-container">
        <div className="victory-screen">
          <div className="victory-content">
            <h1 className="victory-title">🏆 ¡VICTORIA! 🏆</h1>
            <h2 className="winner-name">{winner}</h2>
            <p className="victory-message">¡Ha ganado la batalla épica!</p>
            
            <div className="battle-summary">
              <h3>Resumen de la Batalla</h3>
              <div className="final-stats">
                <div className="trainer-final-stats">
                  <h4>{selectedTrainer1.nombre}</h4>
                  <div className="pokemon-final-lives">
                    {pokemonDataTrainer1.map((pokemon, index) => (
                      <div key={pokemon.id} className="pokemon-final-stat">
                        <img src={`data:image/png;base64,${pokemon.sprite}`} alt={pokemon.nombre} />
                        <span className={livesTrainer1[index] > 0 ? 'alive' : 'fainted'}>
                          {pokemon.nombre}: {livesTrainer1[index]}/{vidaMaxE1[index]} HP
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
                
                <div className="trainer-final-stats">
                  <h4>{selectedTrainer2.nombre}</h4>
                  <div className="pokemon-final-lives">
                    {pokemonDataTrainer2.map((pokemon, index) => (
                      <div key={pokemon.id} className="pokemon-final-stat">
                        <img src={`data:image/png;base64,${pokemon.sprite}`} alt={pokemon.nombre} />
                        <span className={livesTrainer2[index] > 0 ? 'alive' : 'fainted'}>
                          {pokemon.nombre}: {livesTrainer2[index]}/{vidaMaxE2[index]} HP
                        </span>
                      </div>
                    ))}
                  </div>
                </div>
              </div>
            </div>

            <div className="victory-actions">
              <button className="btn btn-primary btn-lg" onClick={resetBattle}>
                <span className="btn-icon">🔄</span>
                Nueva Batalla
              </button>
              <button className="btn btn-secondary btn-lg" onClick={() => navigate("/")}>
                <span className="btn-icon">🏠</span>
                Volver al Inicio
              </button>
            </div>
          </div>
        </div>
      </div>
    );
  }

  return (
    <div className="page-container battle-page">
      {/* Battle Header */}
      <div className="battle-header">
        <div className="battle-title-section">
          <h1 className="battle-title">
            <span className="title-icon">⚔️</span>
            Batalla Pokémon
          </h1>
          <div className="battle-info">
            <div className="turn-indicator">
              <span className="turn-number">Turno {turn}</span>
              <span className={`current-player ${isTeam1Turn ? 'team1' : 'team2'}`}>
                {isTeam1Turn ? selectedTrainer1.nombre : selectedTrainer2.nombre}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Battle Arena */}
      <div className="battle-arena">
        {/* Trainer 1 Side */}
        <div className={`trainer-battlefield ${isTeam1Turn ? 'active-turn' : 'waiting-turn'}`}>
          <div className="trainer-info">
            <h2 className="trainer-name">
              <span className="trainer-icon">👨‍💼</span>
              {selectedTrainer1.nombre}
            </h2>
            {isTeam1Turn && (
              <div className="turn-indicator-badge">
                <span className="badge-icon">⚡</span>
                Tu turno
              </div>
            )}
          </div>
          
          <div className="pokemon-team">
            {pokemonDataTrainer1.map((pokemon, index) => {
              const isAlive = livesTrainer1[index] > 0;
              const isSelected = selectedAttackerE1 === index && isTeam1Turn;
              const healthPercentage = (livesTrainer1[index] / vidaMaxE1[index]) * 100;
              
              return (
                <div 
                  key={pokemon.id} 
                  className={`pokemon-battle-card ${!isAlive ? 'fainted' : ''} ${isSelected ? 'selected' : ''}`}
                >
                  <div className="pokemon-image-container">
                    <img
                      src={`data:image/png;base64,${pokemon.sprite}`}
                      alt={pokemon.nombre}
                      className="pokemon-battle-sprite"
                    />
                    {!isAlive && <div className="fainted-overlay">💀</div>}
                    {isSelected && <div className="selection-glow"></div>}
                  </div>
                  
                  <div className="pokemon-battle-info">
                    <h4 className="pokemon-battle-name">{pokemon.nombre}</h4>
                    <div className={`type-badge type-${pokemon.tipoPokemon.toLowerCase()}`}>
                      {getTypeIcon(pokemon.tipoPokemon)}
                    </div>
                    
                    <div className="health-section">
                      <div className="health-bar">
                        <div 
                          className={`health-fill ${healthPercentage <= 25 ? 'critical' : healthPercentage <= 50 ? 'warning' : 'healthy'}`}
                          style={{width: `${Math.max(0, healthPercentage)}%`}}
                        ></div>
                      </div>
                      <span className="health-text">
                        {livesTrainer1[index]} / {vidaMaxE1[index]} HP
                      </span>
                    </div>
                  </div>

                  {/* Action Selection for Team 1 */}
                  {isTeam1Turn && isAlive && (
                    <div className="action-selection">
                      <div className="action-type-toggle">
                        <button
                          className={`action-toggle-btn ${(!useEffectE1 || selectedAttackerE1 !== index) ? 'active' : ''}`}
                          onClick={() => handlePokemonAction(1, index, false)}
                        >
                          <span className="btn-icon">⚔️</span>
                          Atacar
                        </button>
                        <button
                          className={`action-toggle-btn ${(useEffectE1 && selectedAttackerE1 === index) ? 'active' : ''}`}
                          onClick={() => handlePokemonAction(1, index, true)}
                        >
                          <span className="btn-icon">✨</span>
                          Efecto
                        </button>
                      </div>

                      {selectedAttackerE1 === index && (
                        <div className="actions-list">
                          {useEffectE1 ? (
                            effectsTrainer1[index] && (
                              <div className="effect-item">
                                <span className="effect-name">{effectsTrainer1[index].nombre}</span>
                                <span className="effect-description">{effectsTrainer1[index].descripcion}</span>
                              </div>
                            )
                          ) : (
                            <div className="attacks-selection">
                              <h5 className="selection-title">Selecciona un ataque:</h5>
                              {attacksTrainer1[index]?.map((ataque, attackIndex) => (
                                <button
                                  key={ataque.id}
                                  className={`attack-selection-btn ${selectedAttackE1 === attackIndex ? 'selected' : ''}`}
                                  onClick={() => handleAttackSelection(1, attackIndex)}
                                >
                                  <div className="attack-item">
                                    <span className="attack-name">{ataque.nombre}</span>
                                    <div className={`attack-type type-${ataque.tipoAtaque.toLowerCase()}`}>
                                      {getTypeIcon(ataque.tipoAtaque)}
                                    </div>
                                    <span className="attack-power">Pot: {ataque.potencia}</span>
                                  </div>
                                </button>
                              ))}
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>

        {/* VS Indicator */}
        <div className="vs-section">
          <div className="vs-indicator">
            <span className="vs-text">VS</span>
          </div>
        </div>

        {/* Trainer 2 Side */}
        <div className={`trainer-battlefield ${!isTeam1Turn ? 'active-turn' : 'waiting-turn'}`}>
          <div className="trainer-info">
            <h2 className="trainer-name">
              <span className="trainer-icon">👩‍💼</span>
              {selectedTrainer2.nombre}
            </h2>
            {!isTeam1Turn && (
              <div className="turn-indicator-badge">
                <span className="badge-icon">⚡</span>
                Tu turno
              </div>
            )}
          </div>
          
          <div className="pokemon-team">
            {pokemonDataTrainer2.map((pokemon, index) => {
              const isAlive = livesTrainer2[index] > 0;
              const isSelected = selectedAttackerE2 === index && !isTeam1Turn;
              const healthPercentage = (livesTrainer2[index] / vidaMaxE2[index]) * 100;
              
              return (
                <div 
                  key={pokemon.id} 
                  className={`pokemon-battle-card ${!isAlive ? 'fainted' : ''} ${isSelected ? 'selected' : ''}`}
                >
                  <div className="pokemon-image-container">
                    <img
                      src={`data:image/png;base64,${pokemon.sprite}`}
                      alt={pokemon.nombre}
                      className="pokemon-battle-sprite"
                    />
                    {!isAlive && <div className="fainted-overlay">💀</div>}
                    {isSelected && <div className="selection-glow"></div>}
                  </div>
                  
                  <div className="pokemon-battle-info">
                    <h4 className="pokemon-battle-name">{pokemon.nombre}</h4>
                    <div className={`type-badge type-${pokemon.tipoPokemon.toLowerCase()}`}>
                      {getTypeIcon(pokemon.tipoPokemon)}
                    </div>
                    
                    <div className="health-section">
                      <div className="health-bar">
                        <div 
                          className={`health-fill ${healthPercentage <= 25 ? 'critical' : healthPercentage <= 50 ? 'warning' : 'healthy'}`}
                          style={{width: `${Math.max(0, healthPercentage)}%`}}
                        ></div>
                      </div>
                      <span className="health-text">
                        {livesTrainer2[index]} / {vidaMaxE2[index]} HP
                      </span>
                    </div>
                  </div>

                  {/* Action Selection for Team 2 */}
                  {!isTeam1Turn && isAlive && (
                    <div className="action-selection">
                      <div className="action-type-toggle">
                        <button
                          className={`action-toggle-btn ${(!useEffectE2 || selectedAttackerE2 !== index) ? 'active' : ''}`}
                          onClick={() => handlePokemonAction(2, index, false)}
                        >
                          <span className="btn-icon">⚔️</span>
                          Atacar
                        </button>
                        <button
                          className={`action-toggle-btn ${(useEffectE2 && selectedAttackerE2 === index) ? 'active' : ''}`}
                          onClick={() => handlePokemonAction(2, index, true)}
                        >
                          <span className="btn-icon">✨</span>
                          Efecto
                        </button>
                      </div>

                      {selectedAttackerE2 === index && (
                        <div className="actions-list">
                          {useEffectE2 ? (
                            effectsTrainer2[index] && (
                              <div className="effect-item">
                                <span className="effect-name">{effectsTrainer2[index].nombre}</span>
                                <span className="effect-description">{effectsTrainer2[index].descripcion}</span>
                              </div>
                            )
                          ) : (
                            <div className="attacks-selection">
                              <h5 className="selection-title">Selecciona un ataque:</h5>
                              {attacksTrainer2[index]?.map((ataque, attackIndex) => (
                                <button
                                  key={ataque.id}
                                  className={`attack-selection-btn ${selectedAttackE2 === attackIndex ? 'selected' : ''}`}
                                  onClick={() => handleAttackSelection(2, attackIndex)}
                                >
                                  <div className="attack-item">
                                    <span className="attack-name">{ataque.nombre}</span>
                                    <div className={`attack-type type-${ataque.tipoAtaque.toLowerCase()}`}>
                                      {getTypeIcon(ataque.tipoAtaque)}
                                    </div>
                                    <span className="attack-power">Pot: {ataque.potencia}</span>
                                  </div>
                                </button>
                              ))}
                            </div>
                          )}
                        </div>
                      )}
                    </div>
                  )}
                </div>
              );
            })}
          </div>
        </div>
      </div>

      {/* Target Selection */}
      {((isTeam1Turn && selectedAttackerE1 !== null && !useEffectE1) || 
        (!isTeam1Turn && selectedAttackerE2 !== null && !useEffectE2)) && (
        <div className="target-selection">
          <h3 className="target-title">
            <span className="title-icon">🎯</span>
            Selecciona el objetivo del ataque
          </h3>
          
          <div className="target-options">
            {(isTeam1Turn ? pokemonDataTrainer2 : pokemonDataTrainer1).map((pokemon, index) => {
              const lives = isTeam1Turn ? livesTrainer2 : livesTrainer1;
              const maxLives = isTeam1Turn ? vidaMaxE2 : vidaMaxE1;
              const isAlive = lives[index] > 0;
              const isSelected = isTeam1Turn ? selectedTargetE1 === index : selectedTargetE2 === index;
              
              return (
                <button
                  key={pokemon.id}
                  className={`target-option ${isSelected ? 'selected' : ''} ${!isAlive ? 'disabled' : ''}`}
                  onClick={() => isAlive && handleTargetSelection(isTeam1Turn ? 2 : 1, index)}
                  disabled={!isAlive}
                >
                  <img
                    src={`data:image/png;base64,${pokemon.sprite}`}
                    alt={pokemon.nombre}
                    className="target-sprite"
                  />
                  <div className="target-info">
                    <span className="target-name">{pokemon.nombre}</span>
                    <span className="target-health">{lives[index]}/{maxLives[index]} HP</span>
                  </div>
                  {!isAlive && <div className="fainted-indicator">💀</div>}
                </button>
              );
            })}
          </div>
        </div>
      )}

      {/* Action Button */}
      <div className="battle-actions">
        <button
          className={`btn btn-primary btn-lg ${!canExecuteAction() || battleInProgress ? 'btn-disabled' : ''}`}
          onClick={executeAction}
          disabled={!canExecuteAction() || battleInProgress}
        >
          {battleInProgress ? (
            <>
              <div className="btn-spinner"></div>
              Ejecutando...
            </>
          ) : (
            <>
              <span className="btn-icon">⚡</span>
              {isTeam1Turn ? 
                (useEffectE1 ? 'Usar Efecto' : 'Atacar') : 
                (useEffectE2 ? 'Usar Efecto' : 'Atacar')
              }
            </>
          )}
        </button>
        
        <button className="btn btn-secondary" onClick={resetBattle}>
          <span className="btn-icon">🏠</span>
          Volver a Selección
        </button>
      </div>

      {/* Battle Log */}
      <div className="battle-log">
        <h3 className="log-title">
          <span className="title-icon">📋</span>
          Registro de Batalla
        </h3>
        <div className="log-content">
          {battleLog.map((entry, index) => (
            <div key={index} className="log-entry">
              <span className="log-number">{index + 1}.</span>
              <span className="log-text">{entry}</span>
            </div>
          ))}
        </div>
      </div>
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
    ELECTRICO: "⚡",
    NORMAL: "⭐"
  };
  return icons[tipo] || "❓";
};

export default BattleView;
