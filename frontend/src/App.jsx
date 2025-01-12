import React, { useState } from 'react';
import { BrowserRouter as Router, Route, Routes, Link, Navigate } from 'react-router-dom';
import CrearPokemon from './components/CrearPokemon';
import CrearEntrenador from './components/CrearEntrenador';

function App() {
  const [isLoggedIn, setIsLoggedIn] = useState(false);

  const handleLogin = () => {
    setIsLoggedIn(true);
  };

  const handleLogout = () => {
    setIsLoggedIn(false);
  };

  return (
    <Router>
      <div className="App">

        <Routes>
          <Route path="/pokemon/crear" element={<CrearPokemon />} />
          <Route path="/entrenador/crear" element={<CrearEntrenador />} />
        </Routes>
      </div>
    </Router>
  );
}

export default App;
