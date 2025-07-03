import React, { useState } from 'react';
import { useAudioContext } from '../contexts/AudioContext.jsx';

const AudioControls = ({ className = '' }) => {
  const {
    isMuted,
    masterVolume,
    musicVolume,
    sfxVolume,
    audioEnabled,
    setMasterVolume,
    setMusicVolume,
    setSfxVolume,
    toggleMute,
    toggleAudio
  } = useAudioContext();

  const [showControls, setShowControls] = useState(false);

  const toggleControls = () => {
    setShowControls(!showControls);
  };

  return (
    <div className={`audio-controls ${className}`}>
      {/* Main Audio Toggle */}
      <button
        onClick={toggleAudio}
        className={`audio-toggle-btn ${!audioEnabled ? 'disabled' : ''}`}
        title={audioEnabled ? 'Desactivar Audio' : 'Activar Audio'}
      >
        <span className="btn-icon">
          {audioEnabled ? (isMuted ? '🔇' : '🔊') : '🔈'}
        </span>
      </button>

      {/* Mute Toggle (only show if audio is enabled) */}
      {audioEnabled && (
        <button
          onClick={toggleMute}
          className={`mute-toggle-btn ${isMuted ? 'muted' : ''}`}
          title={isMuted ? 'Activar Sonido' : 'Silenciar'}
        >
          <span className="btn-icon">
            {isMuted ? '�' : '�'}
          </span>
        </button>
      )}

      {/* Settings Toggle */}
      {audioEnabled && (
        <button
          onClick={toggleControls}
          className={`settings-toggle-btn ${showControls ? 'active' : ''}`}
          title="Configuración de Audio"
        >
          <span className="btn-icon">⚙️</span>
        </button>
      )}

      {/* Audio Settings Panel */}
      {showControls && audioEnabled && (
        <div className="audio-settings-panel">
          <div className="audio-setting">
            <label className="setting-label">
              <span className="setting-icon">🎵</span>
              Volumen General
            </label>
            <div className="volume-control">
              <input
                type="range"
                min="0"
                max="1"
                step="0.1"
                value={masterVolume}
                onChange={(e) => setMasterVolume(parseFloat(e.target.value))}
                className="volume-slider"
              />
              <span className="volume-value">{Math.round(masterVolume * 100)}%</span>
            </div>
          </div>

          <div className="audio-setting">
            <label className="setting-label">
              <span className="setting-icon">🎼</span>
              Música
            </label>
            <div className="volume-control">
              <input
                type="range"
                min="0"
                max="1"
                step="0.1"
                value={musicVolume}
                onChange={(e) => setMusicVolume(parseFloat(e.target.value))}
                className="volume-slider"
              />
              <span className="volume-value">{Math.round(musicVolume * 100)}%</span>
            </div>
          </div>

          <div className="audio-setting">
            <label className="setting-label">
              <span className="setting-icon">💥</span>
              Efectos
            </label>
            <div className="volume-control">
              <input
                type="range"
                min="0"
                max="1"
                step="0.1"
                value={sfxVolume}
                onChange={(e) => setSfxVolume(parseFloat(e.target.value))}
                className="volume-slider"
              />
              <span className="volume-value">{Math.round(sfxVolume * 100)}%</span>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};

export default AudioControls;
