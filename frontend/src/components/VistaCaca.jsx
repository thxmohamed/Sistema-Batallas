import React, { useState } from 'react';

export default function RegistrarVacuna() {
  const [vacuna, setVacuna] = useState('');
  const [fechaAplicacion, setFechaAplicacion] = useState('');
  const [dosis, setDosis] = useState('');
  const [programarSiguiente, setProgramarSiguiente] = useState(false);
  const [fechaProxima, setFechaProxima] = useState('');

  const handleGuardar = (e) => {
    e.preventDefault();
    alert('Vacuna registrada correctamente.');
    // Aquí podrías guardar los datos en el backend o actualizar el historial.
  };

  return (
    <div style={{ fontFamily: 'sans-serif', backgroundColor: '#f2f4f8', minHeight: '100vh', padding: '32px' }}>
      <div style={{
        maxWidth: '600px',
        margin: 'auto',
        backgroundColor: 'white',
        padding: '32px',
        borderRadius: '12px',
        boxShadow: '0 1px 6px rgba(0,0,0,0.1)'
      }}>
        <h2 style={{ fontSize: '22px', fontWeight: 'bold', color: '#2b3a55', marginBottom: '24px' }}>
          Registrar Vacuna
        </h2>

        <form onSubmit={handleGuardar} style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>

          <div>
            <label style={{ fontWeight: '600' }}>Vacuna aplicada</label>
            <select
              value={vacuna}
              onChange={(e) => setVacuna(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '8px',
                border: '1px solid #ccc',
                marginTop: '8px'
              }}
            >
              <option value="">Seleccionar</option>
              <option value="Rabia">Rabia</option>
              <option value="Triple">Triple</option>
              <option value="Parvovirus">Parvovirus</option>
              <option value="Leptospirosis">Leptospirosis</option>
              <option value="Otra">Otra</option>
            </select>
          </div>

          <div>
            <label style={{ fontWeight: '600' }}>Fecha de aplicación</label>
            <input
              type="date"
              value={fechaAplicacion}
              onChange={(e) => setFechaAplicacion(e.target.value)}
              required
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '8px',
                border: '1px solid #ccc',
                marginTop: '8px'
              }}
            />
          </div>

          <div>
            <label style={{ fontWeight: '600' }}>Dosis aplicada</label>
            <input
              type="text"
              value={dosis}
              onChange={(e) => setDosis(e.target.value)}
              placeholder="Ej: 2 ml"
              required
              style={{
                width: '100%',
                padding: '10px',
                borderRadius: '8px',
                border: '1px solid #ccc',
                marginTop: '8px'
              }}
            />
          </div>

          <div>
            <label style={{ fontWeight: '600' }}>
              <input
                type="checkbox"
                checked={programarSiguiente}
                onChange={(e) => setProgramarSiguiente(e.target.checked)}
                style={{ marginRight: '8px' }}
              />
              Programar próxima dosis
            </label>
          </div>

          {programarSiguiente && (
            <div>
              <label style={{ fontWeight: '600' }}>Fecha próxima dosis</label>
              <input
                type="date"
                value={fechaProxima}
                onChange={(e) => setFechaProxima(e.target.value)}
                style={{
                  width: '100%',
                  padding: '10px',
                  borderRadius: '8px',
                  border: '1px solid #ccc',
                  marginTop: '8px'
                }}
              />
            </div>
          )}

          <button type="submit" style={{
            marginTop: '16px',
            padding: '12px',
            backgroundColor: '#4c84ff',
            color: 'white',
            border: 'none',
            borderRadius: '8px',
            fontWeight: 'bold',
            fontSize: '16px'
          }}>
            Guardar Vacuna
          </button>
        </form>
      </div>
    </div>
  );
}
