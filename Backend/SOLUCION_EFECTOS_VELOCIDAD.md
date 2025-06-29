# 🔧 Solución al Error de Efectos de Velocidad

## ❌ Problema
```
No enum constant com.example.Pokemon.Entities.Efecto.tipoEfecto.SUBIR_VELOCIDAD_PROPIO
```

Este error ocurre porque existen efectos en la base de datos que referencian tipos de efectos de velocidad que fueron eliminados del sistema.

## ✅ Solución Implementada

### 1. **Compatibilidad Temporal**
- Se agregaron los tipos legacy al enum (`SUBIR_VELOCIDAD_PROPIO`, `BAJAR_VELOCIDAD_RIVAL`)
- El sistema ahora puede leer estos efectos sin errores

### 2. **Migración Automática**
- **Endpoint**: `POST /efecto/migrate-velocity`
- **Función**: Convierte automáticamente:
  - `SUBIR_VELOCIDAD_PROPIO` → `SUBIR_ATAQUE_PROPIO`
  - `BAJAR_VELOCIDAD_RIVAL` → `BAJAR_DEFENSA_RIVAL`

### 3. **Migración Manual (SQL)**
Si prefieres ejecutar la migración directamente en la base de datos, usa el archivo `migrate_effects.sql`:

```sql
-- Migrar efectos de velocidad propia a ataque
UPDATE efecto 
SET tipoEfecto = 'SUBIR_ATAQUE_PROPIO', 
    nombre = REPLACE(nombre, 'Velocidad', 'Ataque'),
    descripcion = REPLACE(descripcion, 'velocidad', 'ataque')
WHERE tipoEfecto = 'SUBIR_VELOCIDAD_PROPIO';

-- Migrar efectos de velocidad rival a defensa
UPDATE efecto 
SET tipoEfecto = 'BAJAR_DEFENSA_RIVAL',
    nombre = REPLACE(nombre, 'Velocidad', 'Defensa'), 
    descripcion = REPLACE(descripcion, 'velocidad', 'defensa')
WHERE tipoEfecto = 'BAJAR_VELOCIDAD_RIVAL';
```

## 🚀 Pasos para Resolver

### Opción A: Migración Automática (Recomendada)
1. Iniciar el backend
2. Hacer una petición POST a: `http://localhost:8090/efecto/migrate-velocity`
3. Los efectos se migrarán automáticamente

### Opción B: Migración Manual
1. Conectar a la base de datos
2. Ejecutar el script `migrate_effects.sql`
3. Reiniciar el backend

## 📝 Notas Importantes

- **Los efectos de velocidad ahora equivalen a:**
  - Velocidad propia → Ataque (más agresividad)
  - Velocidad rival → Defensa (menos capacidad de esquivar)

- **Compatibilidad**: El sistema seguirá funcionando con efectos legacy hasta que se migren

- **Futuro**: Una vez migrados todos los efectos, se pueden eliminar los tipos legacy del enum

## ✅ Resultado Final
- ✅ Backend compila sin errores
- ✅ Sistema compatible con efectos existentes
- ✅ Migración automática disponible
- ✅ Efectos funcionando correctamente en batalla
