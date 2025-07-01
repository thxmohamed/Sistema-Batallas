# Script de Prueba para Verificar Efectos de Ataque

# Este script prueba la funcionalidad de efectos acumulativos usando curl
# Requiere que el backend esté ejecutándose en localhost:8080

# Configuración de test
$baseUrl = "http://localhost:8080"
$headers = @{"Content-Type" = "application/json"}

Write-Host "=== PRUEBA DE EFECTOS ACUMULATIVOS ===" -ForegroundColor Green

# 1. Obtener un Pokémon específico para la prueba
Write-Host "`n1. Obteniendo Pokémon para prueba..." -ForegroundColor Yellow
try {
    $pokemon = Invoke-RestMethod -Uri "$baseUrl/pokemon/1" -Method GET
    Write-Host "Pokemon obtenido: $($pokemon.nombre)" -ForegroundColor Cyan
    Write-Host "  - ID: $($pokemon.id)"
    Write-Host "  - Ataque base: $($pokemon.ataque)"
    Write-Host "  - Ataque modificado: $($pokemon.ataqueModificado)"
    Write-Host "  - Defensa base: $($pokemon.defensa)"
    Write-Host "  - Defensa modificada: $($pokemon.defensaModificada)"
}
catch {
    Write-Host "Error obteniendo Pokémon: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 2. Obtener el efecto "Danza Espada" (SUBIR_ATAQUE_PROPIO)
Write-Host "`n2. Obteniendo efecto Danza Espada..." -ForegroundColor Yellow
try {
    $efectos = Invoke-RestMethod -Uri "$baseUrl/efecto" -Method GET
    $danzaEspada = $efectos | Where-Object { $_.nombre -eq "Danza Espada" }
    
    if ($danzaEspada) {
        Write-Host "Efecto encontrado: $($danzaEspada.nombre)" -ForegroundColor Cyan
        Write-Host "  - ID: $($danzaEspada.id)"
        Write-Host "  - Tipo: $($danzaEspada.tipoEfecto)"
        Write-Host "  - Multiplicador: $($danzaEspada.multiplicador)"
    } else {
        Write-Host "Efecto Danza Espada no encontrado" -ForegroundColor Red
        exit 1
    }
}
catch {
    Write-Host "Error obteniendo efectos: $($_.Exception.Message)" -ForegroundColor Red
    exit 1
}

# 3. Crear un Pokémon rival (dummy) para la prueba
$rivalDummy = @{
    id = 999
    nombre = "Rival Test"
    tipoPokemon = "NORMAL"
    vida = 100
    ataque = 50
    defensa = 50
    vidaBase = 100
    ataqueBase = 50
    defensaBase = 50
    ataqueModificado = 50
    defensaModificada = 50
}

# 4. Aplicar Danza Espada por primera vez
Write-Host "`n3. Aplicando Danza Espada (1ra vez)..." -ForegroundColor Yellow
$aplicarEfectoRequest = @{
    usuario = $pokemon
    rival = $rivalDummy
    efecto = $danzaEspada
} | ConvertTo-Json -Depth 10

try {
    $pokemonActualizado = Invoke-RestMethod -Uri "$baseUrl/pokemon/aplicar-efecto" -Method POST -Body $aplicarEfectoRequest -Headers $headers
    Write-Host "Primera aplicación exitosa:" -ForegroundColor Cyan
    Write-Host "  - Ataque antes: $($pokemon.ataque)"
    Write-Host "  - Ataque después: $($pokemonActualizado.ataque)"
    Write-Host "  - Ataque efectivo: $($pokemonActualizado.ataqueModificado)"
    
    # Actualizar pokemon para siguiente prueba
    $pokemon = $pokemonActualizado
}
catch {
    Write-Host "Error aplicando efecto (1ra vez): $($_.Exception.Message)" -ForegroundColor Red
    Write-Host "Response: $($_.Exception.Response)" -ForegroundColor Red
}

# 5. Aplicar Danza Espada por segunda vez (prueba de acumulación)
Write-Host "`n4. Aplicando Danza Espada (2da vez) para probar acumulación..." -ForegroundColor Yellow
$aplicarEfectoRequest2 = @{
    usuario = $pokemon
    rival = $rivalDummy
    efecto = $danzaEspada
} | ConvertTo-Json -Depth 10

try {
    $pokemonActualizado2 = Invoke-RestMethod -Uri "$baseUrl/pokemon/aplicar-efecto" -Method POST -Body $aplicarEfectoRequest2 -Headers $headers
    Write-Host "Segunda aplicación exitosa:" -ForegroundColor Cyan
    Write-Host "  - Ataque después de 1ra aplicación: $($pokemon.ataqueModificado)"
    Write-Host "  - Ataque después de 2da aplicación: $($pokemonActualizado2.ataqueModificado)"
    
    # Verificar acumulación
    $incrementoEsperado = [math]::Floor($pokemonActualizado.ataqueBase * ($danzaEspada.multiplicador - 1))
    $ataqueEsperado = $pokemon.ataqueModificado + $incrementoEsperado
    
    Write-Host "`n=== VERIFICACIÓN DE ACUMULACIÓN ===" -ForegroundColor Green
    Write-Host "  - Ataque base original: $($pokemonActualizado.ataqueBase)"
    Write-Host "  - Incremento por aplicación: $incrementoEsperado"
    Write-Host "  - Ataque esperado tras 2da aplicación: $ataqueEsperado"
    Write-Host "  - Ataque actual: $($pokemonActualizado2.ataqueModificado)"
    
    if ($pokemonActualizado2.ataqueModificado -eq $ataqueEsperado) {
        Write-Host "  ✓ ACUMULACIÓN FUNCIONA CORRECTAMENTE" -ForegroundColor Green
    } else {
        Write-Host "  ✗ PROBLEMA CON LA ACUMULACIÓN" -ForegroundColor Red
    }
}
catch {
    Write-Host "Error aplicando efecto (2da vez): $($_.Exception.Message)" -ForegroundColor Red
}

# 6. Tercera aplicación para confirmar patrón
Write-Host "`n5. Aplicando Danza Espada (3ra vez) para confirmar patrón..." -ForegroundColor Yellow
$aplicarEfectoRequest3 = @{
    usuario = $pokemonActualizado2
    rival = $rivalDummy
    efecto = $danzaEspada
} | ConvertTo-Json -Depth 10

try {
    $pokemonActualizado3 = Invoke-RestMethod -Uri "$baseUrl/pokemon/aplicar-efecto" -Method POST -Body $aplicarEfectoRequest3 -Headers $headers
    Write-Host "Tercera aplicación exitosa:" -ForegroundColor Cyan
    Write-Host "  - Ataque después de 2da aplicación: $($pokemonActualizado2.ataqueModificado)"
    Write-Host "  - Ataque después de 3ra aplicación: $($pokemonActualizado3.ataqueModificado)"
    
    # Verificar patrón de acumulación
    $incrementoEsperado = [math]::Floor($pokemonActualizado2.ataqueBase * ($danzaEspada.multiplicador - 1))
    $ataqueEsperado = $pokemonActualizado2.ataqueModificado + $incrementoEsperado
    
    Write-Host "`n=== VERIFICACIÓN DE PATRÓN ===" -ForegroundColor Green
    Write-Host "  - Incremento esperado por aplicación: $incrementoEsperado"
    Write-Host "  - Ataque esperado tras 3ra aplicación: $ataqueEsperado"
    Write-Host "  - Ataque actual: $($pokemonActualizado3.ataqueModificado)"
    
    if ($pokemonActualizado3.ataqueModificado -eq $ataqueEsperado) {
        Write-Host "  ✓ PATRÓN DE ACUMULACIÓN CONSISTENTE" -ForegroundColor Green
    } else {
        Write-Host "  ✗ INCONSISTENCIA EN EL PATRÓN" -ForegroundColor Red
    }
}
catch {
    Write-Host "Error aplicando efecto (3ra vez): $($_.Exception.Message)" -ForegroundColor Red
}

Write-Host "`n=== RESUMEN DE LA PRUEBA ===" -ForegroundColor Green
Write-Host "Si ves mensajes de éxito (✓), la lógica de backend está funcionando correctamente." -ForegroundColor Cyan
Write-Host "Si hay errores (✗), revisa los logs del backend para más detalles." -ForegroundColor Cyan
Write-Host "Los valores mostrados deberían incrementar consistentemente en cada aplicación." -ForegroundColor Cyan
