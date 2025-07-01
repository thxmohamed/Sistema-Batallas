#!/bin/bash

# Script de Prueba para Verificar Efectos de Ataque usando curl
# Requiere que el backend esté ejecutándose en localhost:8080

BASE_URL="http://localhost:8080"

echo "=== PRUEBA DE EFECTOS ACUMULATIVOS ==="

# 1. Obtener un Pokémon específico para la prueba
echo -e "\n1. Obteniendo Pokémon para prueba..."
POKEMON=$(curl -s "$BASE_URL/pokemon/1")
echo "Pokemon obtenido: $(echo $POKEMON | jq -r '.nombre')"
echo "  - ID: $(echo $POKEMON | jq -r '.id')"
echo "  - Ataque base: $(echo $POKEMON | jq -r '.ataque')"
echo "  - Ataque modificado: $(echo $POKEMON | jq -r '.ataqueModificado')"

# 2. Obtener el efecto "Danza Espada"
echo -e "\n2. Obteniendo efecto Danza Espada..."
EFECTOS=$(curl -s "$BASE_URL/efecto")
DANZA_ESPADA=$(echo $EFECTOS | jq '.[] | select(.nombre == "Danza Espada")')
echo "Efecto encontrado: $(echo $DANZA_ESPADA | jq -r '.nombre')"
echo "  - ID: $(echo $DANZA_ESPADA | jq -r '.id')"
echo "  - Multiplicador: $(echo $DANZA_ESPADA | jq -r '.multiplicador')"

# 3. Crear payload para aplicar efecto
RIVAL_DUMMY='{"id": 999, "nombre": "Rival Test", "tipoPokemon": "NORMAL", "vida": 100, "ataque": 50, "defensa": 50}'

# 4. Primera aplicación de Danza Espada
echo -e "\n3. Aplicando Danza Espada (1ra vez)..."
PAYLOAD=$(echo "{\"usuario\": $POKEMON, \"rival\": $RIVAL_DUMMY, \"efecto\": $DANZA_ESPADA}")

POKEMON_ACTUALIZADO=$(curl -s -X POST "$BASE_URL/pokemon/aplicar-efecto" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD")

if [ $? -eq 0 ]; then
    echo "Primera aplicación exitosa:"
    echo "  - Ataque antes: $(echo $POKEMON | jq -r '.ataque')"
    echo "  - Ataque después: $(echo $POKEMON_ACTUALIZADO | jq -r '.ataqueModificado')"
else
    echo "Error en primera aplicación"
    exit 1
fi

# 5. Segunda aplicación (prueba de acumulación)
echo -e "\n4. Aplicando Danza Espada (2da vez)..."
PAYLOAD2=$(echo "{\"usuario\": $POKEMON_ACTUALIZADO, \"rival\": $RIVAL_DUMMY, \"efecto\": $DANZA_ESPADA}")

POKEMON_ACTUALIZADO2=$(curl -s -X POST "$BASE_URL/pokemon/aplicar-efecto" \
  -H "Content-Type: application/json" \
  -d "$PAYLOAD2")

if [ $? -eq 0 ]; then
    echo "Segunda aplicación exitosa:"
    echo "  - Ataque después de 1ra aplicación: $(echo $POKEMON_ACTUALIZADO | jq -r '.ataqueModificado')"
    echo "  - Ataque después de 2da aplicación: $(echo $POKEMON_ACTUALIZADO2 | jq -r '.ataqueModificado')"
    
    # Verificar que el ataque haya aumentado
    ATAQUE_1=$(echo $POKEMON_ACTUALIZADO | jq -r '.ataqueModificado')
    ATAQUE_2=$(echo $POKEMON_ACTUALIZADO2 | jq -r '.ataqueModificado')
    
    if [ "$ATAQUE_2" -gt "$ATAQUE_1" ]; then
        echo "  ✓ ACUMULACIÓN DETECTADA: $ATAQUE_1 -> $ATAQUE_2"
    else
        echo "  ✗ NO HAY ACUMULACIÓN: $ATAQUE_1 -> $ATAQUE_2"
    fi
else
    echo "Error en segunda aplicación"
fi

echo -e "\n=== RESUMEN ==="
echo "Revisa los valores mostrados para verificar que el ataque aumenta consistentemente."
echo "También revisa los logs del backend para ver los detalles de los cálculos."
