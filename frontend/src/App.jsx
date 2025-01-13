import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Link, Navigate } from 'react-router-dom';
import CrearPokemon from './components/CrearPokemon';
import CrearEntrenador from './components/CrearEntrenador';
import Home from './components/Home';
import BattleSetupView from './components/BattleSetupView';
import BattleView from './components/BattleView';

function App() {

  return (
    <Router>
      <div className="App">

        <Routes>
          <Route path="/pokemon/crear" element={<CrearPokemon />} />
          <Route path="/entrenador/crear" element={<CrearEntrenador />} />
          <Route path="/battle" element={<BattleView />} />
          <Route path="/" element={<Home />} />
          <Route path="/setup" element={<BattleSetupView />} />
          <Route path="*" element={<Navigate to="/" />} />

        </Routes>
      </div>
    </Router>
  );
}

export default App;
