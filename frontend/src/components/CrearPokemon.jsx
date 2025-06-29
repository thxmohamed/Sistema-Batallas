import React, { useState, useEffect } from 'react';
import pokemonService from '../Services/pokemon.service';
import '../App.css';

const CrearPokemon = () => {
  const [formData, setFormData] = useState({
    nombre: '',
    tipoPokemon: '',
    vida: '',
    ataque: '',
    defensa: '',
    idAtaque1: '',
    idAtaque2: '',
    idEfecto: '',
    estado: '',
    sprite: null,
  });

  const [isLoading, setIsLoading] = useState(false);
  const [message, setMessage] = useState('');
  const [availableAttacks, setAvailableAttacks] = useState([]);
  const [availableEffects, setAvailableEffects] = useState([]);
  const [loadingAttacks, setLoadingAttacks] = useState(true);
  const [loadingEffects, setLoadingEffects] = useState(true);

  // Load available attacks and effects when component mounts
  useEffect(() => {
    const loadAttacksAndEffects = async () => {
      try {
        const [attacksResponse, effectsResponse] = await Promise.all([
          pokemonService.getAllAtaques(),
          pokemonService.getAllEfectos()
        ]);
        setAvailableAttacks(attacksResponse.data);
        setAvailableEffects(effectsResponse.data);
      } catch (error) {
        console.error('Error al cargar ataques y efectos:', error);
        setMessage('⚠️ Error al cargar ataques y efectos disponibles');
      } finally {
        setLoadingAttacks(false);
        setLoadingEffects(false);
      }
    };

    loadAttacksAndEffects();
  }, []);

  // Helper function to get type icon and color
  const getTypeStyle = (tipo) => {
    const typeStyles = {
      AGUA: { icon: "💧", color: "#6890F0", bgColor: "rgba(104, 144, 240, 0.1)" },
      FUEGO: { icon: "🔥", color: "#F08030", bgColor: "rgba(240, 128, 48, 0.1)" },
      PLANTA: { icon: "🌿", color: "#78C850", bgColor: "rgba(120, 200, 80, 0.1)" },
      TIERRA: { icon: "🌍", color: "#E0C068", bgColor: "rgba(224, 192, 104, 0.1)" },
      ELECTRICO: { icon: "⚡", color: "#F8D030", bgColor: "rgba(248, 208, 48, 0.1)" },
      NORMAL: { icon: "⭐", color: "#A8A878", bgColor: "rgba(168, 168, 120, 0.1)" }
    };
    return typeStyles[tipo] || typeStyles.NORMAL;
  };

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleFileChange = (e) => {
    setFormData({ ...formData, sprite: e.target.files[0] });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setIsLoading(true);
    setMessage('');

    const data = new FormData();
    Object.entries(formData).forEach(([key, value]) => {
      const isNumber = !isNaN(value) && key !== "sprite";
      data.append(key, isNumber ? Number(value) : value);
    });

    try {
      const response = await pokemonService.create(data);
      setMessage(`¡Pokémon creado exitosamente: ${response.data.nombre}! 🎉`);
      setFormData({
        nombre: '',
        tipoPokemon: '',
        vida: '',
        ataque: '',
        defensa: '',
        idAtaque1: '',
        idAtaque2: '',
        idEfecto: '',
        estado: '',
        sprite: null,
      });
      // Reset file input
      const fileInput = document.querySelector('input[type="file"]');
      if (fileInput) fileInput.value = '';
    } catch (error) {
      console.error('Error al crear el Pokemon:', error);
      setMessage('❌ Error al crear el Pokémon. Por favor, intenta de nuevo.');
    } finally {
      setIsLoading(false);
    }
  };

  return (
    <div className="page-container">
      <div className="header-section">
        <h1 className="page-title">
          <span className="title-icon">⚡</span>
          Crear Nuevo Pokémon
        </h1>
        <p className="page-description">
          Diseña tu Pokémon único con estadísticas y habilidades personalizadas
        </p>
      </div>

      {message && (
        <div className={`alert ${message.includes('exitosamente') ? 'alert-success' : 'alert-error'}`}>
          <span className="alert-icon">
            {message.includes('exitosamente') ? '✅' : '❌'}
          </span>
          {message}
        </div>
      )}

      <div className="form-section">
        <form onSubmit={handleSubmit} className="pokemon-form">
          <div className="form-grid">
            {/* Basic Info */}
            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">🏷️</span>
                Nombre del Pokémon
              </label>
              <input
                type="text"
                name="nombre"
                value={formData.nombre}
                onChange={handleChange}
                className="form-input"
                placeholder="Ej: Pikachu, Charizard..."
                required
              />
              <small className="form-hint">Elige un nombre único y memorable</small>
            </div>

            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">🌟</span>
                Tipo de Pokémon
              </label>
              <select
                name="tipoPokemon"
                value={formData.tipoPokemon}
                onChange={handleChange}
                className="form-select"
                required
              >
                <option value="">Seleccionar tipo...</option>
                <option value="AGUA">💧 Agua</option>
                <option value="FUEGO">🔥 Fuego</option>
                <option value="PLANTA">🌿 Planta</option>
                <option value="TIERRA">🌍 Tierra</option>
                <option value="ELECTRICO">⚡ Eléctrico</option>
              </select>
            </div>

            {/* Stats */}
            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">❤️</span>
                Puntos de Vida
              </label>
              <input
                type="number"
                name="vida"
                value={formData.vida}
                onChange={handleChange}
                className="form-input"
                placeholder="50-150"
                min="1"
                max="200"
                required
              />
              <small className="form-hint">Determina la resistencia de tu Pokémon</small>
            </div>

            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">⚔️</span>
                Poder de Ataque
              </label>
              <input
                type="number"
                name="ataque"
                value={formData.ataque}
                onChange={handleChange}
                className="form-input"
                placeholder="30-120"
                min="1"
                max="200"
                required
              />
              <small className="form-hint">La fuerza de los ataques físicos</small>
            </div>

            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">🛡️</span>
                Defensa
              </label>
              <input
                type="number"
                name="defensa"
                value={formData.defensa}
                onChange={handleChange}
                className="form-input"
                placeholder="30-120"
                min="1"
                max="200"
                required
              />
              <small className="form-hint">Resistencia a ataques enemigos</small>
            </div>

            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">🎭</span>
                Estado Evolutivo
              </label>
              <select
                name="estado"
                value={formData.estado}
                onChange={handleChange}
                className="form-select"
                required
              >
                <option value="">Seleccionar estado...</option>
                <option value="1">🥚 Primera evolución</option>
                <option value="2">🐣 Segunda evolución</option>
                <option value="3">🦅 Última evolución</option>
                <option value="4">⭐ No evoluciona</option>
              </select>
            </div>

            {/* Attacks Section */}
            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">⚡</span>
                Ataque Principal
              </label>
              {loadingAttacks ? (
                <div className="loading-selector">
                  <div className="loading-spinner"></div>
                  <span>Cargando ataques...</span>
                </div>
              ) : (
                <div className="visual-attack-selector">
                  <input
                    type="hidden"
                    name="idAtaque1"
                    value={formData.idAtaque1}
                    required
                  />
                  <div className="attack-options-grid">
                    {availableAttacks.map((attack) => {
                      const typeStyle = getTypeStyle(attack.tipoAtaque);
                      const isSelected = formData.idAtaque1 === attack.id.toString();
                      return (
                        <div
                          key={attack.id}
                          className={`attack-option ${isSelected ? 'selected' : ''}`}
                          onClick={() => setFormData({ ...formData, idAtaque1: attack.id.toString() })}
                          style={{
                            borderColor: isSelected ? typeStyle.color : '#e5e7eb',
                            backgroundColor: isSelected ? typeStyle.bgColor : 'white'
                          }}
                        >
                          <div className="attack-type-icon" style={{ color: typeStyle.color }}>
                            {typeStyle.icon}
                          </div>
                          <div className="attack-details">
                            <h4 className="attack-name" style={{ color: isSelected ? typeStyle.color : '#374151' }}>
                              {attack.nombre}
                            </h4>
                            <span className="attack-type-badge" style={{ 
                              backgroundColor: typeStyle.color,
                              color: 'white'
                            }}>
                              {attack.tipoAtaque}
                            </span>
                            <p className="attack-description">{attack.descripcion}</p>
                          </div>
                          {isSelected && (
                            <div className="selected-indicator">
                              <span>✓</span>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                  {!formData.idAtaque1 && (
                    <p className="selection-hint">Selecciona tu primer ataque haciendo clic en una opción</p>
                  )}
                </div>
              )}
            </div>

            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">⚡</span>
                Ataque Secundario
              </label>
              {loadingAttacks ? (
                <div className="loading-selector">
                  <div className="loading-spinner"></div>
                  <span>Cargando ataques...</span>
                </div>
              ) : (
                <div className="visual-attack-selector">
                  <input
                    type="hidden"
                    name="idAtaque2"
                    value={formData.idAtaque2}
                    required
                  />
                  <div className="attack-options-grid">
                    {availableAttacks
                      .filter(attack => attack.id.toString() !== formData.idAtaque1) // Exclude first attack
                      .map((attack) => {
                        const typeStyle = getTypeStyle(attack.tipoAtaque);
                        const isSelected = formData.idAtaque2 === attack.id.toString();
                        return (
                          <div
                            key={attack.id}
                            className={`attack-option ${isSelected ? 'selected' : ''}`}
                            onClick={() => setFormData({ ...formData, idAtaque2: attack.id.toString() })}
                            style={{
                              borderColor: isSelected ? typeStyle.color : '#e5e7eb',
                              backgroundColor: isSelected ? typeStyle.bgColor : 'white'
                            }}
                          >
                            <div className="attack-type-icon" style={{ color: typeStyle.color }}>
                              {typeStyle.icon}
                            </div>
                            <div className="attack-details">
                              <h4 className="attack-name" style={{ color: isSelected ? typeStyle.color : '#374151' }}>
                                {attack.nombre}
                              </h4>
                              <span className="attack-type-badge" style={{ 
                                backgroundColor: typeStyle.color,
                                color: 'white'
                              }}>
                                {attack.tipoAtaque}
                              </span>
                              <p className="attack-description">{attack.descripcion}</p>
                            </div>
                            {isSelected && (
                              <div className="selected-indicator">
                                <span>✓</span>
                              </div>
                            )}
                          </div>
                        );
                      })}
                  </div>
                  {!formData.idAtaque2 && (
                    <p className="selection-hint">Selecciona tu segundo ataque haciendo clic en una opción</p>
                  )}
                </div>
              )}
            </div>

            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">✨</span>
                Efecto Especial
              </label>
              
              {/* Selected Effect Display */}
              {formData.idEfecto && (
                <div className="selected-effect-display">
                  <div className="selected-effect-card">
                    {(() => {
                      const selectedEffect = availableEffects.find(e => e.id === parseInt(formData.idEfecto));
                      return selectedEffect ? (
                        <>
                          <div className="effect-header">
                            <span className="effect-icon">✨</span>
                            <span className="effect-name">{selectedEffect.nombre}</span>
                          </div>
                          <div className="effect-description">{selectedEffect.descripcion}</div>
                          <div className="effect-type">{selectedEffect.tipoEfecto}</div>
                        </>
                      ) : 'Cargando efecto...';
                    })()}
                  </div>
                </div>
              )}

              {/* Effects Selector */}
              {loadingEffects ? (
                <div className="loading-attacks">
                  <div className="loading-spinner"></div>
                  <p>Cargando efectos disponibles...</p>
                </div>
              ) : (
                <div className="effects-selector">
                  <div className="effects-grid">
                    {availableEffects.map(effect => (
                      <div
                        key={effect.id}
                        className={`effect-option ${formData.idEfecto === effect.id.toString() ? 'selected' : ''}`}
                        onClick={() => setFormData({...formData, idEfecto: effect.id.toString()})}
                      >
                        <div className="effect-info">
                          <span className="effect-name">{effect.nombre}</span>
                          <span className="effect-type-badge">{effect.tipoEfecto}</span>
                          <p className="effect-description">{effect.descripcion}</p>
                        </div>
                      </div>
                    ))}
                  </div>
                  {!formData.idEfecto && (
                    <p className="selection-hint">Selecciona el efecto especial de tu Pokémon</p>
                  )}
                </div>
              )}
            </div>

            <div className="form-group sprite-upload">
              <label className="form-label">
                <span className="label-icon">🖼️</span>
                Imagen del Pokémon
              </label>
              <div className="file-upload-area">
                <input
                  type="file"
                  name="sprite"
                  onChange={handleFileChange}
                  className="file-input"
                  accept="image/*"
                  required
                />
                <div className="file-upload-text">
                  <span className="upload-icon">📁</span>
                  <span>Selecciona una imagen</span>
                  <small>PNG, JPG, GIF (máx. 5MB)</small>
                </div>
              </div>
              {formData.sprite && (
                <div className="file-preview">
                  <span className="preview-icon">✅</span>
                  <span>Archivo seleccionado: {formData.sprite.name}</span>
                </div>
              )}
            </div>
          </div>

          <div className="action-section">
            <button 
              type="submit" 
              className={`btn btn-primary btn-lg ${isLoading ? 'btn-loading' : ''}`}
              disabled={isLoading}
            >
              {isLoading ? (
                <>
                  <div className="btn-spinner"></div>
                  Creando Pokémon...
                </>
              ) : (
                <>
                  <span className="btn-icon">⚡</span>
                  Crear Pokémon
                </>
              )}
            </button>
          </div>
        </form>

        <div className="info-card">
          <h3 className="info-title">
            <span className="title-icon">💡</span>
            Consejos para crear un Pokémon balanceado
          </h3>
          <ul className="tips-list">
            <li>
              <span className="tip-icon">⚖️</span>
              Mantén las estadísticas equilibradas para un Pokémon versátil
            </li>
            <li>
              <span className="tip-icon">🎯</span>
              Elige ataques que complementen el tipo de tu Pokémon
            </li>
            <li>
              <span className="tip-icon">🔄</span>
              Considera la sinergia entre los dos ataques seleccionados
            </li>
            <li>
              <span className="tip-icon">🎨</span>
              Usa una imagen clara y de buena calidad para el sprite
            </li>
          </ul>
        </div>
      </div>
    </div>
  );
};

export default CrearPokemon;
