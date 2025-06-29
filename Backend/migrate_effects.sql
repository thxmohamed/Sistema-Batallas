-- Script para migrar efectos de velocidad obsoletos
-- Ejecutar este script en la base de datos para actualizar los efectos existentes

-- Actualizar efectos de velocidad propia a efectos de ataque
UPDATE efecto 
SET tipoEfecto = 'SUBIR_ATAQUE_PROPIO', 
    nombre = REPLACE(nombre, 'Velocidad', 'Ataque'),
    descripcion = REPLACE(descripcion, 'velocidad', 'ataque')
WHERE tipoEfecto = 'SUBIR_VELOCIDAD_PROPIO';

-- Actualizar efectos de velocidad rival a efectos de defensa
UPDATE efecto 
SET tipoEfecto = 'BAJAR_DEFENSA_RIVAL',
    nombre = REPLACE(nombre, 'Velocidad', 'Defensa'), 
    descripcion = REPLACE(descripcion, 'velocidad', 'defensa')
WHERE tipoEfecto = 'BAJAR_VELOCIDAD_RIVAL';

-- Verificar los cambios
SELECT id, nombre, tipoEfecto, descripcion, multiplicador 
FROM efecto 
ORDER BY id;
