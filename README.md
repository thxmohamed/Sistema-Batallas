# ⚔️ Sistema de Batallas Pokémon ⚔️

<div align="center">

![Pokémon Battle System](https://img.shields.io/badge/Pokémon-Battle%20System-FF6B35?style=for-the-badge&logo=pokeball&logoColor=white)
![Java](https://img.shields.io/badge/Java-17+-ED8B00?style=for-the-badge&logo=java&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2+-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![React](https://img.shields.io/badge/React-18+-61DAFB?style=for-the-badge&logo=react&logoColor=black)
![MySQL](https://img.shields.io/badge/MySQL-8.0+-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

**¡Un sistema de combate Pokémon completo y moderno con efectos avanzados, sistema de audio inmersivo y batallas estratégicas!**

[🚀 Características](#-características) • [🛠️ Instalación](#%EF%B8%8F-instalación) • [📖 Guía de Uso](#-guía-de-uso) • [🎮 Cómo Jugar](#-cómo-jugar) • [🧪 Desarrollo](#-desarrollo)

</div>

---

## 🌟 Características

### ⚡ Sistema de Combate Avanzado
- **6 tipos de Pokémon**: Fuego 🔥, Agua 💧, Planta 🌿, Eléctrico ⚡, Tierra 🌍, Normal ⭐
- **Sistema de efectividades**: Cada tipo tiene fortalezas y debilidades realistas
- **Combates por turnos**: Estrategia pura donde cada decisión cuenta
- **Equipos de 3 Pokémon**: Forma tu equipo ideal y cubre tus debilidades.

### 🎯 Efectos de Estado
- **🍄 Veneno (Tóxico)**: Daño continuo que se aplica al inicio del turno del equipo afectado
- **⚔️ Modificadores de Ataque**: Potencia o debilita el poder ofensivo
- **🛡️ Modificadores de Defensa**: Controla la resistencia al daño
- **❤️ Recuperación**: Sana a tus Pokémon en momentos críticos
- **📊 Efectos de Equipo**: Los efectos afectan a todo el equipo rival, no solo a un Pokémon

### 🎵 Audio Inmersivo de Primera Clase
- **🎼 Música Contextual**: Diferentes temas para menú y batalla
- **🔇 Control Inteligente**: La música se ajusta automáticamente según el contexto
- **🎚️ Sin Solapamientos**: Sistema robusto que evita múltiples audios simultáneos

### 💎 Interfaz Moderna y Elegante
- **⚡ Animaciones Fluidas**: Transiciones suaves y efectos visuales impactantes
- **📊 Feedback Visual**: Indicadores claros de estado, efectos y cambios
- **🌈 Tabla de Efectividades**: Visualización clara de fortalezas y debilidades

---

## 🛠️ Instalación

### 📋 Prerrequisitos

Antes de sumergirte en el mundo de las batallas Pokémon, asegúrate de tener instalado:

#### ☕ Java Development Kit (JDK) 17+
```bash
# Verificar instalación
java --version
javac --version
```
**💡 Tip**: Descarga desde [Oracle JDK](https://www.oracle.com/java/technologies/downloads/) o [OpenJDK](https://openjdk.org/)

#### 🐬 MySQL 8.0+
```bash
# Verificar instalación
mysql --version
```
**💡 Tip**: Descarga desde [MySQL](https://dev.mysql.com/downloads/mysql/)

#### 🟢 Node.js 18+ y npm
```bash
# Verificar instalación
node --version
npm --version
```
**💡 Tip**: Descarga desde [Node.js](https://nodejs.org/)

#### 🔧 Maven 3.6+
```bash
# Verificar instalación
mvn --version
```
**💡 Tip**: Descarga desde [Apache Maven](https://maven.apache.org/)

---

## 🚀 Configuración del Proyecto

### 🗄️ Paso 1: Configurar la Base de Datos

#### 1.1 Crear la Base de Datos
```sql
-- Conectar a MySQL como root
mysql -u root -p

-- Crear la base de datos
CREATE DATABASE pokemon;

-- Crear usuario para la aplicación (opcional pero recomendado)
CREATE USER 'pokemon_user'@'localhost' IDENTIFIED BY 'pokemon_password';
GRANT ALL PRIVILEGES ON pokemon.* TO 'pokemon_user'@'localhost';
FLUSH PRIVILEGES;

-- Usar la base de datos
USE pokemon;
```

#### 1.2 Configurar las Credenciales del Backend
Edita el archivo `Backend/src/main/resources/application.properties`:

```properties
# Configuración de la base de datos
spring.datasource.url=jdbc:mysql://localhost:3306/pokemon
spring.datasource.username=pokemon_user
spring.datasource.password=pokemon_password
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver

# Configuración de JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQLDialect

# Configuración del servidor
server.port=8090
```

### ⚙️ Paso 2: Instalar Dependencias del Backend

```bash
# Navegar al directorio del backend
cd Backend

# Limpiar y compilar el proyecto
mvn clean compile

# Ejecutar tests (opcional)
mvn test

# Instalar dependencias
mvn install
```

### 🎨 Paso 3: Instalar Dependencias del Frontend

```bash
# Navegar al directorio del frontend
cd frontend

# Instalar todas las dependencias
npm install

# Verificar que no hay vulnerabilidades
npm audit fix
```

---

## 🎮 Paso 4: Inicializar Datos del Juego

### 4.1 Ejecutar el Script de Datos
Una vez que el backend esté corriendo y haya creado las tablas automáticamente:

```bash
# Conectar a MySQL
mysql -u pokemon_user -p pokemon

# Ejecutar el script de datos desde MySQL
source /ruta/completa/al/proyecto/loadData.sql;

# O alternativamente desde la línea de comandos
mysql -u pokemon_user -p pokemon < loadData.sql
```

### 4.2 Verificar la Carga de Datos
```sql
-- Verificar que los datos se cargaron correctamente
SELECT COUNT(*) as total_ataques FROM ataque;
SELECT COUNT(*) as total_efectos FROM efecto;

-- Ver algunos ejemplos
SELECT * FROM ataque LIMIT 5;
SELECT * FROM efecto LIMIT 5;
```

**🎯 Datos incluidos:**
- **30 Ataques únicos** distribuidos entre los 6 tipos
- **6 Efectos especiales** para estrategias avanzadas
- **Balanceado y probado** para gameplay equilibrado

---

## 🏃‍♂️ Ejecutar la Aplicación

### 🔥 Paso 1: Iniciar el Backend

```bash
# Navegar al directorio del backend
cd Backend

# Opción 1: Usar Maven
mvn spring-boot:run

# Opción 2: Usar Java directamente (después de compilar)
mvn package
java -jar target/Pokemon-0.0.1-SNAPSHOT.jar

# Opción 3: Usar tu IDE favorito, por ejemplo, IntelIJ Idea
# Ejecutar la clase PokemonApplication.java
```

**✅ Backend corriendo exitosamente cuando veas:**
```
🚀 Tomcat started on port(s): 8090 (http)
⚡ Started PokemonApplication in X.XXX seconds
```

**📡 API disponible en:** `http://localhost:8090`

### 🎨 Paso 2: Iniciar el Frontend

```bash
# En una nueva terminal, navegar al frontend
cd frontend

# Iniciar el servidor de desarrollo
npm run dev

# O alternativamente
npm start
```

**✅ Frontend corriendo exitosamente cuando veas:**
```
🎯 Local:   http://localhost:5173/
🌐 Network: http://192.168.x.x:5173/
```

### 🎉 ¡Listo para Batallar!

Abre tu navegador y visita: **http://localhost:5173**

---

## 📖 Guía de Uso

### 🎮 Cómo Jugar

#### 1️⃣ **Crear tu Primer Pokémon**
- Ve a "**Crear Pokémon**" 
- Elige un nombre
- Selecciona un tipo
- Ajusta las estadísticas base (vida, ataque, defensa)
- Escoge los dos movimientos y el efecto especial.
- ¡Tu Pokémon está listo!

#### 2️⃣ **Formar tu Equipo**
- Ve a "**Crear Entrenador**"
- Elige un nombre de entrenador
- Selecciona exactamente **3 Pokémon** para tu equipo
- Considera la sinergia entre tipos

#### 3️⃣ **¡A Batallar!**
- Ve a "**Batalla**"
- Selecciona dos entrenadores para el combate
- **¡Que comience la batalla épica!**

### ⚔️ Mecánicas de Combate

#### 🎯 Sistema de Turnos
1. **Selección de Acción**: Elige entre atacar o usar un efecto según te convenga
2. **Aplicación de Efectos**: Los efectos de estado se aplican automáticamente
3. **Resolución de Daño**: Se calcula considerando tipos y estadísticas
4. **Cambio de Turno**: El turno pasa al siguiente entrenador

#### 🔥 Tabla de Efectividades
| Tipo Atacante | Súper Efectivo Contra | Poco Efectivo Contra |
|---------------|----------------------|---------------------|
| 🔥 **Fuego**  | 🌿 Planta           | 💧 Agua             |
| 💧 **Agua**   | 🔥 Fuego            | 🌿 Planta           |
| 🌿 **Planta** | 💧 Agua, 🌍 Tierra  | 🔥 Fuego            |
| ⚡ **Eléctrico** | 💧 Agua           | 🌍 Tierra           |
| 🌍 **Tierra** | ⚡ Eléctrico        | 🌿 Planta           |
| ⭐ **Normal**  | *Neutral contra todos* | *Neutral contra todos* |

Importante: cada tipo es resistente a sí mismo.

#### 🎭 Efectos de Estado
- **🍄 Tóxico**: Reduce 10% de vida al inicio del turno del equipo afectado durante 4 turnos
- **⚔️ Danza Espada**: Duplica el ataque del usuario
- **🛡️ Rizo Defensa**: Duplica la defensa del usuario
- **👁️ Ojos Llorosos**: Reduce el ataque del equipo rival en 20%
- **🔱 Látigo**: Reduce la defensa del equipo rival en 20%
- **❤️ Recuperación**: Restaura 50% de la vida máxima

---

## 🧪 Desarrollo y Personalización

### 🏗️ Arquitectura del Sistema

```
Sistema-Batallas/
├── 🎮 Backend/                 # API REST con Spring Boot
│   ├── src/main/java/
│   │   └── com/example/Pokemon/
│   │       ├── 🎯 Controllers/  # Endpoints REST
│   │       ├── 📦 Entities/     # Modelos de datos
│   │       ├── 🔧 Services/     # Lógica de negocio
│   │       ├── 🗄️ Repositories/ # Acceso a datos
│   │       └── 📋 DTO/          # Objetos de transferencia
│   └── src/main/resources/
│       └── application.properties
├── 🎨 frontend/                # Interfaz con React + Vite
│   ├── src/
│   │   ├── 🧩 components/      # Componentes React
│   │   ├── 🔧 services/        # Servicios HTTP
│   │   ├── 🎵 contexts/        # Contextos React
│   │   └── 🎨 assets/          # Recursos estáticos
│   ├── package.json
│   └── vite.config.js
├── 📊 loadData.sql             # Datos iniciales
└── 📖 README.md               # Esta guía épica
```

### 🔧 Comandos de Desarrollo Útiles

#### Backend
```bash
# Compilar sin ejecutar tests
mvn compile -DskipTests

# Ejecutar solo tests
mvn test

# Generar JAR para producción
mvn package -DskipTests

# Limpiar y reinstalar
mvn clean install
```

#### Frontend
```bash
# Modo desarrollo con hot reload
npm run dev

# Construir para producción
npm run build

# Vista previa de la build de producción
npm run preview

# Linter y formato de código
npm run lint
npm run format
```

### 🎮 Extender el Sistema

#### Personalizar Audio
- Coloca archivos MP3 en `frontend/src/assets/audio/`
- Reemplaza los archivos existentes colocando el mismo nombre a los nuevos

---

## 🚨 Solución de Problemas

### ❌ Problemas Comunes

#### "Port 8090 already in use"
```bash
# Encontrar el proceso usando el puerto
netstat -ano | findstr :8090

# Terminar el proceso (Windows)
taskkill /PID <numero_proceso> /F
```

#### "Access denied for user"
```sql
-- Verificar usuarios de MySQL
SELECT user, host FROM mysql.user;

-- Recrear usuario si es necesario
DROP USER 'pokemon_user'@'localhost';
CREATE USER 'pokemon_user'@'localhost' IDENTIFIED BY 'pokemon_password';
GRANT ALL PRIVILEGES ON pokemon.* TO 'pokemon_user'@'localhost';
```

#### "Cannot connect to database"
1. Verificar que MySQL esté corriendo
2. Comprobar las credenciales en `application.properties`
3. Asegurar que la base de datos existe

#### Frontend no carga
```bash
# Limpiar caché de npm
npm cache clean --force

# Reinstalar dependencias
rm -rf node_modules package-lock.json
npm install

# Verificar puertos disponibles
netstat -ano | findstr :5173
```

### 🔍 Logs de Debug

#### Backend
```bash
# Ver logs en tiempo real
tail -f logs/spring.log

# Logs con más detalle
java -jar target/Pokemon-0.0.1-SNAPSHOT.jar --debug
```

#### Frontend
- Abre las **DevTools** del navegador (F12)
- Revisa la pestaña **Console** para errores JavaScript
- Revisa la pestaña **Network** para errores de API

---

### 🚀 Próximas Funcionalidades Planeadas
- [ ] **18 Tipos Oficiales de Pokémon**
- [ ] **Ampliar los equipos a 6 Pokémon en lugar de 3**
- [ ] **Sistema de PP a los ataques**

---

## 🎯 Créditos

Desarrollado con ❤️ por un entrenador Pokémon apasionado

**Tecnologías utilizadas:**
- ☕ **Spring Boot** - Backend robusto y escalable
- ⚛️ **React + Vite** - Frontend moderno y rápido
- 🐬 **MySQL** - Base de datos confiable
- 🎨 **CSS3** - Estilos modernos y responsive
- 🎵 **Web Audio API** - Sistema de audio inmersivo

---

<div align="center">

**¿Listo para convertirte en el mejor entrenador Pokémon?**

[⭐ Da una estrella al proyecto](https://github.com/thxmohamed/Sistema-Batallas) • [🐛 Reportar un bug](https://github.com/thxmohamed/Sistema-Batallas/issues) • [💡 Sugerir mejora](https://github.com/thxmohamed/Sistema-Batallas/issues)

---

*"¡Hazte con todos!"* - Mohamed Al-Marzuk

</div>