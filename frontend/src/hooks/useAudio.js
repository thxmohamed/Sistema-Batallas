import { useRef, useEffect, useState, useCallback } from 'react';

export const useAudio = (audioPath, options = {}) => {
  const audioRef = useRef(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isLoaded, setIsLoaded] = useState(false);
  const [volume, setVolume] = useState(options.volume || 0.7);
  const [loop, setLoop] = useState(options.loop || false);
  const mountedRef = useRef(true);

  // Función para inicializar el audio
  const initializeAudio = useCallback(() => {
    if (!audioPath || !mountedRef.current) return;

    console.log('🎵 Inicializando audio:', audioPath);
    
    // Si ya existe un audio, limpiarlo primero
    if (audioRef.current) {
      audioRef.current.pause();
      audioRef.current = null;
    }

    const audio = new Audio(audioPath);
    audio.volume = volume;
    audio.loop = loop;
    audio.preload = 'auto';
    
    // Event listeners
    const handleCanPlayThrough = () => {
      if (mountedRef.current) {
        console.log('🎵 Audio cargado y listo:', audioPath);
        setIsLoaded(true);
      }
    };
    const handleEnded = () => {
      if (mountedRef.current) setIsPlaying(false);
    };
    const handlePlay = () => {
      if (mountedRef.current) setIsPlaying(true);
    };
    const handlePause = () => {
      if (mountedRef.current) setIsPlaying(false);
    };
    const handleError = (e) => {
      console.error('❌ Error cargando audio:', audioPath, e);
    };
    
    audio.addEventListener('canplaythrough', handleCanPlayThrough);
    audio.addEventListener('ended', handleEnded);
    audio.addEventListener('play', handlePlay);
    audio.addEventListener('pause', handlePause);
    audio.addEventListener('error', handleError);
    
    // Asignar la referencia solo después de configurar todo
    audioRef.current = audio;
    
    // Preload the audio
    audio.load();
    
    return () => {
      if (audio) {
        audio.removeEventListener('canplaythrough', handleCanPlayThrough);
        audio.removeEventListener('ended', handleEnded);
        audio.removeEventListener('play', handlePlay);
        audio.removeEventListener('pause', handlePause);
        audio.removeEventListener('error', handleError);
        audio.pause();
      }
    };
  }, [audioPath, volume, loop]);

  useEffect(() => {
    mountedRef.current = true;
    const cleanup = initializeAudio();
    
    return () => {
      mountedRef.current = false;
      if (cleanup) cleanup();
      // NO seteamos audioRef.current = null aquí para evitar race conditions
    };
  }, [initializeAudio]);

  // Cleanup al desmontar el componente
  useEffect(() => {
    return () => {
      mountedRef.current = false;
      if (audioRef.current) {
        audioRef.current.pause();
        audioRef.current = null;
      }
    };
  }, []);

  // Update volume and loop when they change
  useEffect(() => {
    if (audioRef.current && mountedRef.current) {
      audioRef.current.volume = volume;
      audioRef.current.loop = loop;
    }
  }, [volume, loop]);

  const play = async () => {
    // Verificar que el componente sigue montado y el audio existe
    if (!mountedRef.current) {
      console.warn('⚠️ Componente desmontado, no se puede reproducir audio:', audioPath);
      return;
    }

    // Si no hay referencia, intentar reinicializar
    if (!audioRef.current) {
      console.warn('⚠️ audioRef es null, reintentando inicialización...', audioPath);
      initializeAudio();
      // Esperar un momento para que se inicialice
      await new Promise(resolve => setTimeout(resolve, 100));
    }

    if (audioRef.current && isLoaded && mountedRef.current) {
      try {
        // Reset to beginning and ensure it's not paused
        audioRef.current.currentTime = 0;
        
        // Ensure volume is set correctly
        audioRef.current.volume = volume;
        
        console.log('🎵 Reproduciendo audio:', audioPath, 'Volume:', volume, 'audioRef exists:', !!audioRef.current);
        
        const playPromise = audioRef.current.play();
        
        if (playPromise !== undefined) {
          await playPromise;
          console.log('✅ Audio reproducido exitosamente:', audioPath);
        }
      } catch (error) {
        console.warn('❌ Error playing audio:', audioPath, error.message);
        // Try again after a short delay if it's a user interaction error
        if (error.name === 'NotAllowedError') {
          console.log('🔄 Reintentando reproducción de audio tras interacción de usuario...');
          setTimeout(async () => {
            try {
              if (audioRef.current && mountedRef.current) {
                await audioRef.current.play();
                console.log('✅ Audio reproducido exitosamente en segundo intento:', audioPath);
              }
            } catch (retryError) {
              console.warn('❌ Error en segundo intento:', retryError.message);
            }
          }, 100);
        }
      }
    } else {
      console.warn('⚠️ No se puede reproducir audio:', audioPath, 'isLoaded:', isLoaded, 'audioRef exists:', !!audioRef.current, 'mounted:', mountedRef.current);
      
      // Intentar una reinicialización forzada si es crítico (como audio de victoria)
      if (audioPath && audioPath.includes('win-theme') && mountedRef.current) {
        console.log('🔄 Forzando reinicialización para audio de victoria...');
        initializeAudio();
      }
    }
  };

  const pause = () => {
    if (audioRef.current && mountedRef.current) {
      audioRef.current.pause();
    }
  };

  const stop = () => {
    if (audioRef.current && mountedRef.current) {
      audioRef.current.pause();
      audioRef.current.currentTime = 0;
    }
  };

  const changeVolume = (newVolume) => {
    const vol = Math.max(0, Math.min(1, newVolume));
    setVolume(vol);
    if (audioRef.current && mountedRef.current) {
      audioRef.current.volume = vol;
    }
  };

  return {
    play,
    pause,
    stop,
    isPlaying,
    isLoaded,
    volume,
    setVolume: changeVolume,
    setLoop
  };
};

export default useAudio;
