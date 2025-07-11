import React, { useEffect, useMemo } from 'react';
import { useAudioContext } from '../contexts/AudioContext.jsx';
import useAudio from '../hooks/useAudio';

const AudioManager = ({ 
  battleMusic = '/audio/music/battle-theme.mp3',
  victoryMusic = '/audio/music/win-theme.mp3',
  onAudioReady 
}) => {
  const { getEffectiveVolume, audioEnabled } = useAudioContext();

  // Memoize volumes to prevent infinite re-renders
  const musicVolume = useMemo(() => getEffectiveVolume('music'), [getEffectiveVolume]);
  const sfxVolume = useMemo(() => getEffectiveVolume('sfx'), [getEffectiveVolume]);

  // Background music - inicializar con volumen fijo y precarga agresiva
  const battleTheme = useAudio(battleMusic, {
    volume: 0.5, // Volumen inicial fijo
    loop: true,
    preload: 'auto'
  });

  const victoryTheme = useAudio(victoryMusic, {
    volume: 0.5, // Volumen inicial fijo
    loop: false,
    preload: 'auto'
  });

  // Sound effects - inicializar con volumen fijo y precarga
  const attackHitSFX = useAudio('/audio/sfx/pokemon-hit.mp3', {
    volume: 0.8, // Volumen inicial fijo
    preload: 'auto'
  });

  const effectUseSFX = useAudio('/audio/sfx/effect-use.mp3', {
    volume: 0.8, // Volumen inicial fijo
    preload: 'auto'
  });

  const pokemonFaintSFX = useAudio('/audio/sfx/pokemon-fainted.mp3', {
    volume: 0.8, // Volumen inicial fijo
    preload: 'auto'
  });

  const buttonClickSFX = useAudio('/audio/sfx/button-click.mp3', {
    volume: 0.8, // Volumen inicial fijo
    preload: 'auto'
  });

  // Update volumes when context changes
  useEffect(() => {
    battleTheme.setVolume(musicVolume);
    victoryTheme.setVolume(musicVolume);
    attackHitSFX.setVolume(sfxVolume);
    effectUseSFX.setVolume(sfxVolume);
    pokemonFaintSFX.setVolume(sfxVolume);
    buttonClickSFX.setVolume(sfxVolume);
  }, [musicVolume, sfxVolume, battleTheme, victoryTheme, attackHitSFX, effectUseSFX, pokemonFaintSFX, buttonClickSFX]);

  // Stop all audio when disabled
  useEffect(() => {
    if (!audioEnabled) {
      battleTheme.stop();
      victoryTheme.stop();
    }
  }, [audioEnabled]);

  // Notify parent when audio is ready
  useEffect(() => {
    if (onAudioReady && battleTheme.isLoaded && victoryTheme.isLoaded) {
      console.log('🎵 Audio Manager: Todos los archivos de audio están cargados, notificando componente padre');
      onAudioReady({
        playBattleMusic: async () => {
          console.log('🎵 Intentando reproducir música de batalla. Audio habilitado:', audioEnabled);
          if (audioEnabled && battleTheme.isLoaded) {
            try {
              // Stop victory music if playing
              victoryTheme.stop();
              await battleTheme.play();
              console.log('✅ Música de batalla reproducida exitosamente');
            } catch (error) {
              console.error('❌ Error reproduciendo música de batalla:', error);
            }
          }
        },
        stopBattleMusic: () => {
          console.log('🎵 Parando música de batalla');
          return battleTheme.stop();
        },
        playVictoryMusic: async () => {
          console.log('🎵 AudioManager: Intentando reproducir música de victoria');
          console.log('🎵 Estado inicial:', { 
            audioEnabled, 
            victoryLoaded: victoryTheme.isLoaded,
            victoryPlaying: victoryTheme.isPlaying,
            battlePlaying: battleTheme.isPlaying 
          });
          
          if (!audioEnabled) {
            console.warn('⚠️ No se puede reproducir música de victoria - audio deshabilitado');
            return false;
          }

          try {
            // Stop battle music first
            if (battleTheme.isPlaying) {
              console.log('🎵 Deteniendo música de batalla...');
              battleTheme.stop();
              await new Promise(resolve => setTimeout(resolve, 100));
            }
            
            // Stop victory theme if already playing
            if (victoryTheme.isPlaying) {
              console.log('🎵 Deteniendo música de victoria previa...');
              victoryTheme.stop();
              await new Promise(resolve => setTimeout(resolve, 50));
            }
            
            // Check if victory theme is loaded, if not create fallback immediately
            if (!victoryTheme.isLoaded) {
              console.warn('⚠️ Audio de victoria no cargado, usando fallback directo');
              const fallbackAudio = new Audio('/audio/music/win-theme.mp3');
              fallbackAudio.volume = musicVolume;
              fallbackAudio.preload = 'auto';
              
              await new Promise((resolve, reject) => {
                const timeout = setTimeout(() => {
                  reject(new Error('Timeout loading fallback audio'));
                }, 3000);
                
                fallbackAudio.addEventListener('canplaythrough', () => {
                  clearTimeout(timeout);
                  resolve();
                }, { once: true });
                
                fallbackAudio.addEventListener('error', (e) => {
                  clearTimeout(timeout);
                  reject(e);
                }, { once: true });
                
                fallbackAudio.load();
              });
              
              await fallbackAudio.play();
              console.log('✅ Música de victoria fallback reproducida exitosamente');
              return true;
            }
            
            // Try to play the loaded victory theme
            console.log('🎵 Reproduciendo música de victoria cargada...');
            await victoryTheme.play();
            console.log('✅ Música de victoria reproducida exitosamente');
            return true;
            
          } catch (error) {
            console.error('❌ Error reproduciendo música de victoria:', error);
            
            // Final fallback - simple audio creation and play
            try {
              console.log('🚨 Intentando fallback de emergencia...');
              const emergencyAudio = new Audio('/audio/music/win-theme.mp3');
              emergencyAudio.volume = musicVolume;
              
              // Force load and play immediately
              emergencyAudio.load();
              await new Promise(resolve => setTimeout(resolve, 100));
              await emergencyAudio.play();
              
              console.log('✅ Audio de emergencia reproducido exitosamente');
              return true;
            } catch (emergencyError) {
              console.error('❌ Error incluso con audio de emergencia:', emergencyError);
              return false;
            }
          }
        },
        stopVictoryMusic: () => {
          console.log('🎵 Parando música de victoria');
          return victoryTheme.stop();
        },
        playAttackHit: () => audioEnabled && attackHitSFX.play(),
        playEffectUse: () => audioEnabled && effectUseSFX.play(),
        playPokemonFaint: () => audioEnabled && pokemonFaintSFX.play(),
        playButtonClick: () => audioEnabled && buttonClickSFX.play(),
        isAudioEnabled: audioEnabled
      });
    } else {
      console.log('🎵 Audio Manager: Esperando carga de archivos. Battle loaded:', battleTheme.isLoaded, 'Victory loaded:', victoryTheme.isLoaded);
    }
  }, [
    battleTheme.isLoaded,
    victoryTheme.isLoaded,
    audioEnabled,
    battleTheme.play,
    victoryTheme.play,
    attackHitSFX.play,
    effectUseSFX.play,
    pokemonFaintSFX.play,
    buttonClickSFX.play
  ]);

  return null; // This component doesn't render anything
};

export default AudioManager;
