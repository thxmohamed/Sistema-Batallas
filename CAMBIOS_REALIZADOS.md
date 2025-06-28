# Cambios Realizados en el Sistema de Batallas Pokémon

## 1. Eliminación del Campo Velocidad

### Backend:
- **Pokemon.java**: Eliminado el campo `velocidad` y sus getters/setters
- **PokemonController.java**: Eliminado el parámetro `velocidad` del endpoint de creación
- **PokemonService.java**: Eliminados los casos de efecto relacionados con velocidad (`SUBIR_VELOCIDAD_PROPIO`, `BAJAR_VELOCIDAD_RIVAL`)
- **Efecto.java**: Eliminados los tipos de efecto relacionados con velocidad

### Frontend:
- **CrearPokemon.jsx**: Eliminado el campo velocidad del formulario
- **CrearEntrenador.jsx**: Eliminada la visualización de velocidad en los detalles

## 2. Sistema de Turnos por Equipos

### Backend:
- **BatallaService.java**: 
  - Cambiado `esPrimeroAtacante1()` por `esPrimeroEntrenador1(int turno)`
  - Los turnos impares (1, 3, 5...) corresponden al Entrenador 1
  - Los turnos pares (2, 4, 6...) corresponden al Entrenador 2

### Frontend:
- **BattleView.jsx**: Agregado indicador visual del turno actual

## 3. Implementación de Efectos en el Combate

### Backend:
- **BatallaDTO.java**: 
  - Habilitados los campos `efectoE1` y `efectoE2`
  - Agregados campos `usarEfectoE1` y `usarEfectoE2` para controlar el tipo de acción
- **BatallaService.java**:
  - Lógica actualizada para soportar tanto ataques como efectos
  - Método `efectoRequiereRival()` para determinar el objetivo del efecto
  - Los efectos que afectan al rival: DANO_CONTINUO, BAJAR_ATAQUE_RIVAL, BAJAR_DEFENSA_RIVAL
  - Los efectos que afectan al usuario: SUBIR_VIDA, SUBIR_ATAQUE_PROPIO, SUBIR_DEFENSA_PROPIO

### Frontend:
- **BattleView.jsx**:
  - Agregados estados para efectos de cada entrenador
  - Checkboxes para alternar entre usar ataques o efectos
  - Interfaz actualizada para mostrar opciones de ataques y efectos
  - DTO actualizado para enviar información de efectos al backend

## 4. Funcionalidades del Sistema Actualizado

### Mecánicas de Turnos:
- **Turno 1**: Entrenador 1 ataca primero, luego Entrenador 2 (si su Pokémon no fue debilitado)
- **Turno 2**: Entrenador 2 ataca primero, luego Entrenador 1 (si su Pokémon no fue debilitado)
- Y así sucesivamente alternando quién ataca primero

### Opciones de Combate:
En cada turno, cada jugador puede elegir:
1. **Usar Ataque**: Seleccionar uno de los dos ataques del Pokémon
2. **Usar Efecto**: Activar el efecto especial del Pokémon

### Tipos de Efectos Disponibles:
- **DANO_CONTINUO**: Causa daño porcentual al rival
- **SUBIR_VIDA**: Restaura vida del usuario
- **SUBIR_ATAQUE_PROPIO**: Aumenta el ataque del usuario
- **SUBIR_DEFENSA_PROPIO**: Aumenta la defensa del usuario
- **BAJAR_ATAQUE_RIVAL**: Reduce el ataque del rival
- **BAJAR_DEFENSA_RIVAL**: Reduce la defensa del rival

## 5. Interfaz de Usuario

### Indicadores Visuales:
- Información del turno actual y qué entrenador debe actuar
- Checkboxes para alternar entre ataques y efectos
- Botones de colores diferentes para ataques (azul) y efectos (coral)
- Vida actual vs vida máxima de cada Pokémon

### Flujo de Combate:
1. Cada entrenador selecciona un Pokémon atacante
2. Decide si usar ataque o efecto (checkbox)
3. Selecciona el Pokémon objetivo (para ataques)
4. Presiona "Atacar" para ejecutar el turno
5. El sistema alterna automáticamente el orden de ataque en el siguiente turno

El sistema ahora es más estratégico ya que los jugadores deben decidir cuándo usar sus efectos especiales versus ataques directos, y el orden de turnos fijo permite planificación táctica.
