import React, { createContext, useContext, useState, useEffect } from 'react';

const AudioContext = createContext();

export const useAudioContext = () => {
  const context = useContext(AudioContext);
  if (!context) {
    throw new Error('useAudioContext must be used within an AudioProvider');
  }
  return context;
};

export const AudioProvider = ({ children }) => {
  const [isMuted, setIsMuted] = useState(false);
  const [masterVolume, setMasterVolume] = useState(0.7);
  const [musicVolume, setMusicVolume] = useState(0.5);
  const [sfxVolume, setSfxVolume] = useState(0.8);
  const [audioEnabled, setAudioEnabled] = useState(true);

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
    setAudioEnabled(!audioEnabled);
  };

  const getEffectiveVolume = (type = 'sfx') => {
    if (!audioEnabled || isMuted) return 0;
    
    const baseVolume = type === 'music' ? musicVolume : sfxVolume;
    return masterVolume * baseVolume;
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
    getEffectiveVolume
  };

  return (
    <AudioContext.Provider value={contextValue}>
      {children}
    </AudioContext.Provider>
  );
};
