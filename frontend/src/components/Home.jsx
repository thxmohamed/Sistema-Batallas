import React from 'react';
import { Link } from 'react-router-dom';

const Home = () => {
  return (
    <div className="fade-in">
      <h1>🌟 Bienvenido a PokéBattle Arena</h1>
      
      <div className="card">
        <h2>⚡ El Sistema de Batallas Pokémon Definitivo</h2>
        <p style={{ 
          fontSize: '18px', 
          color: '#718096', 
          textAlign: 'center', 
          marginBottom: '30px',
          lineHeight: '1.6'
        }}>
          Crea tus Pokémon únicos, entrena equipos poderosos y participa en épicas batallas estratégicas. 
          ¡Conviértete en el maestro Pokémon definitivo!
        </p>
      </div>

      <div className="grid grid-2">
        <div className="card">
          <h3>🐾 Crear Pokémon</h3>
          <p style={{ color: '#718096', marginBottom: '20px' }}>
            Diseña Pokémon únicos con estadísticas personalizadas, tipos elementales y habilidades especiales.
          </p>
          <Link to="/pokemon/crear" className="btn btn-success" style={{ textDecoration: 'none' }}>
            ✨ Crear Pokémon
          </Link>
        </div>

        <div className="card">
          <h3>👤 Formar Equipo</h3>
          <p style={{ color: '#718096', marginBottom: '20px' }}>
            Selecciona 3 Pokémon para formar tu equipo de entrenador y prepárate para la batalla.
          </p>
          <Link to="/entrenador/crear" className="btn btn-secondary" style={{ textDecoration: 'none' }}>
            🏆 Crear Entrenador
          </Link>
        </div>

        <div className="card">
          <h3>⚔️ Batallar</h3>
          <p style={{ color: '#718096', marginBottom: '20px' }}>
            Enfréntate a otros entrenadores en batallas estratégicas por turnos llenas de acción.
          </p>
          <Link to="/setup" className="btn" style={{ textDecoration: 'none' }}>
            🔥 Iniciar Batalla
          </Link>
        </div>

        <div className="card">
          <h3>📚 Información</h3>
          <p style={{ color: '#718096', marginBottom: '20px' }}>
            Aprende sobre los tipos de Pokémon, efectividades y mecánicas del sistema de batalla.
          </p>
          <Link to="/informacion" className="btn btn-danger" style={{ textDecoration: 'none' }}>
            📖 Ver Guía
          </Link>
        </div>
      </div>

      <div className="card" style={{ marginTop: '40px', textAlign: 'center' }}>
        <h3>🎮 Características del Juego</h3>
        <div className="grid grid-3" style={{ marginTop: '20px' }}>
          <div style={{ padding: '20px' }}>
            <div style={{ fontSize: '48px', marginBottom: '10px' }}>🔄</div>
            <h4 style={{ color: '#4a5568', marginBottom: '10px' }}>Sistema de Turnos</h4>
            <p style={{ color: '#718096', fontSize: '14px' }}>
              Batallas estratégicas donde el orden de ataque alterna entre equipos
            </p>
          </div>
          <div style={{ padding: '20px' }}>
            <div style={{ fontSize: '48px', marginBottom: '10px' }}>⚗️</div>
            <h4 style={{ color: '#4a5568', marginBottom: '10px' }}>Efectos Especiales</h4>
            <p style={{ color: '#718096', fontSize: '14px' }}>
              Usa ataques directos o efectos especiales para ganar ventaja
            </p>
          </div>
          <div style={{ padding: '20px' }}>
            <div style={{ fontSize: '48px', marginBottom: '10px' }}>🎯</div>
            <h4 style={{ color: '#4a5568', marginBottom: '10px' }}>Tipos Elementales</h4>
            <p style={{ color: '#718096', fontSize: '14px' }}>
              5 tipos únicos con ventajas y desventajas entre ellos
            </p>
          </div>
        </div>
      </div>

      <div className="card" style={{ 
        background: 'linear-gradient(135deg, #667eea, #764ba2)', 
        color: 'white',
        textAlign: 'center'
      }}>
        <h3>🏆 ¿Listo para convertirte en un Maestro Pokémon?</h3>
        <p style={{ marginBottom: '20px', opacity: '0.9' }}>
          Comienza tu aventura creando tu primer Pokémon o formando tu equipo de entrenador
        </p>
        <div style={{ display: 'flex', gap: '15px', justifyContent: 'center', flexWrap: 'wrap' }}>
          <Link to="/pokemon/crear" className="btn" style={{ 
            textDecoration: 'none',
            background: 'rgba(255, 255, 255, 0.2)',
            backdropFilter: 'blur(10px)'
          }}>
            🚀 Empezar Ahora
          </Link>
          <Link to="/informacion" className="btn" style={{ 
            textDecoration: 'none',
            background: 'rgba(255, 255, 255, 0.2)',
            backdropFilter: 'blur(10px)'
          }}>
            📋 Ver Reglas
          </Link>
        </div>
      </div>
    </div>
  );
};

export default Home;
