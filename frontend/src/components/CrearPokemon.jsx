import React, { useState } from 'react';
import pokemonService from '../Services/pokemon.service';
import '../App.css';

const CrearPokemon = () => {
  const [formData, setFormData] = useState({
    nombre: '',
    tipoPokemon: '',
    vida: '',
    ataque: '',
    defensa: '',
    velocidad: '',
    idAtaque1: '',
    idAtaque2: '',
    idEfecto: '',
    estado: '',
    sprite: null,
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData({ ...formData, [name]: value });
  };

  const handleFileChange = (e) => {
    setFormData({ ...formData, sprite: e.target.files[0] });
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    const data = new FormData();
    Object.entries(formData).forEach(([key, value]) => {
      const isNumber = !isNaN(value) && key !== "sprite";
      data.append(key, isNumber ? Number(value) : value);
      console.log("hola")
      console.log(key, value);
      
    });

    try {
      const response = await pokemonService.create(data);
      alert(`Pokemon creado exitosamente: ${response.data.nombre}`);
      setFormData({
        nombre: '',
        tipoPokemon: '',
        vida: '',
        ataque: '',
        defensa: '',
        velocidad: '',
        idAtaque1: '',
        idAtaque2: '',
        idEfecto: '',
        estado: '',
        sprite: null,
      });
    } catch (error) {
      console.error('Error al crear el Pokemon:', error);
      alert('Error al crear el Pokemon.');
    }
  };

  return (
    <div className="crear-pokemon">
      <h1>Crear Pokemon</h1>
      <form onSubmit={handleSubmit}>
        <div>
          <label>Nombre:</label>
          <input
            type="text"
            name="nombre"
            value={formData.nombre}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Tipo:</label>
          <select
            name="tipoPokemon"
            value={formData.tipoPokemon}
            onChange={handleChange}
            required
          >
            <option value="">Seleccionar...</option>
            <option value="AGUA">Agua</option>
            <option value="FUEGO">Fuego</option>
            <option value="PLANTA">Planta</option>
            <option value="TIERRA">Tierra</option>
            <option value="ELECTRICO">Eléctrico</option>
          </select>
        </div>
        <div>
          <label>Vida:</label>
          <input
            type="number"
            name="vida"
            value={formData.vida}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Ataque:</label>
          <input
            type="number"
            name="ataque"
            value={formData.ataque}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Defensa:</label>
          <input
            type="number"
            name="defensa"
            value={formData.defensa}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Velocidad:</label>
          <input
            type="number"
            name="velocidad"
            value={formData.velocidad}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>ID Ataque 1:</label>
          <input
            type="number"
            name="idAtaque1"
            value={formData.idAtaque1}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>ID Ataque 2:</label>
          <input
            type="number"
            name="idAtaque2"
            value={formData.idAtaque2}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>ID Efecto:</label>
          <input
            type="number"
            name="idEfecto"
            value={formData.idEfecto}
            onChange={handleChange}
            required
          />
        </div>
        <div>
          <label>Estado:</label>
          <select
            name="estado"
            value={formData.estado}
            onChange={handleChange}
            required
          >
            <option value="">Seleccionar...</option>
            <option value="1">Primera evolución</option>
            <option value="2">Segunda evolución</option>
            <option value="3">Última evolución</option>
            <option value="4">No evoluciona</option>
          </select>
        </div>
        <div>
          <label>Sprite:</label>
          <input
            type="file"
            accept="image/*"
            onChange={handleFileChange}
          />
        </div>
        <button type="submit">Crear Pokemon</button>
      </form>
    </div>
  );
};

export default CrearPokemon;
