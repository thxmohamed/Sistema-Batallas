import React from 'react';
import '../App.css';

const Information = () => {
  return (
    <div className="page-container">
      <div className="header-section">
        <h1 className="page-title">
          <span className="title-icon">📚</span>
          Guía del Entrenador
        </h1>
        <p className="page-description">
          Todo lo que necesitas saber para convertirte en un maestro Pokémon
        </p>
      </div>

      <div className="info-content">
        {/* Quick Start Guide */}
        <div className="info-section">
          <div className="section-header">
            <h2 className="section-title">
              <span className="title-icon">🚀</span>
              Guía Rápida
            </h2>
          </div>
          
          <div className="steps-grid">
            <div className="step-card">
              <div className="step-number">1</div>
              <div className="step-content">
                <h3 className="step-title">Crear Pokémon</h3>
                <p className="step-description">
                  Primero crea tus Pokémon personalizados con estadísticas únicas
                </p>
                <div className="step-icon">⚡</div>
              </div>
            </div>
            
            <div className="step-card">
              <div className="step-number">2</div>
              <div className="step-content">
                <h3 className="step-title">Formar Equipo</h3>
                <p className="step-description">
                  Selecciona 3 Pokémon y crea tu equipo de entrenador ideal
                </p>
                <div className="step-icon">👨‍💼</div>
              </div>
            </div>
            
            <div className="step-card">
              <div className="step-number">3</div>
              <div className="step-content">
                <h3 className="step-title">¡A Batallar!</h3>
                <p className="step-description">
                  Selecciona dos entrenadores y observa la batalla épica
                </p>
                <div className="step-icon">⚔️</div>
              </div>
            </div>
          </div>
        </div>

        {/* Team Building Guide */}
        <div className="info-section">
          <div className="section-header">
            <h2 className="section-title">
              <span className="title-icon">🎯</span>
              Cómo Crear un Equipo Exitoso
            </h2>
          </div>
          
          <div className="guide-content">
            <div className="guide-item">
              <div className="guide-icon">⚖️</div>
              <div className="guide-text">
                <h3>Balance es Clave</h3>
                <p>
                  Crea un equipo equilibrado con diferentes tipos de Pokémon. 
                  Cada tipo tiene fortalezas y debilidades específicas.
                </p>
              </div>
            </div>
            
            <div className="guide-item">
              <div className="guide-icon">📊</div>
              <div className="guide-text">
                <h3>Estadísticas Importantes</h3>
                <ul className="guide-list">
                  <li><strong>Vida (❤️):</strong> Determina cuánto daño puede recibir</li>
                  <li><strong>Ataque (⚔️):</strong> La fuerza de los ataques</li>
                  <li><strong>Defensa (🛡️):</strong> Reduce el daño recibido</li>
                </ul>
              </div>
            </div>
            
            <div className="guide-item">
              <div className="guide-icon">🎲</div>
              <div className="guide-text">
                <h3>Estrategia de Equipo</h3>
                <p>
                  Los equipos deben tener exactamente 3 Pokémon. Considera usar 
                  diferentes tipos para cubrir las debilidades de cada uno.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Battle System */}
        <div className="info-section">
          <div className="section-header">
            <h2 className="section-title">
              <span className="title-icon">⚔️</span>
              Sistema de Combate
            </h2>
          </div>
          
          <div className="battle-info">
            <div className="battle-feature">
              <div className="feature-icon">🔄</div>
              <div className="feature-content">
                <h3>Turnos Alternos</h3>
                <p>
                  Los equipos atacan por turnos alternos. En turnos impares ataca el equipo 1, 
                  en turnos pares ataca el equipo 2.
                </p>
              </div>
            </div>
            
            <div className="battle-feature">
              <div className="feature-icon">⚡</div>
              <div className="feature-content">
                <h3>Ataques y Efectos</h3>
                <p>
                  En cada turno puedes elegir entre usar un ataque directo o aplicar 
                  un efecto especial a tus Pokémon.
                </p>
              </div>
            </div>
          </div>
        </div>

        {/* Type Effectiveness */}
        <div className="info-section">
          <div className="section-header">
            <h2 className="section-title">
              <span className="title-icon">🔥</span>
              Tabla de Efectividades
            </h2>
          </div>
          
          <div className="effectiveness-intro">
            <p>
              Las efectividades determinan qué tan poderosos son los ataques entre diferentes tipos. 
              Cada tipo es fuerte contra uno y débil contra otro.
            </p>
          </div>
          
          <div className="effectiveness-grid">
            <div className="type-effectiveness">
              <div className="type-header agua">
                <span className="type-icon">💧</span>
                <span className="type-name">Agua</span>
              </div>
              <div className="effectiveness-info">
                <div className="effective-against">
                  <span className="effect-label">Fuerte contra:</span>
                  <span className="effect-type fuego">🔥 Fuego</span>
                </div>
                <div className="weak-against">
                  <span className="effect-label">Débil contra:</span>
                  <span className="effect-type electrico">⚡ Eléctrico</span>
                </div>
              </div>
            </div>
            
            <div className="type-effectiveness">
              <div className="type-header fuego">
                <span className="type-icon">🔥</span>
                <span className="type-name">Fuego</span>
              </div>
              <div className="effectiveness-info">
                <div className="effective-against">
                  <span className="effect-label">Fuerte contra:</span>
                  <span className="effect-type planta">🌿 Planta</span>
                </div>
                <div className="weak-against">
                  <span className="effect-label">Débil contra:</span>
                  <span className="effect-type agua">💧 Agua</span>
                </div>
              </div>
            </div>
            
            <div className="type-effectiveness">
              <div className="type-header planta">
                <span className="type-icon">🌿</span>
                <span className="type-name">Planta</span>
              </div>
              <div className="effectiveness-info">
                <div className="effective-against">
                  <span className="effect-label">Fuerte contra:</span>
                  <span className="effect-type tierra">🌍 Tierra</span>
                </div>
                <div className="weak-against">
                  <span className="effect-label">Débil contra:</span>
                  <span className="effect-type fuego">🔥 Fuego</span>
                </div>
              </div>
            </div>
            
            <div className="type-effectiveness">
              <div className="type-header tierra">
                <span className="type-icon">🌍</span>
                <span className="type-name">Tierra</span>
              </div>
              <div className="effectiveness-info">
                <div className="effective-against">
                  <span className="effect-label">Fuerte contra:</span>
                  <span className="effect-type electrico">⚡ Eléctrico</span>
                </div>
                <div className="weak-against">
                  <span className="effect-label">Débil contra:</span>
                  <span className="effect-type planta">🌿 Planta</span>
                </div>
              </div>
            </div>
            
            <div className="type-effectiveness">
              <div className="type-header electrico">
                <span className="type-icon">⚡</span>
                <span className="type-name">Eléctrico</span>
              </div>
              <div className="effectiveness-info">
                <div className="effective-against">
                  <span className="effect-label">Fuerte contra:</span>
                  <span className="effect-type agua">💧 Agua</span>
                </div>
                <div className="weak-against">
                  <span className="effect-label">Débil contra:</span>
                  <span className="effect-type tierra">🌍 Tierra</span>
                </div>
              </div>
            </div>
          </div>
          
          <div className="effectiveness-note">
            <div className="note-icon">💡</div>
            <p>
              <strong>Tip:</strong> Los ataques de tipo Normal son neutrales contra todos los tipos, 
              lo que los convierte en opciones versátiles pero menos especializadas.
            </p>
          </div>
        </div>

        {/* Navigation */}
        <div className="action-section">
          <button 
            className="btn btn-primary btn-lg" 
            onClick={() => window.history.back()}
          >
            <span className="btn-icon">🏠</span>
            Volver al Inicio
          </button>
        </div>
      </div>
    </div>
  );
};

export default Information;
