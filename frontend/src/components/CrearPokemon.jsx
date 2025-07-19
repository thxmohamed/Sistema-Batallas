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
  
  // Search filters for attacks
  const [attackFilters, setAttackFilters] = useState({
    attack1: { name: '', type: '' },
    attack2: { name: '', type: '' }
  });

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
      NORMAL: { icon: "⭐", color: "#A8A878", bgColor: "rgba(168, 168, 120, 0.1)" },
      FUEGO: { icon: "🔥", color: "#F08030", bgColor: "rgba(240, 128, 48, 0.1)" },
      AGUA: { icon: "💧", color: "#6890F0", bgColor: "rgba(104, 144, 240, 0.1)" },
      ELECTRICO: { icon: "⚡", color: "#F8D030", bgColor: "rgba(248, 208, 48, 0.1)" },
      PLANTA: { icon: "🌿", color: "#78C850", bgColor: "rgba(120, 200, 80, 0.1)" },
      HIELO: { icon: "❄️", color: "#98D8D8", bgColor: "rgba(152, 216, 216, 0.1)" },
      LUCHA: { icon: "👊", color: "#C03028", bgColor: "rgba(192, 48, 40, 0.1)" },
      VENENO: { icon: "☠️", color: "#A040A0", bgColor: "rgba(160, 64, 160, 0.1)" },
      TIERRA: { icon: "🌍", color: "#E0C068", bgColor: "rgba(224, 192, 104, 0.1)" },
      VOLADOR: { icon: "🦅", color: "#A890F0", bgColor: "rgba(168, 144, 240, 0.1)" },
      PSIQUICO: { icon: "🔮", color: "#F85888", bgColor: "rgba(248, 88, 136, 0.1)" },
      BICHO: { icon: "🐛", color: "#A8B820", bgColor: "rgba(168, 184, 32, 0.1)" },
      ROCA: { icon: "⛰️", color: "#B8A038", bgColor: "rgba(184, 160, 56, 0.1)" },
      FANTASMA: { icon: "👻", color: "#705898", bgColor: "rgba(112, 88, 152, 0.1)" },
      DRAGON: { icon: "🐉", color: "#7038F8", bgColor: "rgba(112, 56, 248, 0.1)" },
      SINIESTRO: { icon: "🌙", color: "#705848", bgColor: "rgba(112, 88, 72, 0.1)" },
      ACERO: { icon: "⚙️", color: "#B8B8D0", bgColor: "rgba(184, 184, 208, 0.1)" },
      HADA: { icon: "✨", color: "#EE99AC", bgColor: "rgba(238, 153, 172, 0.1)" }
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

  // Filter attacks based on search criteria
  const filterAttacks = (attackKey, excludeAttackId = null) => {
    const filters = attackFilters[attackKey];
    return availableAttacks.filter(attack => {
      // Exclude the other selected attack
      if (excludeAttackId && attack.id.toString() === excludeAttackId) {
        return false;
      }
      
      // Filter by name
      if (filters.name && !attack.nombre.toLowerCase().includes(filters.name.toLowerCase())) {
        return false;
      }
      
      // Filter by type
      if (filters.type && attack.tipoAtaque !== filters.type) {
        return false;
      }
      
      return true;
    });
  };

  const handleAttackFilterChange = (attackKey, filterType, value) => {
    setAttackFilters(prev => ({
      ...prev,
      [attackKey]: {
        ...prev[attackKey],
        [filterType]: value
      }
    }));
  };

  // Get unique attack types for filter dropdown
  const getUniqueAttackTypes = () => {
    const types = [...new Set(availableAttacks.map(attack => attack.tipoAtaque))];
    return types.sort();
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
                <option value="NORMAL">⭐ Normal</option>
                <option value="FUEGO">🔥 Fuego</option>
                <option value="AGUA">💧 Agua</option>
                <option value="ELECTRICO">⚡ Eléctrico</option>
                <option value="PLANTA">🌿 Planta</option>
                <option value="HIELO">❄️ Hielo</option>
                <option value="LUCHA">👊 Lucha</option>
                <option value="VENENO">☠️ Veneno</option>
                <option value="TIERRA">🌍 Tierra</option>
                <option value="VOLADOR">🦅 Volador</option>
                <option value="PSIQUICO">🔮 Psíquico</option>
                <option value="BICHO">🐛 Bicho</option>
                <option value="ROCA">⛰️ Roca</option>
                <option value="FANTASMA">👻 Fantasma</option>
                <option value="DRAGON">🐉 Dragón</option>
                <option value="SINIESTRO">🌙 Siniestro</option>
                <option value="ACERO">⚙️ Acero</option>
                <option value="HADA">✨ Hada</option>
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
                  
                  {/* Search Filters */}
                  <div className="attack-filters">
                    <div className="filter-row">
                      <div className="filter-group">
                        <label className="filter-label">🔍 Buscar por nombre:</label>
                        <input
                          type="text"
                          className="filter-input"
                          placeholder="Nombre del ataque..."
                          value={attackFilters.attack1.name}
                          onChange={(e) => handleAttackFilterChange('attack1', 'name', e.target.value)}
                        />
                      </div>
                      <div className="filter-group">
                        <label className="filter-label">🌟 Filtrar por tipo:</label>
                        <select
                          className="filter-select"
                          value={attackFilters.attack1.type}
                          onChange={(e) => handleAttackFilterChange('attack1', 'type', e.target.value)}
                        >
                          <option value="">Todos los tipos</option>
                          {getUniqueAttackTypes().map(type => (
                            <option key={type} value={type}>
                              {getTypeStyle(type).icon} {type}
                            </option>
                          ))}
                        </select>
                      </div>
                      {(attackFilters.attack1.name || attackFilters.attack1.type) && (
                        <button
                          type="button"
                          className="clear-filters-btn"
                          onClick={() => setAttackFilters(prev => ({
                            ...prev,
                            attack1: { name: '', type: '' }
                          }))}
                        >
                          ✕ Limpiar
                        </button>
                      )}
                    </div>
                  </div>
                  
                  <div className="attack-options-grid">
                    {filterAttacks('attack1').map((attack) => {
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
                  {filterAttacks('attack1').length === 0 && availableAttacks.length > 0 && (
                    <div className="no-results-message">
                      <span className="no-results-icon">🔍</span>
                      <p>No se encontraron ataques con los filtros aplicados</p>
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        onClick={() => setAttackFilters(prev => ({
                          ...prev,
                          attack1: { name: '', type: '' }
                        }))}
                      >
                        Limpiar filtros
                      </button>
                    </div>
                  )}
                  {!formData.idAtaque1 && filterAttacks('attack1').length > 0 && (
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
                  
                  {/* Search Filters */}
                  <div className="attack-filters">
                    <div className="filter-row">
                      <div className="filter-group">
                        <label className="filter-label">🔍 Buscar por nombre:</label>
                        <input
                          type="text"
                          className="filter-input"
                          placeholder="Nombre del ataque..."
                          value={attackFilters.attack2.name}
                          onChange={(e) => handleAttackFilterChange('attack2', 'name', e.target.value)}
                        />
                      </div>
                      <div className="filter-group">
                        <label className="filter-label">🌟 Filtrar por tipo:</label>
                        <select
                          className="filter-select"
                          value={attackFilters.attack2.type}
                          onChange={(e) => handleAttackFilterChange('attack2', 'type', e.target.value)}
                        >
                          <option value="">Todos los tipos</option>
                          {getUniqueAttackTypes().map(type => (
                            <option key={type} value={type}>
                              {getTypeStyle(type).icon} {type}
                            </option>
                          ))}
                        </select>
                      </div>
                      {(attackFilters.attack2.name || attackFilters.attack2.type) && (
                        <button
                          type="button"
                          className="clear-filters-btn"
                          onClick={() => setAttackFilters(prev => ({
                            ...prev,
                            attack2: { name: '', type: '' }
                          }))}
                        >
                          ✕ Limpiar
                        </button>
                      )}
                    </div>
                  </div>
                  
                  <div className="attack-options-grid">
                    {filterAttacks('attack2').map((attack) => {
                      const typeStyle = getTypeStyle(attack.tipoAtaque);
                      const isSelected = formData.idAtaque2 === attack.id.toString();
                      const isUsedInPrimary = formData.idAtaque1 === attack.id.toString();
                      
                      // Don't show if already selected as primary attack
                      if (isUsedInPrimary) return null;
                      
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
                  {filterAttacks('attack2').filter(attack => attack.id.toString() !== formData.idAtaque1).length === 0 && availableAttacks.length > 0 && (
                    <div className="no-results-message">
                      <span className="no-results-icon">🔍</span>
                      <p>No se encontraron ataques con los filtros aplicados</p>
                      <button
                        type="button"
                        className="btn btn-secondary btn-sm"
                        onClick={() => setAttackFilters(prev => ({
                          ...prev,
                          attack2: { name: '', type: '' }
                        }))}
                      >
                        Limpiar filtros
                      </button>
                    </div>
                  )}
                  {!formData.idAtaque2 && filterAttacks('attack2').filter(attack => attack.id.toString() !== formData.idAtaque1).length > 0 && (
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
              
              {loadingEffects ? (
                <div className="loading-selector">
                  <div className="loading-spinner"></div>
                  <span>Cargando efectos especiales...</span>
                </div>
              ) : (
                <div className="visual-effects-selector">
                  <input
                    type="hidden"
                    name="idEfecto"
                    value={formData.idEfecto}
                    required
                  />
                  <div className="effects-options-grid">
                    {availableEffects.map((effect) => {
                      const isSelected = formData.idEfecto === effect.id.toString();
                      const effectTypeColors = {
                        'DANO_CONTINUO': { color: '#8b5cf1', bgColor: 'rgba(139, 92, 246, 0.1)', icon: '🍄' },
                        'SUBIR_ATAQUE_PROPIO': { color: '#ef4444', bgColor: 'rgba(239, 68, 68, 0.1)', icon: '⚔️' },
                        'SUBIR_DEFENSA_PROPIO': { color: '#3b82f6', bgColor: 'rgba(59, 130, 246, 0.1)', icon: '🛡️' },
                        'BAJAR_ATAQUE_RIVAL': { color: '#f97316', bgColor: 'rgba(249, 115, 22, 0.1)', icon: '💥' },
                        'BAJAR_DEFENSA_RIVAL': { color: '#eab308', bgColor: 'rgba(234, 179, 8, 0.1)', icon: '🔱' },
                        'SUBIR_VIDA': { color: '#22c55e', bgColor: 'rgba(34, 197, 94, 0.1)', icon: '❤️' }
                      };
                      const typeStyle = effectTypeColors[effect.tipoEfecto] || { color: '#6b7280', bgColor: 'rgba(107, 114, 128, 0.1)', icon: '✨' };
                      
                      return (
                        <div
                          key={effect.id}
                          className={`effect-option-card ${isSelected ? 'selected' : ''}`}
                          onClick={() => setFormData({ ...formData, idEfecto: effect.id.toString() })}
                          style={{
                            borderColor: isSelected ? typeStyle.color : '#e5e7eb',
                            backgroundColor: isSelected ? typeStyle.bgColor : 'white'
                          }}
                        >
                          <div className="effect-icon-container" style={{ color: typeStyle.color }}>
                            <span className="effect-main-icon">{typeStyle.icon}</span>
                          </div>
                          <div className="effect-content">
                            <h4 className="effect-name" style={{ color: isSelected ? typeStyle.color : '#374151' }}>
                              {effect.nombre}
                            </h4>
                            <span className="effect-type-badge" style={{ 
                              backgroundColor: typeStyle.color,
                              color: 'white'
                            }}>
                              {effect.tipoEfecto.replace(/_/g, ' ')}
                            </span>
                            <p className="effect-description">{effect.descripcion}</p>
                            {effect.multiplicador && (
                              <div className="effect-multiplier" style={{ color: typeStyle.color }}>
                                <span className="multiplier-icon">📊</span>
                                Multiplicador: {effect.multiplicador}x
                              </div>
                            )}
                          </div>
                          {isSelected && (
                            <div className="selected-indicator" style={{ backgroundColor: typeStyle.color }}>
                              <span>✓</span>
                            </div>
                          )}
                        </div>
                      );
                    })}
                  </div>
                  {!formData.idEfecto && (
                    <div className="selection-hint-box">
                      <span className="hint-icon">💡</span>
                      <p>Selecciona un efecto especial para tu Pokémon. Cada efecto tiene diferentes propósitos estratégicos en batalla.</p>
                    </div>
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
            Consejos para crear un Pokémon balanceado:
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
