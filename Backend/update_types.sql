-- Script para actualizar los tipos de Pokémon y ataques a los 18 tipos completos

-- Actualizar la tabla pokemon para incluir todos los tipos
ALTER TABLE pokemon MODIFY COLUMN tipo_pokemon ENUM(
    'NORMAL', 'FUEGO', 'AGUA', 'ELECTRICO', 'PLANTA', 'HIELO',
    'LUCHA', 'VENENO', 'TIERRA', 'VOLADOR', 'PSIQUICO', 'BICHO',
    'ROCA', 'FANTASMA', 'DRAGON', 'SINIESTRO', 'ACERO', 'HADA'
) NOT NULL;

-- Actualizar la tabla ataque para incluir todos los tipos
ALTER TABLE ataque MODIFY COLUMN tipo_ataque ENUM(
    'NORMAL', 'FUEGO', 'AGUA', 'ELECTRICO', 'PLANTA', 'HIELO',
    'LUCHA', 'VENENO', 'TIERRA', 'VOLADOR', 'PSIQUICO', 'BICHO',
    'ROCA', 'FANTASMA', 'DRAGON', 'SINIESTRO', 'ACERO', 'HADA'
) NOT NULL;

-- Verificar los cambios
DESCRIBE pokemon;
DESCRIBE ataque;
