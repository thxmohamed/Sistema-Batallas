import React from 'react';
import { useNavigate } from 'react-router-dom';
import '../App.css';

const Home = () => {
  const navigate = useNavigate();

  const goToCreateTeam = () => {
    navigate('/entrenador/crear');
  };

  return (
    <div className="home-container">
      <h1 className="heading">Hola <br/> ¿Qué desea hacer? </h1>
      
      <button className="button-home" onClick={goToCreateTeam}>
        Crear un equipo
      </button>
      <button className="button-home" onClick={() => navigate('/setup')}>
        Combatir
      </button>
    </div>
  );
};

export default Home;
