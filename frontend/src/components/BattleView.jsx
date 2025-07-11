import React, { useState, useEffect, useRef } from "react";
import { useLocation, useNavigate } from "react-router-dom";
import pokemonService from "../Services/pokemon.service";
import batallaService from "../services/batalla.service";
import entrenadorService from "../services/entrenador.service";
import cpuService from "../services/cpu.service";
import AudioManager from "./AudioManager";
import AudioControls from "./AudioControls";
import { useAudioContext } from "../contexts/AudioContext.jsx";
import "../App.css";

const BattleView = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const { mainTheme } = useAudioContext();

  const { selectedTrainer1, selectedTrainer2, randomBattle, isRandomBattle, isCpuBattle, cpuDifficulty, cpuIsTeam1 } = location.state || {};

  if (!isRandomBattle && !isCpuBattle && (!selectedTrainer1 || !selectedTrainer2)) {
    navigate("/setup");
  }

  if (isRandomBattle && !randomBattle) {
    navigate("/setup");
  }

  if (isCpuBattle && !selectedTrainer1) {
    navigate("/setup");
  }

  // Define team names based on battle type
  const teamName1 = isRandomBattle ? randomBattle?.nombreEquipo1 : selectedTrainer1?.nombre;
  const teamName2 = isRandomBattle 
    ? randomBattle?.nombreEquipo2 
    : (isCpuBattle ? `CPU (${cpuDifficulty || 'NORMAL'})` : selectedTrainer2?.nombre);

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
  const [cpuTurnProcessing, setCpuTurnProcessing] = useState(false);

  // Estados para efectos de equipo (DANO_CONTINUO)
  const [teamEffects, setTeamEffects] = useState({
    team1: { effectId: null, turnsRemaining: 0 },
    team2: { effectId: null, turnsRemaining: 0 }
  });

  // Audio references
  const audioRef = useRef(null);
  const [audioControls, setAudioControls] = useState(null);
  const [battleMusicPlaying, setBattleMusicPlaying] = useState(false);

  // Current team based on turn
  const currentTeam = turn % 2 === 1 ? 1 : 2;
  const isTeam1Turn = currentTeam === 1;

  // Audio ready handler
  const handleAudioReady = (controls) => {
    setAudioControls(controls);
    audioRef.current = controls;
    
    // Detener música principal al entrar en batalla
    if (mainTheme.isPlaying) {
      mainTheme.stop();
    }
    
    // Start battle music when audio is ready
    if (controls.isAudioEnabled && !battleMusicPlaying && !winner) {
      controls.playBattleMusic();
      setBattleMusicPlaying(true);
    }
  };

  // Handle victory music when winner is set
  useEffect(() => {
    if (winner && audioControls && audioControls.isAudioEnabled) {
      console.log('🏆 Winner detected, preparing victory sequence:', winner);
      
      const playVictorySequence = async () => {
        try {
          // Step 1: Stop battle music immediately
          console.log('🎵 Step 1: Stopping battle music...');
          audioControls.stopBattleMusic();
          setBattleMusicPlaying(false);
          
          // Step 2: Wait a moment for battle music to stop
          await new Promise(resolve => setTimeout(resolve, 200));
          
          // Step 3: Attempt to play victory music
          console.log('🎵 Step 3: Playing victory music...');
          const victorySuccess = await audioControls.playVictoryMusic();
          
          if (victorySuccess) {
            console.log('✅ Victory music sequence completed successfully');
          } else {
            console.warn('⚠️ Victory music failed, trying manual fallback...');
            // Manual fallback as last resort
            const manualAudio = new Audio('/audio/music/win-theme.mp3');
            manualAudio.volume = 0.6;
            manualAudio.preload = 'auto';
            
            // Wait for it to load
            await new Promise((resolve, reject) => {
              const timeout = setTimeout(() => reject(new Error('Timeout')), 5000);
              manualAudio.addEventListener('canplaythrough', () => {
                clearTimeout(timeout);
                resolve();
              }, { once: true });
              manualAudio.load();
            });
            
            await manualAudio.play();
            console.log('✅ Manual fallback victory music played');
          }
          
        } catch (error) {
          console.error('❌ Complete victory sequence failed:', error);
          
          // Last ditch effort - simple direct play
          try {
            const lastResortAudio = new Audio('/audio/music/win-theme.mp3');
            lastResortAudio.volume = 0.5;
            lastResortAudio.play().then(() => {
              console.log('✅ Last resort audio played');
            }).catch(e => {
              console.error('❌ Even last resort failed:', e);
            });
          } catch (e) {
            console.error('❌ All victory music attempts failed:', e);
          }
        }
      };
      
      // Execute the victory sequence
      playVictorySequence();
    }
  }, [winner, audioControls]);

  const playSoundEffect = (soundType, element = null) => {
    if (audioControls) {
      switch (soundType) {
        case 'attack':
          audioControls.playAttackHit();
          break;
        case 'effect':
          audioControls.playEffectUse();
          break;
        case 'faint':
          audioControls.playPokemonFaint();
          break;
        case 'click':
          audioControls.playButtonClick();
          break;
        default:
          break;
      }

      // Add visual feedback
      if (element) {
        element.classList.add('audio-feedback');
        setTimeout(() => {
          element.classList.remove('audio-feedback');
        }, 300);
      }
    }
  };

  useEffect(() => {
    const initializeBattle = async () => {
      try {
        if (isRandomBattle) {
          // Initialize random battle
          const pokemonData1 = randomBattle.entrenador1;
          const pokemonData2 = randomBattle.entrenador2;
          
          setPokemonDataTrainer1(pokemonData1);
          setPokemonDataTrainer2(pokemonData2);
          
          const newLivesTrainer1 = pokemonData1.map((pokemon) => pokemon.vida);
          const newLivesTrainer2 = pokemonData2.map((pokemon) => pokemon.vida);
          setLivesTrainer1(newLivesTrainer1);
          setLivesTrainer2(newLivesTrainer2);
          setVidaMaxE1(newLivesTrainer1);
          setVidaMaxE2(newLivesTrainer2);

          // Load attacks for team 1
          const attackResponses1 = await Promise.all(
            pokemonData1.map((pokemon) => pokemonService.getAtaques(pokemon.id))
          );
          setAttacksTrainer1(attackResponses1.map((response) => response.data));

          // Load attacks for team 2
          const attackResponses2 = await Promise.all(
            pokemonData2.map((pokemon) => pokemonService.getAtaques(pokemon.id))
          );
          setAttacksTrainer2(attackResponses2.map((response) => response.data));

          // Load effects for team 1
          const effectResponses1 = await Promise.all(
            pokemonData1.map((pokemon) => pokemonService.getEfecto(pokemon.id))
          );
          setEffectsTrainer1(effectResponses1.map((response) => response.data));

          // Load effects for team 2
          const effectResponses2 = await Promise.all(
            pokemonData2.map((pokemon) => pokemonService.getEfecto(pokemon.id))
          );
          setEffectsTrainer2(effectResponses2.map((response) => response.data));

          setBattleLog([
            `¡La batalla aleatoria entre ${teamName1} y ${teamName2} ha comenzado!`,
            `${teamName1} inicia el combate.`
          ]);
        } else if (isCpuBattle) {
          // Initialize CPU battle - need to create a random team for CPU
          console.log("Inicializando batalla CPU...");
          
          // First, load player trainer's Pokemon using the correct endpoint
          const pokemonResponse1 = await entrenadorService.getPokemon(selectedTrainer1.id);
          const pokemonData1 = pokemonResponse1.data;
          
          if (!pokemonData1 || pokemonData1.length === 0) {
            throw new Error("El entrenador seleccionado no tiene Pokémon asignados");
          }
          
          setPokemonDataTrainer1(pokemonData1);
          
          const newLivesTrainer1 = pokemonData1.map((pokemon) => pokemon.vida);
          setLivesTrainer1(newLivesTrainer1);
          setVidaMaxE1(newLivesTrainer1);

          // Load attacks for player team
          const attackResponses1 = await Promise.all(
            pokemonData1.map((pokemon) => pokemonService.getAtaques(pokemon.id))
          );
          setAttacksTrainer1(attackResponses1.map((response) => response.data));

          // Load effects for player team
          const effectResponses1 = await Promise.all(
            pokemonData1.map((pokemon) => pokemonService.getEfecto(pokemon.id))
          );
          setEffectsTrainer1(effectResponses1.map((response) => response.data));

          // Generate random team for CPU
          const randomBattleResponse = await batallaService.createRandomBattleWithMode("TOTAL");
          const cpuTeamData = randomBattleResponse.data.entrenador1; // Use first team for CPU
          
          setPokemonDataTrainer2(cpuTeamData);
          
          const newLivesTrainer2 = cpuTeamData.map((pokemon) => pokemon.vida);
          setLivesTrainer2(newLivesTrainer2);
          setVidaMaxE2(newLivesTrainer2);

          // Load attacks for CPU team
          const attackResponses2 = await Promise.all(
            cpuTeamData.map((pokemon) => pokemonService.getAtaques(pokemon.id))
          );
          setAttacksTrainer2(attackResponses2.map((response) => response.data));

          // Load effects for CPU team
          const effectResponses2 = await Promise.all(
            cpuTeamData.map((pokemon) => pokemonService.getEfecto(pokemon.id))
          );
          setEffectsTrainer2(effectResponses2.map((response) => response.data));

          setBattleLog([
            `¡La batalla entre ${teamName1} y ${teamName2} ha comenzado!`,
            `${teamName1} enfrenta a la CPU en dificultad ${cpuDifficulty}`,
            `${teamName1} inicia el combate.`
          ]);
        } else {
          // Initialize normal battle
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
            `¡La batalla entre ${teamName1} y ${teamName2} ha comenzado!`,
            `${teamName1} inicia el combate.`
          ]);
        }
        
      } catch (error) {
        console.error("Error al inicializar la batalla:", error);
      } finally {
        setLoading(false);
      }
    };

    initializeBattle();
  }, [selectedTrainer1, selectedTrainer2]);

  // useEffect para limpiar estados cuando hay un ganador
  useEffect(() => {
    if (winner) {
      console.log("🏆 Ganador detectado - limpiando estados de CPU");
      setCpuTurnProcessing(false);
      setBattleInProgress(false);
    }
  }, [winner]);

  // Función para manejar el turno de la CPU (llamada directamente, no en useEffect)
  const handleCpuTurnAction = async (forceTurn = null, updatedPokemonData1 = null, updatedPokemonData2 = null) => {
    const currentTurn = forceTurn || turn;
    // Usar datos actualizados si están disponibles, si no usar el estado actual
    const currentPokemonData1 = updatedPokemonData1 || pokemonDataTrainer1;
    const currentPokemonData2 = updatedPokemonData2 || pokemonDataTrainer2;
    
    console.log("🤖 handleCpuTurnAction: Verificando condiciones de entrada");
    console.log("🤖 Estados:", {
      battleInProgress,
      cpuTurnProcessing,
      winner,
      turn,
      forceTurn,
      currentTurn,
      isCpuBattle,
      currentTeam: currentTurn % 2 === 1 ? 1 : 2,
      isTeam1Turn: (currentTurn % 2 === 1),
      isCpuTurn: (currentTurn % 2 === 0),
      usingUpdatedData: updatedPokemonData1 !== null
    });

    if (battleInProgress || cpuTurnProcessing || winner || !isCpuBattle) {
      console.log("🤖 CPU: Turno cancelado - batalla en progreso, CPU procesando, hay ganador, o no es batalla CPU");
      return;
    }

    // Verificar que realmente es turno de la CPU (turno par, equipo 2)
    if (currentTurn % 2 === 1) {
      console.log("🤖 CPU: Turno cancelado - es turno del jugador (turno impar, equipo 1)");
      return;
    }

    try {
      console.log("🤖 CPU: Iniciando turno", currentTurn);
      setCpuTurnProcessing(true);
      setBattleInProgress(true);
      
      // Crear el estado de batalla actual para enviar a la CPU
      const batallaDTO = {
        entrenador1: currentPokemonData1,
        entrenador2: currentPokemonData2,
        turno: currentTurn
      };

      console.log("🤖 CPU: Enviando request:", {
        difficulty: cpuDifficulty,
        cpuIsTeam1: false,
        pokemonEquipo1: currentPokemonData1.map(p => ({ nombre: p.nombre, vida: p.vida })),
        pokemonEquipo2: currentPokemonData2.map(p => ({ nombre: p.nombre, vida: p.vida }))
      });

      // Obtener decisión de la CPU
      const cpuResponse = await cpuService.getCpuAction(batallaDTO, cpuDifficulty, false);
      
      if (!cpuResponse.data.success) {
        console.error("❌ CPU: Error en decisión:", cpuResponse.data.reasoning);
        setBattleLog(prev => [...prev, `❌ Error CPU: ${cpuResponse.data.reasoning}`]);
        return;
      }

      const cpuAction = cpuResponse.data;
      console.log("✅ CPU: Acción recibida:", cpuAction);

      // Aplicar la decisión de la CPU directamente
      setSelectedAttackerE2(cpuAction.attackerIndex);
      setSelectedTargetE2(cpuAction.targetIndex);
      setUseEffectE2(cpuAction.useEffect);
      
      if (!cpuAction.useEffect) {
        setSelectedAttackE2(cpuAction.attackIndex);
      }

      console.log("🤖 CPU: Selecciones configuradas");
      
      // Añadir mensaje al log sobre la acción de la CPU
      setBattleLog(prev => [...prev, `🤖 CPU ${cpuAction.reasoning}`]);

      // Ejecutar inmediatamente después de configurar
      setTimeout(async () => {
        try {
          console.log("🎯 CPU: Ejecutando combate inmediatamente");
          console.log("🎯 CPU: Estados antes de ejecutar:", {
            winner,
            cpuTurnProcessing,
            battleInProgress,
            canExecuteAction: canExecuteAction(),
            selectedAttackerE2,
            selectedTargetE2,
            useEffectE2,
            selectedAttackE2
          });
          
          // Verificación: debe estar procesando turno CPU y no haber ganador
          if (!winner && cpuTurnProcessing) {
            console.log("✅ CPU: Condiciones válidas, ejecutando combate...");
            await executeCombat('CPU');
          } else {
            console.log("🤖 CPU: Combate cancelado - estado inválido", {
              battleInProgress,
              winner,
              cpuTurnProcessing
            });
          }
        } catch (error) {
          console.error("❌ CPU: Error ejecutando combate:", error);
        } finally {
          // Limpiar estados de CPU DESPUÉS de ejecutar el combate
          console.log("🤖 CPU: Finalizando turno en setTimeout finally");
          setCpuTurnProcessing(false);
          setBattleInProgress(false);
        }
      }, 1000);

    } catch (error) {
      console.error("❌ CPU: Error general:", error);
      setBattleLog(prev => [...prev, `❌ Error en turno de CPU: ${error.message}`]);
      // Solo limpiar en caso de error antes del setTimeout
      setCpuTurnProcessing(false);
      setBattleInProgress(false);
    }
    // Remover el finally aquí para que no se ejecute inmediatamente
  };

  const handlePokemonAction = (trainerTeam, pokemonIndex, useEffect) => {
    playSoundEffect('click');
    
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
    playSoundEffect('click');
    
    if (trainerTeam === 1) {
      setSelectedAttackE1(attackIndex);
    } else {
      setSelectedAttackE2(attackIndex);
    }
  };

  const handleTargetSelection = (targetTeam, targetIndex) => {
    playSoundEffect('click');
    
    if (isTeam1Turn) {
      setSelectedTargetE1(targetIndex);
    } else {
      setSelectedTargetE2(targetIndex);
    }
  };

  const canExecuteAction = () => {
    // Validaciones generales de estado
    // Durante un turno de CPU, permitir la ejecución si cpuTurnProcessing es true
    if (winner) {
      return false;
    }
    
    // Si es turno de CPU (cpuTurnProcessing = true), permitir la ejecución
    // Si no es turno de CPU, no permitir si battle está en progreso
    if (!cpuTurnProcessing && battleInProgress) {
      return false;
    }
    
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

  const executeCombat = async (source = 'unknown') => {
    const executionId = Math.random().toString(36).substr(2, 9);
    console.log(`🎯 executeCombat [${executionId}] desde ${source}: Verificando condiciones de ejecución`);
    
    // VALIDACIÓN CRÍTICA: Evitar múltiples ejecuciones concurrentes
    // Excepto si es la CPU ejecutando su propio turno
    if (battleInProgress && source !== 'CPU') {
      console.log(`❌ executeCombat [${executionId}]: ABORTADO - batalla ya en progreso (no CPU)`);
      return;
    }

    // VALIDACIÓN CRÍTICA: Verificar que no hay ganador
    if (winner) {
      console.log(`❌ executeCombat [${executionId}]: ABORTADO - ya hay un ganador`);
      return;
    }

    // VALIDACIÓN CRÍTICA: Verificar que se puede ejecutar la acción
    if (!canExecuteAction()) {
      console.log(`❌ executeCombat [${executionId}]: ABORTADO - no se puede ejecutar la acción`);
      return;
    }

    // ESTABLECER INMEDIATAMENTE EL ESTADO DE BATALLA EN PROGRESO (solo si no es CPU)
    if (source !== 'CPU') {
      setBattleInProgress(true);
    }
    
    console.log(`🎯 executeCombat [${executionId}]: canExecuteAction():`, canExecuteAction());
    console.log(`🎯 executeCombat [${executionId}]: Estados actuales:`, {
      battleInProgress: true, // Ya establecido arriba
      winner,
      isTeam1Turn,
      selectedAttackerE1,
      selectedAttackerE2,
      selectedTargetE1,
      selectedTargetE2,
      selectedAttackE1,
      selectedAttackE2,
      useEffectE1,
      useEffectE2,
      livesTrainer1: livesTrainer1?.[selectedAttackerE1],
      livesTrainer2: livesTrainer2?.[selectedAttackerE2]
    });

    console.log(`✅ executeCombat [${executionId}]: Iniciando combate`);

    try {
      // Play appropriate sound effect
      const useEffect = isTeam1Turn ? useEffectE1 : useEffectE2;
      playSoundEffect(useEffect ? 'effect' : 'attack');

      // Update Pokemon lives in the data
      // Prepare batalla data maintaining all pokemon properties (including modified stats)
      const updatedEntrenador1 = pokemonDataTrainer1 && pokemonDataTrainer1 && pokemonDataTrainer1.map((pokemon, index) => ({
        ...pokemon,
        vida: livesTrainer1[index],
        // Ensure modified stats are preserved if they exist
        ataqueModificado: pokemon.ataqueModificado || pokemon.ataque,
        defensaModificada: pokemon.defensaModificada || pokemon.defensa,
      }));

      const updatedEntrenador2 = pokemonDataTrainer2 && pokemonDataTrainer2 && pokemonDataTrainer2.map((pokemon, index) => ({
        ...pokemon,
        vida: livesTrainer2[index],
        // Ensure modified stats are preserved if they exist
        ataqueModificado: pokemon.ataqueModificado || pokemon.ataque,
        defensaModificada: pokemon.defensaModificada || pokemon.defensa,
      }));

      // Determinar atacante y receptor basado en el turno actual
      const posicionAtacante = isTeam1Turn ? selectedAttackerE1 : selectedAttackerE2;
      const posicionReceptor = isTeam1Turn ? selectedTargetE1 : selectedTargetE2;
      
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
        // Incluir efectos de equipo actuales
        efectoContinuoEquipo1: teamEffects.team1.effectId,
        efectoContinuoEquipo2: teamEffects.team2.effectId,
        turnosRestantesEquipo1: teamEffects.team1.turnsRemaining,
        turnosRestantesEquipo2: teamEffects.team2.turnsRemaining,
      };

      const response = await batallaService.combatir(
        posicionAtacante || 0,
        posicionReceptor || 0,
        batallaDTO
      );

      // Update the pokemon data with response from backend (includes modified stats)
      setPokemonDataTrainer1(response.data.entrenador1);
      setPokemonDataTrainer2(response.data.entrenador2);

      // Actualizar efectos de equipo desde la respuesta del backend
      setTeamEffects({
        team1: {
          effectId: response.data.efectoContinuoEquipo1 || null,
          turnsRemaining: response.data.turnosRestantesEquipo1 || 0
        },
        team2: {
          effectId: response.data.efectoContinuoEquipo2 || null,
          turnsRemaining: response.data.turnosRestantesEquipo2 || 0
        }
      });

      // Update lives
      const previousLives1 = [...livesTrainer1];
      const previousLives2 = [...livesTrainer2];
      
      const newLivesTrainer1 = response.data.entrenador1.map((pokemon) => pokemon.vida);
      const newLivesTrainer2 = response.data.entrenador2.map((pokemon) => pokemon.vida);
      
      setLivesTrainer1(newLivesTrainer1);
      setLivesTrainer2(newLivesTrainer2);

      // Check for newly fainted Pokemon and play sound
      const pokemonFainted = newLivesTrainer1.some((vida, index) => vida === 0 && previousLives1[index] > 0) ||
                            newLivesTrainer2.some((vida, index) => vida === 0 && previousLives2[index] > 0);
      
      if (pokemonFainted) {
        setTimeout(() => playSoundEffect('faint'), 500); // Delay for dramatic effect
      }

      // Verificar si hay daño por veneno (comparar vidas antes y después)
      const damageByPoison1 = newLivesTrainer1.some((vida, index) => vida < previousLives1[index] && vida >= 0);
      const damageByPoison2 = newLivesTrainer2.some((vida, index) => vida < previousLives2[index] && vida >= 0);
      
      // Detectar si el daño fue causado por veneno al inicio del turno
      if ((damageByPoison1 && teamEffects.team1.effectId) || (damageByPoison2 && teamEffects.team2.effectId)) {
        const damagedTeam = damageByPoison1 ? teamName1 : teamName2;
        setBattleLog(prev => [...prev, `💀 El equipo de ${damagedTeam} sufre daño por envenenamiento`]);
        
        // Mostrar Pokémon específicos que sufrieron daño
        if (damageByPoison1) {
          newLivesTrainer1.forEach((vida, index) => {
            if (vida < previousLives1[index]) {
              const pokemon = pokemonDataTrainer1[index];
              const damage = previousLives1[index] - vida;
              setBattleLog(prev => [...prev, `🩸 ${pokemon.nombre} pierde ${damage} HP por veneno (${vida}/${vidaMaxE1[index]} HP)`]);
            }
          });
        }
        if (damageByPoison2) {
          newLivesTrainer2.forEach((vida, index) => {
            if (vida < previousLives2[index]) {
              const pokemon = pokemonDataTrainer2[index];
              const damage = previousLives2[index] - vida;
              setBattleLog(prev => [...prev, `🩸 ${pokemon.nombre} pierde ${damage} HP por veneno (${vida}/${vidaMaxE2[index]} HP)`]);
            }
          });
        }
      }

      // Add to battle log
      const currentTrainer = isTeam1Turn ? teamName1 : teamName2;
      const action = isTeam1Turn ? 
        (useEffectE1 ? `usó ${effectsTrainer1[selectedAttackerE1]?.nombre}` : `atacó con ${ataque?.nombre || 'Ataque'}`) :
        (useEffectE2 ? `usó ${effectsTrainer2[selectedAttackerE2]?.nombre}` : `atacó con ${ataque?.nombre || 'Ataque'}`);
      
      setBattleLog(prev => [...prev, `Turno ${turn}: ${currentTrainer} ${action}`]);

      // Añadir mensajes de efectos de equipo al log
      if (response.data.efectoContinuoEquipo1 && response.data.turnosRestantesEquipo1 > 0 && !teamEffects.team1.effectId) {
        setBattleLog(prev => [...prev, `¡El equipo de ${teamName1} ha sido envenenado! (${response.data.turnosRestantesEquipo1} turnos)`]);
      }
      if (response.data.efectoContinuoEquipo2 && response.data.turnosRestantesEquipo2 > 0 && !teamEffects.team2.effectId) {
        setBattleLog(prev => [...prev, `¡El equipo de ${teamName2} ha sido envenenado! (${response.data.turnosRestantesEquipo2} turnos)`]);
      }

      // Mostrar cuando los efectos terminan
      if (teamEffects.team1.effectId && !response.data.efectoContinuoEquipo1) {
        setBattleLog(prev => [...prev, `El envenenamiento del equipo de ${teamName1} ha terminado.`]);
      }
      if (teamEffects.team2.effectId && !response.data.efectoContinuoEquipo2) {
        setBattleLog(prev => [...prev, `El envenenamiento del equipo de ${teamName2} ha terminado.`]);
      }

      // Añadir mensajes de efectos de reducción de estadísticas
      if (response.data.ataqueReducidoEquipo1) {
        setBattleLog(prev => [...prev, `⬇️ ¡El ataque de todo el equipo de ${teamName1} ha sido reducido!`]);
      }
      if (response.data.ataqueReducidoEquipo2) {
        setBattleLog(prev => [...prev, `⬇️ ¡El ataque de todo el equipo de ${teamName2} ha sido reducido!`]);
      }
      if (response.data.defensaReducidaEquipo1) {
        setBattleLog(prev => [...prev, `🛡️⬇️ ¡La defensa de todo el equipo de ${teamName1} ha sido reducida!`]);
      }
      if (response.data.defensaReducidaEquipo2) {
        setBattleLog(prev => [...prev, `🛡️⬇️ ¡La defensa de todo el equipo de ${teamName2} ha sido reducida!`]);
      }

      // Check for winner
      const isTrainer1Lost = newLivesTrainer1.every((vida) => vida <= 0);
      const isTrainer2Lost = newLivesTrainer2.every((vida) => vida <= 0);

      if (isTrainer1Lost || isTrainer2Lost) {
        const winnerTrainer = isTrainer1Lost ? teamName2 : teamName1;
        const defeatedTrainer = isTrainer1Lost ? teamName1 : teamName2;
        
        // Determinar si la victoria fue por veneno
        const victoryByPoison = (isTrainer1Lost && damageByPoison1) || (isTrainer2Lost && damageByPoison2);
        
        setWinner(winnerTrainer);
        
        if (victoryByPoison) {
          setBattleLog(prev => [...prev, `☠️ ¡${defeatedTrainer} ha sido derrotado por envenenamiento!`]);
          setBattleLog(prev => [...prev, `🏆 ¡${winnerTrainer} ha ganado la batalla por daño continuo!`]);
        } else {
          setBattleLog(prev => [...prev, `¡${winnerTrainer} ha ganado la batalla!`]);
        }
        
        // La música de victoria se maneja en el useEffect cuando se establece el winner
      } else {
        const nextTurn = turn + 1;
        setTurn(nextTurn);
        
        // Si es batalla CPU y el próximo turno es de la CPU (equipo 2), ejecutar turno automáticamente
        // CPU juega en turnos pares (2, 4, 6...) cuando nextTurn % 2 === 0
        if (isCpuBattle && nextTurn % 2 === 0 && !cpuTurnProcessing && !battleInProgress) {
          console.log("🤖 Programando turno de CPU para turno", nextTurn);
          console.log("🤖 Estado actual del turno:", turn, "-> próximo turno:", nextTurn);
          // Delay para que se procese el cambio de turno
          setTimeout(() => {
            // Doble verificación antes de ejecutar
            console.log("🤖 Ejecutando después del delay. Turn actual:", turn);
            if (!cpuTurnProcessing && !battleInProgress && !winner) {
              // Pasar los datos actualizados directamente desde la respuesta del backend
              handleCpuTurnAction(nextTurn, response.data.entrenador1, response.data.entrenador2);
            } else {
              console.log("🤖 CPU: Turno cancelado - estado cambió durante delay");
            }
          }, 200);
        }
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
      // Solo limpiar cpuTurnProcessing si no es desde la CPU o si no hay turno de CPU próximo
      // La CPU limpiará su propio estado después de ejecutar
      if (source === 'CPU') {
        // La CPU limpia su propio estado
        setCpuTurnProcessing(false);
      }
      // Si es turno del jugador, no limpiar cpuTurnProcessing aquí porque podría haber turno CPU próximo
    }
  };

  const resetBattle = () => {
    // Reanudar música principal al salir de batalla
    if (!mainTheme.isPlaying) {
      setTimeout(() => {
        mainTheme.play();
      }, 500); // Pequeño delay para evitar conflictos
    }
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
                  <h4>{teamName1}</h4>
                  <div className="pokemon-final-lives">
                    {pokemonDataTrainer1 && pokemonDataTrainer1 && pokemonDataTrainer1.map((pokemon, index) => (
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
                  <h4>{teamName2}</h4>
                  <div className="pokemon-final-lives">
                    {pokemonDataTrainer2 && pokemonDataTrainer2 && pokemonDataTrainer2.map((pokemon, index) => (
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
      {/* Audio Manager - SIEMPRE presente para evitar desmontaje */}
      <AudioManager onAudioReady={handleAudioReady} />
      
      {winner ? (
        /* Victory Screen */
        <div className="victory-screen">
          <div className="victory-content">
            <h1 className="victory-title">🏆 ¡VICTORIA! 🏆</h1>
            <h2 className="winner-name">{winner}</h2>
            <p className="victory-message">¡Ha ganado la batalla épica!</p>
            
            <div className="battle-summary">
              <h3>Resumen de la Batalla</h3>
              <div className="final-stats">
                <div className="trainer-final-stats">
                  <h4>{teamName1}</h4>
                  <div className="pokemon-final-lives">
                    {pokemonDataTrainer1 && pokemonDataTrainer1 && pokemonDataTrainer1.map((pokemon, index) => (
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
                  <h4>{teamName2}</h4>
                  <div className="pokemon-final-lives">
                    {pokemonDataTrainer2 && pokemonDataTrainer2 && pokemonDataTrainer2.map((pokemon, index) => (
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
      ) : (
        /* Battle Interface */
        <>
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
                {isTeam1Turn ? teamName1 : teamName2}
              </span>
            </div>
          </div>
        </div>
      </div>

      {/* Battle Arena */}
      <div className="battle-arena">
        {/* Trainer 1 Side */}
        <div className={`trainer-battlefield ${isTeam1Turn ? 'active-turn' : 'waiting-turn'} ${teamEffects.team1.effectId ? 'poisoned' : ''}`}>
          <div className="trainer-info">
            <h2 className="trainer-name">
              <span className="trainer-icon">👨‍💼</span>
              {teamName1}
            </h2>
            {isTeam1Turn && (
              <div className="turn-indicator-badge">
                <span className="badge-icon">⚡</span>
                Tu turno
              </div>
            )}
            {/* Indicador de efecto continuo para equipo 1 */}
            {teamEffects.team1.effectId && teamEffects.team1.turnsRemaining > 0 && (
              <div className="team-effect-indicator poison">
                <span className="effect-icon">☠️</span>
                <span className="effect-text">Envenenado</span>
                <span className="effect-turns">{teamEffects.team1.turnsRemaining} turnos</span>
              </div>
            )}
          </div>
          
          <div className="pokemon-team">
            {pokemonDataTrainer1 && pokemonDataTrainer1 && pokemonDataTrainer1 && pokemonDataTrainer1.map((pokemon, index) => {
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
        <div className={`trainer-battlefield ${!isTeam1Turn ? 'active-turn' : 'waiting-turn'} ${teamEffects.team2.effectId ? 'poisoned' : ''}`}>
          <div className="trainer-info">
            <h2 className="trainer-name">
              <span className="trainer-icon">👩‍💼</span>
              {teamName2}
            </h2>
            {!isTeam1Turn && (
              <div className="turn-indicator-badge">
                <span className="badge-icon">⚡</span>
                Tu turno
              </div>
            )}
            {/* Indicador de efecto continuo para equipo 2 */}
            {teamEffects.team2.effectId && teamEffects.team2.turnsRemaining > 0 && (
              <div className="team-effect-indicator poison">
                <span className="effect-icon">☠️</span>
                <span className="effect-text">Envenenado</span>
                <span className="effect-turns">{teamEffects.team2.turnsRemaining} turnos</span>
              </div>
            )}
          </div>
          
          <div className="pokemon-team">
            {pokemonDataTrainer2 && pokemonDataTrainer2 && pokemonDataTrainer2 && pokemonDataTrainer2.map((pokemon, index) => {
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
          onClick={(e) => {
            e.preventDefault();
            e.stopPropagation();
            if (!battleInProgress && canExecuteAction()) {
              executeCombat('BUTTON');
            }
          }}
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
        </>
      )}
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
