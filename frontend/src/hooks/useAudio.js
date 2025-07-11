import { useRef, useEffect, useState, useCallback } from 'react';

export const useAudio = (audioPath, options = {}) => {
  const audioRef = useRef(null);
  const [isPlaying, setIsPlaying] = useState(false);
  const [isLoaded, setIsLoaded] = useState(false);
  const [volume, setVolume] = useState(options.volume || 0.7);
  const [loop, setLoop] = useState(options.loop || false);
  const [preload, setPreload] = useState(options.preload || 'auto');
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
    audio.volume = options.volume || 0.7; // Usar volumen inicial de opciones
    audio.loop = loop;
    audio.preload = preload; // Usar configuración de precarga
    audio.crossOrigin = 'anonymous'; // Para evitar problemas de CORS
    
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
  }, [audioPath, loop, preload]); // Agregado preload a las dependencias

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

  // Update volume and loop when they change (SIN reinicializar)
  useEffect(() => {
    if (audioRef.current && mountedRef.current) {
      audioRef.current.volume = volume;
      audioRef.current.loop = loop;
      audioRef.current.preload = preload;
      console.log('🎵 Configuración actualizada para', audioPath, '- Volume:', volume, 'Loop:', loop, 'Preload:', preload);
    }
  }, [volume, loop, preload, audioPath]);

  const play = async () => {
    // Verificar que el componente sigue montado
    if (!mountedRef.current) {
      console.warn('⚠️ Componente desmontado, no se puede reproducir audio:', audioPath);
      return Promise.reject('Component unmounted');
    }

    // Si no hay referencia o no está cargado, intentar reinicializar
    if (!audioRef.current || !isLoaded) {
      console.warn('⚠️ Audio no disponible, reintentando inicialización...', audioPath);
      initializeAudio();
      
      // Esperar a que se cargue con timeout
      const maxWaitTime = 3000; // 3 segundos máximo
      const startTime = Date.now();
      
      while ((!audioRef.current || !isLoaded) && (Date.now() - startTime) < maxWaitTime && mountedRef.current) {
        await new Promise(resolve => setTimeout(resolve, 100));
      }
      
      if (!audioRef.current || !isLoaded) {
        console.error('❌ Audio no se pudo cargar después de espera:', audioPath);
        return Promise.reject('Audio failed to load');
      }
    }

    if (audioRef.current && isLoaded && mountedRef.current) {
      try {
        // Reset to beginning
        audioRef.current.currentTime = 0;
        
        // Ensure volume is set correctly
        audioRef.current.volume = volume;
        
        console.log('🎵 Reproduciendo audio:', audioPath, 'Volume:', volume);
        
        const playPromise = audioRef.current.play();
        
        if (playPromise !== undefined) {
          await playPromise;
          console.log('✅ Audio reproducido exitosamente:', audioPath);
          return Promise.resolve();
        }
      } catch (error) {
        console.warn('❌ Error playing audio:', audioPath, error.message);
        
        // Try again for user interaction errors
        if (error.name === 'NotAllowedError') {
          console.log('🔄 Reintentando reproducción tras interacción...');
          setTimeout(async () => {
            try {
              if (audioRef.current && mountedRef.current) {
                await audioRef.current.play();
                console.log('✅ Audio reproducido en segundo intento:', audioPath);
              }
            } catch (retryError) {
              console.warn('❌ Error en segundo intento:', retryError.message);
            }
          }, 100);
        }
        
        return Promise.reject(error);
      }
    } else {
      const error = `Cannot play audio ${audioPath}: isLoaded=${isLoaded}, audioRef=${!!audioRef.current}, mounted=${mountedRef.current}`;
      console.warn('⚠️', error);
      return Promise.reject(error);
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
    
    // Actualizar inmediatamente el volumen del audio si existe
    if (audioRef.current && mountedRef.current) {
      audioRef.current.volume = vol;
      console.log('🎵 Volumen cambiado inmediatamente para', audioPath, ':', vol);
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
    setLoop,
    setPreload
  };
};

export default useAudio;
