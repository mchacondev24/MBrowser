# MBrowser 💧⚡
> **Navegador Android de Alto Rendimiento: Modo Super Hiper Veloz, GameBoost, DevTools con Inspector de Errores y Estética Glassmorphism Aero Aqua.**

---

## 🌟 Descripción General

**MBrowser** es un navegador móvil para Android diseñado bajo la arquitectura de alto rendimiento de Mozilla / Waterfox (GeckoView / WebKit Engine architecture) y optimizado para ofrecer una experiencia ultra veloz, privada, personalizable e inmersiva.

Integra directamente el portafolio del desarrollador ([ingemaxwellchacon.com](https://ingemaxwellchacon.com)), herramientas para programadores web, y un módulo especializado para **Cloud Gaming** y juegos de baja latencia.

---

## 🚀 Novedades y Características Principales

### 1. ⚡ Modo Super Hiper Veloz (Core Performance & Privacy)
- **Bloqueo Nativo de Rastreadores y Publicidad:** Intercepta peticiones de red a nivel de motor contra más de 40 dominios de telemetría, analíticas intrusivas, píxeles de seguimiento y scripts publicitarios pesados.
- **Ahorro Extremo de Ancho de Banda y RAM:** Omisión de scripts no esenciales antes de que consuman CPU.
- **Aceleración por Hardware:** Renderizado GPU activo para máxima fluidez a 60/120 FPS.

### 2. 🎮 Modo Gaming & Cloud Play ("MBrowser GameBoost")
- **Reducción de Latencia y Ping:** Priorización de tráfico WebSockets y streams de datos en tiempo real (esencial para juegos multijugador y Cloud Gaming).
- **GPU Turbo & Forzado de WebGL/WebGPU:** Maximiza la aceleración gráfica evitando tirones en juegos HTML5, Canvas y emuladores web.
- **Pantalla Completa Inmersiva:** Oculta automáticamente todas las barras de sistema y navegación para una experiencia total sin distracciones.
- **Lanzador Rápido:** Accesos directos a plataformas como Xbox Cloud Gaming, GeForce Now y Poki.

### 3. 🛠️ Modo Desarrollador, Inspector de Errores & Limpieza de Caché
- **Purga Instantánea de Caché:** Botón dedicado para borrar la caché de disco, memoria, cookies y WebStorage al instante con un solo toque, evitando el problema de no ver reflejados los cambios al programar.
- **Hard Reload (Bypass Cache):** Recarga forzada saltándose la caché HTTP.
- **Inspector de Consola en Tiempo Real:** Captura y categoriza errores de JavaScript (`console.error`, excepciones no capturadas y promesas rechazadas).
- **Copiar Errores en 1 Toque:** Botón para copiar inmediatamente todos los logs y mensajes de error al portapapeles con número de línea y archivo de origen.

### 4. 💎 Estética Retro-Futurista (Windows Vista Aero & Mac Aqua Glassmorphism)
- **Barras de Navegación Traslúcidas:** Efectos de cristal con reflejos especulares, bordes luminosos y gradientes Aqua Blue / Aero Vista.
- **Botonería de Gel Estilizada:** Botones con brillo superior y micro-animaciones fluidas.
- **Paleta de Colores Curada:** Contrastes profundos en azul noche, cian brillante, esmeralda gaming y acentos neón.

### 5. 🌐 IngeHub & Página de Inicio Integrada
- **Página de Inicio por Defecto:** Configurada hacia `https://ingemaxwellchacon.com`.
- **IngeHub:** Panel deslizante lateral con acceso al portafolio oficial del creador, utilidades de desarrollo y estadísticas de rastreadores bloqueados en tiempo real.

---

## 🏗️ Arquitectura del Proyecto

```
MBrowser/
├── app/
│   ├── src/
│   │   ├── main/
│   │   │   ├── java/com/maxwell/mbrowser/
│   │   │   │   ├── MainActivity.kt               # Controlador principal de navegación
│   │   │   │   ├── engine/
│   │   │   │   │   └── AdTrackerBlocker.kt       # Motor de intercepción y bloqueo de trackers
│   │   │   │   ├── devtools/
│   │   │   │   │   ├── DevToolsManager.kt        # Gestión de caché, consola y copiado de errores
│   │   │   │   │   └── ConsoleLogItem.kt         # Modelo de datos de logs
│   │   │   │   ├── gameboost/
│   │   │   │   │   └── GameBoostManager.kt       # Aceleración GPU y pantalla completa inmersiva
│   │   │   │   ├── hub/
│   │   │   │   │   └── IngeHubManager.kt         # Panel de portafolio y accesos directos
│   │   │   │   └── dialogs/
│   │   │   │       └── AboutDialog.kt            # Diálogo y licencias MPL 2.0
│   │   │   ├── res/
│   │   │   │   ├── layout/                       # Diseños XML (Aero & Aqua Glass UI)
│   │   │   │   ├── drawable/                     # Drawables de cristal, gradientes e iconos vectoriales
│   │   │   │   └── values/                       # Colores, temas y cadenas
│   │   │   └── AndroidManifest.xml               # Permisos y configuración de aceleración por hardware
│   │   └── build.gradle.kts                      # Configuración de dependencias de la app
├── gradle/wrapper/                               # Gradle Wrapper (8.11.1)
├── build.gradle.kts                              # Configuración raíz de plugins
├── settings.gradle.kts                           # Repositorios y módulos
└── README.md                                     # Documentación técnica
```

---

## ⚙️ Requisitos de Compilación

- **Java Development Kit (JDK):** JDK 17 o superior
- **Android SDK:** API Level 35 (Android 15)
- **Min SDK:** API Level 26 (Android 8.0 Oreo)
- **Gradle:** 8.11.1

---

## 📦 Instrucciones para Compilar el APK

Para compilar el archivo APK en modo Debug:
```bash
./gradlew assembleDebug
```
El archivo generado se ubicará en:
`app/build/outputs/apk/debug/app-debug.apk`

Para compilar el APK de Release:
```bash
./gradlew assembleRelease
```

---

## 📜 Licencia y Reconocimientos

Este proyecto está licenciado bajo la **Mozilla Public License Version 2.0 (MPL-2.0)**.
- Reconocimiento a Mozilla y el proyecto Waterfox (`BrowserWorks/waterfox`) por la base de arquitectura de navegación móvil y GeckoView.
- Desarrollado por **Maxwell Chacón** ([ingemaxwellchacon.com](https://ingemaxwellchacon.com)).
