import React, { useState, useEffect, useCallback } from "react";
import pokemonService from "../services/pokemon.service";
import entrenadorService from "../services/entrenador.service";
import "../App.css";

const CrearEntrenador = () => {
  const [pokemonData, setPokemonData] = useState({
    content: [],
    page: 0,
    size: 12,
    totalElements: 0,
    totalPages: 0,
    hasNext: false,
    hasPrevious: false
  });
  
  const [filters, setFilters] = useState({
    nombre: '',
    tipo: '',
    efecto: '',
    tipoAtaque: ''
  });
  
  const [selectedPokemons, setSelectedPokemons] = useState([]);
  const [trainerName, setTrainerName] = useState("");
  const [pokemonDetails, setPokemonDetails] = useState(null);
  const [loading, setLoading] = useState(false);
  const [successMessage, setSuccessMessage] = useState("");
  const [errorMessage, setErrorMessage] = useState("");
  
  // Available options for filters
  const [tiposDisponibles] = useState(["AGUA", "FUEGO", "PLANTA", "TIERRA", "ELECTRICO"]);
  const [efectosDisponibles] = useState(["DANO_CONTINUO", "SUBIR_ATAQUE_PROPIO", "SUBIR_DEFENSA_PROPIO", "BAJAR_ATAQUE_RIVAL", "BAJAR_DEFENSA_RIVAL", "SUBIR_VIDA"]);
  const [tiposAtaqueDisponibles] = useState(["AGUA", "FUEGO", "PLANTA", "TIERRA", "ELECTRICO"]);

  // Debounced search function
  const [searchTimeout, setSearchTimeout] = useState(null);

  // Fetch Pokémon with filters and pagination
  const fetchPokemons = useCallback(async (page = 0, resetData = false) => {
    try {
      setLoading(true);
      
      const searchParams = {
        page,
        size: pokemonData.size,
        ...filters
      };
      
      const response = await pokemonService.searchPokemon(searchParams);
      
      if (resetData || page === 0) {
        setPokemonData(response.data);
      } else {
        // Para cargar más (si implementamos scroll infinito en el futuro)
        setPokemonData(prev => ({
          ...response.data,
          content: [...prev.content, ...response.data.content]
        }));
      }
    } catch (error) {
      console.error("Error al obtener los Pokémon:", error);
      setErrorMessage("Error al cargar los Pokémon. Por favor, intenta nuevamente.");
    } finally {
      setLoading(false);
    }
  }, [filters, pokemonData.size]);

  // Initial load and when filters change
  useEffect(() => {
    fetchPokemons(0, true);
  }, [filters]);

  // Debounced search for name filter
  const handleNameFilterChange = (value) => {
    setFilters(prev => ({ ...prev, nombre: value }));
    
    // Clear previous timeout
    if (searchTimeout) {
      clearTimeout(searchTimeout);
    }
    
    // Set new timeout for debounced search
    const timeout = setTimeout(() => {
      // The search will be triggered by the useEffect above
    }, 500);
    
    setSearchTimeout(timeout);
  };

  // Handle other filter changes
  const handleFilterChange = (filterName, value) => {
    setFilters(prev => ({ ...prev, [filterName]: value }));
  };

  // Clear all filters
  const clearFilters = () => {
    setFilters({
      nombre: '',
      tipo: '',
      efecto: '',
      tipoAtaque: ''
    });
  };

  // Pagination functions
  const goToPage = (page) => {
    fetchPokemons(page, true);
  };

  const nextPage = () => {
    if (pokemonData.hasNext) {
      goToPage(pokemonData.page + 1);
    }
  };

  const prevPage = () => {
    if (pokemonData.hasPrevious) {
      goToPage(pokemonData.page - 1);
    }
  };

  // Pagination range calculator
  const getPaginationRange = () => {
    const { page, totalPages } = pokemonData;
    
    // Validar que tenemos datos válidos
    if (!page && page !== 0 || !totalPages || totalPages <= 0) {
      return [];
    }
    
    const delta = 2; // Number of pages to show on each side of current page
    const range = [];
    const rangeWithDots = [];

    for (let i = Math.max(0, page - delta); i <= Math.min(totalPages - 1, page + delta); i++) {
      range.push(i);
    }

    if (range.length === 0) {
      return [];
    }

    if (range[0] > 0) {
      if (range[0] > 1) {
        rangeWithDots.push(0, '...');
      } else {
        rangeWithDots.push(0);
      }
    }

    rangeWithDots.push(...range);

    if (range[range.length - 1] < totalPages - 1) {
      if (range[range.length - 1] < totalPages - 2) {
        rangeWithDots.push('...', totalPages - 1);
      } else {
        rangeWithDots.push(totalPages - 1);
      }
    }

    return rangeWithDots;
  };

  // Handle Pokémon selection
  const togglePokemonSelection = (pokemonId) => {
    if (selectedPokemons.includes(pokemonId)) {
      setSelectedPokemons(selectedPokemons.filter((id) => id !== pokemonId));
    } else if (selectedPokemons.length < 3) {
      setSelectedPokemons([...selectedPokemons, pokemonId]);
    } else {
      setErrorMessage("Solo puedes seleccionar hasta 3 Pokémon.");
      setTimeout(() => setErrorMessage(""), 3000);
    }
  };

  // Show Pokémon details
  const showDetails = async (id) => {
    try {
      const response = await pokemonService.getById(id);
      const attacks = await pokemonService.getAtaques(id);
      const efecto = await pokemonService.getEfecto(id);

      setPokemonDetails({
        ...response.data,
        attacks: attacks.data,
        efecto: efecto.data, // Agregar el efecto a los detalles
      });
    } catch (error) {
      console.error("Error al obtener los detalles del Pokémon:", error);
    }
  };

  // Handle trainer creation
  const handleCreateTrainer = async () => {
    // Clear previous messages
    setSuccessMessage("");
    setErrorMessage("");

    if (trainerName.trim() === "") {
      setErrorMessage("Debes ingresar un nombre para el entrenador.");
      return;
    }

    if (selectedPokemons.length !== 3) {
      setErrorMessage("Debes seleccionar exactamente 3 Pokémon.");
      return;
    }

    const trainerData = {
      nombre: trainerName.trim(),
      idPokemon1: selectedPokemons[0],
      idPokemon2: selectedPokemons[1],
      idPokemon3: selectedPokemons[2],
    };

    try {
      setLoading(true);
      const response = await entrenadorService.create(trainerData);
      setSuccessMessage(`¡Entrenador "${response.data.nombre}" creado exitosamente!`);
      setTrainerName("");
      setSelectedPokemons([]);
      
      // Clear success message after 5 seconds
      setTimeout(() => setSuccessMessage(""), 5000);
    } catch (error) {
      console.error("Error al crear el entrenador:", error);
      setErrorMessage("Error al crear el entrenador. Por favor, intenta nuevamente.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="page-container">
      <div className="header-section">
        <h1 className="page-title">
          <span className="title-icon">👨‍💼</span>
          Crear Entrenador
        </h1>
        <p className="page-description">
          Crea tu equipo de 3 Pokémon y conviértete en un entrenador legendario
        </p>
      </div>

      {/* Messages */}
      {successMessage && (
        <div className="alert alert-success">
          <span className="alert-icon">✅</span>
          {successMessage}
        </div>
      )}
      {errorMessage && (
        <div className="alert alert-error">
          <span className="alert-icon">❌</span>
          {errorMessage}
        </div>
      )}

      {/* Trainer Name Form */}
      <div className="form-section">
        <div className="form-group">
          <label className="form-label">
            <span className="label-icon">👤</span>
            Nombre del Entrenador
          </label>
          <input
            type="text"
            className="form-input"
            value={trainerName}
            onChange={(e) => setTrainerName(e.target.value)}
            placeholder="Ingresa tu nombre de entrenador..."
            maxLength={30}
            required
          />
          <small className="form-hint">
            Elige un nombre único que te represente como entrenador
          </small>
        </div>
      </div>

      {/* Pokemon Selection Section */}
      <div className="selection-section">
        <div className="section-header">
          <h2 className="section-title">
            <span className="title-icon">⚡</span>
            Selecciona tu Equipo ({selectedPokemons.length}/3)
          </h2>
          <div className="progress-indicator">
            <div className="progress-bar">
              <div 
                className="progress-fill" 
                style={{width: `${(selectedPokemons.length / 3) * 100}%`}}
              ></div>
            </div>
            <span className="progress-text">
              {selectedPokemons.length === 3 ? "¡Equipo completo!" : `${3 - selectedPokemons.length} Pokémon restantes`}
            </span>
          </div>
        </div>

        {/* Advanced Filters Section */}
        <div className="filters-section">
          <div className="section-header">
            <h3 className="section-title">
              <span className="title-icon">🔍</span>
              Filtros de Búsqueda
            </h3>
            <button 
              className="btn btn-secondary btn-sm" 
              onClick={clearFilters}
              title="Limpiar todos los filtros"
            >
              <span className="btn-icon">🧹</span>
              Limpiar
            </button>
          </div>
          
          <div className="filters-grid">
            {/* Name Search */}
            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">🏷️</span>
                Buscar por nombre
              </label>
              <input
                type="text"
                className="form-input"
                value={filters.nombre}
                onChange={(e) => handleNameFilterChange(e.target.value)}
                placeholder="Ej: Pikachu..."
              />
              <small className="form-hint">Búsqueda en tiempo real</small>
            </div>

            {/* Type Filter */}
            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">🌟</span>
                Tipo de Pokémon
              </label>
              <select 
                className="form-select" 
                value={filters.tipo}
                onChange={(e) => handleFilterChange('tipo', e.target.value)}
              >
                <option value="">🌟 Todos los tipos</option>
                {tiposDisponibles.map((tipo) => (
                  <option key={tipo} value={tipo}>
                    {getTypeIcon(tipo)} {tipo}
                  </option>
                ))}
              </select>
            </div>

            {/* Effect Filter */}
            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">✨</span>
                Efecto Especial
              </label>
              <select 
                className="form-select" 
                value={filters.efecto}
                onChange={(e) => handleFilterChange('efecto', e.target.value)}
              >
                <option value="">✨ Todos los efectos</option>
                {efectosDisponibles.map((efecto) => (
                  <option key={efecto} value={efecto}>
                    {getEffectIcon(efecto)} {efecto.replace(/_/g, ' ')}
                  </option>
                ))}
              </select>
            </div>

            {/* Attack Type Filter */}
            <div className="form-group">
              <label className="form-label">
                <span className="label-icon">⚔️</span>
                Tipo de Ataque
              </label>
              <select 
                className="form-select" 
                value={filters.tipoAtaque}
                onChange={(e) => handleFilterChange('tipoAtaque', e.target.value)}
              >
                <option value="">⚔️ Todos los ataques</option>
                {tiposAtaqueDisponibles.map((tipo) => (
                  <option key={tipo} value={tipo}>
                    {getTypeIcon(tipo)} {tipo}
                  </option>
                ))}
              </select>
              <small className="form-hint">Pokémon que tengan al menos un ataque de este tipo</small>
            </div>
          </div>

          {/* Search Results Info */}
          <div className="search-info">
            <div className="results-count">
              <span className="count-icon">📊</span>
              <span>
                {loading ? "Buscando..." : 
                  `${pokemonData.totalElements} Pokémon encontrados`
                }
              </span>
            </div>
            {(filters.nombre || filters.tipo || filters.efecto || filters.tipoAtaque) && (
              <div className="active-filters">
                <span className="filters-label">Filtros activos:</span>
                <div className="filter-tags">
                  {filters.nombre && (
                    <span className="filter-tag">
                      🏷️ "{filters.nombre}"
                      <button onClick={() => handleFilterChange('nombre', '')}>✕</button>
                    </span>
                  )}
                  {filters.tipo && (
                    <span className="filter-tag">
                      {getTypeIcon(filters.tipo)} {filters.tipo}
                      <button onClick={() => handleFilterChange('tipo', '')}>✕</button>
                    </span>
                  )}
                  {filters.efecto && (
                    <span className="filter-tag">
                      {getEffectIcon(filters.efecto)} {filters.efecto.replace(/_/g, ' ')}
                      <button onClick={() => handleFilterChange('efecto', '')}>✕</button>
                    </span>
                  )}
                  {filters.tipoAtaque && (
                    <span className="filter-tag">
                      ⚔️ {filters.tipoAtaque}
                      <button onClick={() => handleFilterChange('tipoAtaque', '')}>✕</button>
                    </span>
                  )}
                </div>
              </div>
            )}
          </div>
        </div>

        {/* Loading State */}
        {loading && (
          <div className="loading-container">
            <div className="loading-spinner"></div>
            <p>Cargando Pokémon...</p>
          </div>
        )}

        {/* Pokemon Grid */}
        {!loading && (
          <div className="pokemon-grid">
            {pokemonData.content.map((pokemon) => (
              <div 
                key={pokemon.id} 
                className={`pokemon-card ${selectedPokemons.includes(pokemon.id) ? 'selected' : ''}`}
              >
                <div className="pokemon-image-container">
                  <img
                    src={`data:image/png;base64,${pokemon.sprite}`}
                    alt={pokemon.nombre}
                    className="pokemon-sprite"
                  />
                  {selectedPokemons.includes(pokemon.id) && (
                    <div className="selection-badge">
                      ✓ Seleccionado
                    </div>
                  )}
                </div>
                
                <div className="pokemon-info">
                  <h3 className="pokemon-name">{pokemon.nombre}</h3>
                  <div className="pokemon-type">
                    <span className={`type-badge type-${pokemon.tipoPokemon.toLowerCase()}`}>
                      {getTypeIcon(pokemon.tipoPokemon)} {pokemon.tipoPokemon}
                    </span>
                  </div>
                  <div className="pokemon-stats">
                    <div className="stat">
                      <span className="stat-icon">❤️</span>
                      <span>{pokemon.vida}</span>
                    </div>
                    <div className="stat">
                      <span className="stat-icon">⚔️</span>
                      <span>{pokemon.ataque}</span>
                    </div>
                    <div className="stat">
                      <span className="stat-icon">🛡️</span>
                      <span>{pokemon.defensa}</span>
                    </div>
                  </div>
                </div>

                <div className="pokemon-actions">
                  <button 
                    className="btn btn-secondary btn-sm" 
                    onClick={() => showDetails(pokemon.id)}
                  >
                    <span className="btn-icon">👁️</span>
                    Ver Detalles
                  </button>
                  <button
                    onClick={() => togglePokemonSelection(pokemon.id)}
                    className={`btn btn-sm ${
                      selectedPokemons.includes(pokemon.id) 
                        ? 'btn-danger' 
                        : selectedPokemons.length >= 3 
                          ? 'btn-disabled' 
                          : 'btn-primary'
                    }`}
                    disabled={selectedPokemons.length >= 3 && !selectedPokemons.includes(pokemon.id)}
                  >
                    <span className="btn-icon">
                      {selectedPokemons.includes(pokemon.id) ? '✖️' : '✅'}
                    </span>
                    {selectedPokemons.includes(pokemon.id) ? "Quitar" : "Seleccionar"}
                  </button>
                </div>
              </div>
            ))}
          </div>
        )}

        {/* Pagination */}
        {!loading && pokemonData.totalPages > 1 && (
          <div className="pagination-section">
            <div className="pagination-info">
              <span>
                Página {pokemonData.page + 1} de {pokemonData.totalPages} 
                ({pokemonData.totalElements} Pokémon en total)
              </span>
            </div>
            
            <div className="pagination-controls">
              <button 
                className="btn btn-secondary btn-sm"
                onClick={prevPage}
                disabled={!pokemonData.hasPrevious}
              >
                <span className="btn-icon">⬅️</span>
                Anterior
              </button>
              
              <div className="page-numbers">
                {getPaginationRange().map((pageNum, index) => (
                  pageNum === '...' ? (
                    <span key={index} className="page-ellipsis">...</span>
                  ) : (
                    <button
                      key={pageNum}
                      className={`btn btn-sm ${pokemonData.page === pageNum ? 'btn-primary' : 'btn-outline'}`}
                      onClick={() => goToPage(pageNum)}
                    >
                      {pageNum + 1}
                    </button>
                  )
                ))}
              </div>
              
              <button 
                className="btn btn-secondary btn-sm"
                onClick={nextPage}
                disabled={!pokemonData.hasNext}
              >
                Siguiente
                <span className="btn-icon">➡️</span>
              </button>
            </div>
          </div>
        )}

        {!loading && pokemonData.content.length === 0 && (
          <div className="empty-state">
            <div className="empty-icon">🔍</div>
            <h3>No se encontraron Pokémon</h3>
            <p>Intenta ajustar los filtros para ver más opciones</p>
            <button className="btn btn-primary btn-sm" onClick={clearFilters}>
              <span className="btn-icon">🧹</span>
              Limpiar filtros
            </button>
          </div>
        )}
      </div>

      {/* Pokemon Details Modal */}
      {pokemonDetails && (
        <div className="modal-overlay" onClick={() => setPokemonDetails(null)}>
          <div className="modal-content" onClick={(e) => e.stopPropagation()}>
            <div className="modal-header">
              <h2 className="modal-title">
                <span className="title-icon">⚡</span>
                Detalles de {pokemonDetails.nombre}
              </h2>
              <button 
                className="modal-close" 
                onClick={() => setPokemonDetails(null)}
                aria-label="Cerrar"
              >
                ✕
              </button>
            </div>
            
            <div className="modal-body">
              <div className="pokemon-detail-grid">
                <div className="pokemon-detail-image">
                  <img
                    src={`data:image/png;base64,${pokemonDetails.sprite}`}
                    alt={pokemonDetails.nombre}
                    className="detail-sprite"
                  />
                  <div className={`type-badge type-${pokemonDetails.tipoPokemon.toLowerCase()}`}>
                    {getTypeIcon(pokemonDetails.tipoPokemon)} {pokemonDetails.tipoPokemon}
                  </div>
                </div>
                
                <div className="pokemon-detail-stats">
                  <h3 className="stats-title">Estadísticas</h3>
                  <div className="stats-grid">
                    <div className="stat-item">
                      <span className="stat-label">
                        <span className="stat-icon">❤️</span>
                        Vida
                      </span>
                      <div className="stat-bar">
                        <div 
                          className="stat-fill stat-hp" 
                          style={{width: `${(pokemonDetails.vida / 150) * 100}%`}}
                        ></div>
                        <span className="stat-value">{pokemonDetails.vida}</span>
                      </div>
                    </div>
                    
                    <div className="stat-item">
                      <span className="stat-label">
                        <span className="stat-icon">⚔️</span>
                        Ataque
                        {pokemonDetails.ataqueModificado && pokemonDetails.ataqueModificado !== pokemonDetails.ataque && (
                          <span className="stat-modified"> (Modificado)</span>
                        )}
                      </span>
                      <div className="stat-bar">
                        <div 
                          className="stat-fill stat-attack" 
                          style={{width: `${((pokemonDetails.ataqueModificado || pokemonDetails.ataque) / 150) * 100}%`}}
                        ></div>
                        <span className="stat-value">
                          {pokemonDetails.ataqueModificado || pokemonDetails.ataque}
                          {pokemonDetails.ataqueModificado && pokemonDetails.ataqueModificado !== pokemonDetails.ataque && (
                            <small className="base-stat"> (Base: {pokemonDetails.ataque})</small>
                          )}
                        </span>
                      </div>
                    </div>
                    
                    <div className="stat-item">
                      <span className="stat-label">
                        <span className="stat-icon">🛡️</span>
                        Defensa
                        {pokemonDetails.defensaModificada && pokemonDetails.defensaModificada !== pokemonDetails.defensa && (
                          <span className="stat-modified"> (Modificado)</span>
                        )}
                      </span>
                      <div className="stat-bar">
                        <div 
                          className="stat-fill stat-defense" 
                          style={{width: `${((pokemonDetails.defensaModificada || pokemonDetails.defensa) / 150) * 100}%`}}
                        ></div>
                        <span className="stat-value">
                          {pokemonDetails.defensaModificada || pokemonDetails.defensa}
                          {pokemonDetails.defensaModificada && pokemonDetails.defensaModificada !== pokemonDetails.defensa && (
                            <small className="base-stat"> (Base: {pokemonDetails.defensa})</small>
                          )}
                        </span>
                      </div>
                    </div>
                  </div>
                </div>
              </div>
              
              <div className="attacks-section">
                <h3 className="attacks-title">
                  <span className="title-icon">💥</span>
                  Ataques Disponibles
                </h3>
                <div className="attacks-grid">
                  {pokemonDetails.attacks.map((attack) => (
                    <div key={attack.id} className="attack-card">
                      <div className="attack-header">
                        <h4 className="attack-name">{attack.nombre}</h4>
                        <span className={`attack-type type-${attack.tipoAtaque.toLowerCase()}`}>
                          {getTypeIcon(attack.tipoAtaque)}
                        </span>
                      </div>
                      <p className="attack-description">{attack.descripcion}</p>
                    </div>
                  ))}
                </div>
              </div>

              {/* Effects Section */}
              {pokemonDetails.efecto && (
                <div className="effects-section">
                  <h3 className="effects-title">
                    <span className="title-icon">✨</span>
                    Efecto Especial
                  </h3>
                  <div className="effect-detail-card">
                    <div className="effect-detail-header">
                      <h4 className="effect-detail-name">{pokemonDetails.efecto.nombre}</h4>
                      <span className="effect-detail-type">{pokemonDetails.efecto.tipoEfecto}</span>
                    </div>
                    <p className="effect-detail-description">{pokemonDetails.efecto.descripcion}</p>
                  </div>
                </div>
              )}
            </div>
          </div>
        </div>
      )}

      {/* Create Trainer Button */}
      <div className="action-section">
        <button 
          onClick={handleCreateTrainer} 
          className={`btn btn-primary btn-lg ${loading ? 'btn-loading' : ''}`}
          disabled={loading || selectedPokemons.length !== 3 || !trainerName.trim()}
        >
          {loading ? (
            <>
              <div className="btn-spinner"></div>
              Creando Entrenador...
            </>
          ) : (
            <>
              <span className="btn-icon">🎯</span>
              Crear Entrenador
            </>
          )}
        </button>
        
        {selectedPokemons.length !== 3 || !trainerName.trim() ? (
          <small className="form-hint">
            {!trainerName.trim() && "Ingresa un nombre para continuar"}
            {trainerName.trim() && selectedPokemons.length !== 3 && 
              `Selecciona ${3 - selectedPokemons.length} Pokémon más para completar tu equipo`
            }
          </small>
        ) : (
          <small className="form-success">
            ¡Todo listo! Tu equipo está completo.
          </small>
        )}
      </div>
    </div>
  );
};

// Helper function for type icons
const getTypeIcon = (tipo) => {
  const icons = {
    AGUA: "💧",
    FUEGO: "🔥",
    PLANTA: "🌿",
    TIERRA: "🌍",
    ELECTRICO: "⚡"
  };
  return icons[tipo] || "⭐";
};

// Helper function for effect icons
const getEffectIcon = (efecto) => {
  const icons = {
    DANO_CONTINUO: "🍄",
    SUBIR_ATAQUE_PROPIO: "⚔️",
    SUBIR_DEFENSA_PROPIO: "🛡️",
    BAJAR_ATAQUE_RIVAL: "💥",
    BAJAR_DEFENSA_RIVAL: "🔱",
    SUBIR_VIDA: "❤️"
  };
  return icons[efecto] || "✨";
};

export default CrearEntrenador;
