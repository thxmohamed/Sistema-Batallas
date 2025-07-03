import React, { createContext, useContext, useState, useEffect, useRef } from 'react';
import { useLocation } from 'react-router-dom';
import useAudio from '../hooks/useAudio';

const AudioContext = createContext();

export const useAudioContext = () => {
  const context = useContext(AudioContext);
  if (!context) {
    throw new Error('useAudioContext must be used within an AudioProvider');
  }
  return context;
};

export const AudioProvider = ({ children }) => {
  const location = useLocation();
  const [isMuted, setIsMuted] = useState(false);
  const [masterVolume, setMasterVolume] = useState(0.7);
  const [musicVolume, setMusicVolume] = useState(0.5);
  const [sfxVolume, setSfxVolume] = useState(0.8);
  const [audioEnabled, setAudioEnabled] = useState(true);

  // Música de fondo global
  const mainTheme = useAudio('/audio/music/main-theme.mp3', {
    volume: 0,
    loop: true
  });

  // Controlar música de fondo según la ruta
  useEffect(() => {
    const isBattlePage = location.pathname === '/battle' || location.pathname.includes('/battle');
    
    if (audioEnabled && !isMuted && !isBattlePage && mainTheme.isLoaded) {
      // Reproducir música principal en todas las páginas excepto batalla
      mainTheme.setVolume(getEffectiveVolume('music'));
      if (!mainTheme.isPlaying) {
        mainTheme.play();
      }
    } else {
      // Detener música principal en batalla o cuando audio esté deshabilitado
      if (mainTheme.isPlaying) {
        mainTheme.stop();
      }
    }
  }, [location.pathname, audioEnabled, isMuted, mainTheme.isLoaded, mainTheme.isPlaying]);

  // Actualizar volumen de música principal cuando cambien los controles
  useEffect(() => {
    if (mainTheme.isLoaded) {
      mainTheme.setVolume(getEffectiveVolume('music'));
    }
  }, [masterVolume, musicVolume, isMuted, audioEnabled]);

  // Load settings from localStorage
  useEffect(() => {
    const savedSettings = localStorage.getItem('pokemonBattleAudioSettings');
    if (savedSettings) {
      try {
        const settings = JSON.parse(savedSettings);
        setIsMuted(settings.isMuted || false);
        setMasterVolume(settings.masterVolume || 0.7);
        setMusicVolume(settings.musicVolume || 0.5);
        setSfxVolume(settings.sfxVolume || 0.8);
        setAudioEnabled(settings.audioEnabled !== false);
      } catch (error) {
        console.warn('Error loading audio settings:', error);
      }
    }
  }, []);

  // Save settings to localStorage
  useEffect(() => {
    const settings = {
      isMuted,
      masterVolume,
      musicVolume,
      sfxVolume,
      audioEnabled
    };
    localStorage.setItem('pokemonBattleAudioSettings', JSON.stringify(settings));
  }, [isMuted, masterVolume, musicVolume, sfxVolume, audioEnabled]);

  const toggleMute = () => {
    setIsMuted(!isMuted);
  };

  const toggleAudio = () => {
    const newEnabled = !audioEnabled;
    setAudioEnabled(newEnabled);
    
    // Si se desactiva el audio, detener música principal
    if (!newEnabled && mainTheme.isPlaying) {
      mainTheme.stop();
    }
  };

  const getEffectiveVolume = (type = 'sfx') => {
    if (!audioEnabled || isMuted) return 0;
    
    const baseVolume = type === 'music' ? musicVolume : sfxVolume;
    const effectiveVolume = masterVolume * baseVolume;
    
    // Evitar que el volumen sea 0 a menos que esté explícitamente silenciado
    return effectiveVolume > 0 ? Math.max(effectiveVolume, 0.01) : 0;
  };

  const contextValue = {
    isMuted,
    masterVolume,
    musicVolume,
    sfxVolume,
    audioEnabled,
    setIsMuted,
    setMasterVolume,
    setMusicVolume,
    setSfxVolume,
    setAudioEnabled,
    toggleMute,
    toggleAudio,
    getEffectiveVolume,
    // Exponer controles de música principal
    mainTheme: {
      isPlaying: mainTheme.isPlaying,
      play: mainTheme.play,
      stop: mainTheme.stop,
      pause: mainTheme.pause
    }
  };

  return (
    <AudioContext.Provider value={contextValue}>
      {children}
    </AudioContext.Provider>
  );
};
