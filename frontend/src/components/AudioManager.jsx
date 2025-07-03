import React, { useEffect } from 'react';
import { useAudioContext } from '../contexts/AudioContext.jsx';
import useAudio from '../hooks/useAudio';

const AudioManager = ({ 
  battleMusic = '/audio/music/battle-theme.mp3',
  victoryMusic = '/audio/music/win-theme.mp3',
  onAudioReady 
}) => {
  const { getEffectiveVolume, audioEnabled } = useAudioContext();

  // Background music
  const battleTheme = useAudio(battleMusic, {
    volume: getEffectiveVolume('music'),
    loop: true
  });

  const victoryTheme = useAudio(victoryMusic, {
    volume: getEffectiveVolume('music'),
    loop: false
  });

  // Sound effects
  const attackHitSFX = useAudio('/audio/sfx/pokemon-hit.mp3', {
    volume: getEffectiveVolume('sfx')
  });

  const effectUseSFX = useAudio('/audio/sfx/effect-use.mp3', {
    volume: getEffectiveVolume('sfx')
  });

  const pokemonFaintSFX = useAudio('/audio/sfx/pokemon-fainted.mp3', {
    volume: getEffectiveVolume('sfx')
  });

  const buttonClickSFX = useAudio('/audio/sfx/button-click.mp3', {
    volume: getEffectiveVolume('sfx')
  });

  // Update volumes when context changes
  useEffect(() => {
    battleTheme.setVolume(getEffectiveVolume('music'));
    victoryTheme.setVolume(getEffectiveVolume('music'));
    attackHitSFX.setVolume(getEffectiveVolume('sfx'));
    effectUseSFX.setVolume(getEffectiveVolume('sfx'));
    pokemonFaintSFX.setVolume(getEffectiveVolume('sfx'));
    buttonClickSFX.setVolume(getEffectiveVolume('sfx'));
  }, [getEffectiveVolume('music'), getEffectiveVolume('sfx')]);

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
          console.log('🎵 Estado:', { 
            audioEnabled, 
            victoryLoaded: victoryTheme.isLoaded,
            victoryPlaying: victoryTheme.isPlaying,
            battlePlaying: battleTheme.isPlaying 
          });
          
          if (audioEnabled) {
            try {
              // Ensure battle music is stopped
              if (battleTheme.isPlaying) {
                console.log('🎵 Parando música de batalla antes de victoria...');
                battleTheme.stop();
              }
              
              // Stop victory theme if already playing and reset
              if (victoryTheme.isPlaying) {
                victoryTheme.stop();
              }
              
              // Small delay to ensure previous audio is stopped
              await new Promise(resolve => setTimeout(resolve, 50));
              
              // Intentar reproducir música de victoria
              console.log('🎵 Reproduciendo música de victoria ahora...');
              
              // Si no está cargado, intentar forzar la carga
              if (!victoryTheme.isLoaded) {
                console.log('🔄 Audio de victoria no cargado, forzando reinicialización...');
                // Dar tiempo para que se reinicialice
                await new Promise(resolve => setTimeout(resolve, 200));
              }
              
              await victoryTheme.play();
              console.log('✅ Música de victoria reproducida exitosamente');
              
              return true;
            } catch (error) {
              console.error('❌ Error reproduciendo música de victoria:', error);
              
              // Intentar crear y reproducir audio de emergencia como último recurso
              console.log('🚨 Intentando reproducción de emergencia...');
              try {
                const emergencyAudio = new Audio('/audio/music/win-theme.mp3');
                emergencyAudio.volume = getEffectiveVolume('music');
                await emergencyAudio.play();
                console.log('✅ Audio de emergencia reproducido exitosamente');
                return true;
              } catch (emergencyError) {
                console.error('❌ Error incluso con audio de emergencia:', emergencyError);
                return false;
              }
            }
          } else {
            console.warn('⚠️ No se puede reproducir música de victoria - audio deshabilitado');
            return false;
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
