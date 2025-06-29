-- Script para limpiar y recargar la base de datos con nuevos ataques incluyendo tipo NORMAL
-- IMPORTANTE: Este script borrará todos los datos existentes

-- Limpiar datos existentes
DELETE FROM entrenador WHERE id > 0;
DELETE FROM ataque WHERE id > 0;
DELETE FROM efecto WHERE id > 0;

-- Reiniciar contadores de ID
ALTER TABLE entrenador AUTO_INCREMENT = 1;
ALTER TABLE ataque AUTO_INCREMENT = 1;
ALTER TABLE efecto AUTO_INCREMENT = 1;

-- Cargar nuevos datos desde loadData.sql
-- Los ataques ahora incluyen tipo NORMAL con efectividad neutra (x1) contra todos los tipos
