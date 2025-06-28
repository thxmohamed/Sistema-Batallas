import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Link, Navigate } from 'react-router-dom';
import CrearPokemon from './components/CrearPokemon';
import CrearEntrenador from './components/CrearEntrenador';
import Home from './components/Home';
import BattleSetupView from './components/BattleSetupView';
import BattleView from './components/BattleView';
import Informacion from './components/Informacion';
import './App.css';

function App() {
  return (
    <Router>
      <div className="app-container">
        <nav className="navbar">
          <Link to="/" className="nav-logo">⚡ PokéBattle</Link>
          <ul className="nav-links">
            <li><Link to="/" className="nav-link">🏠 Inicio</Link></li>
            <li><Link to="/pokemon/crear" className="nav-link">🐾 Crear Pokémon</Link></li>
            <li><Link to="/entrenador/crear" className="nav-link">👤 Crear Entrenador</Link></li>
            <li><Link to="/setup" className="nav-link">⚔️ Batalla</Link></li>
            <li><Link to="/informacion" className="nav-link">ℹ️ Info</Link></li>
          </ul>
        </nav>

        <Routes>
          <Route path="/pokemon/crear" element={<CrearPokemon />} />
          <Route path="/entrenador/crear" element={<CrearEntrenador />} />
          <Route path="/battle" element={<BattleView />} />
          <Route path="/" element={<Home />} />
          <Route path="/setup" element={<BattleSetupView />} />
          <Route path="/informacion" element={<Informacion />} />
          <Route path="*" element={<Navigate to="/" />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
