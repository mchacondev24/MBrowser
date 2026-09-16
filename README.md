# MBrowser 💧⚡
> **Super-App Todo-en-Uno para Android:** Navegador Web de Rendimiento Extremo (GeckoView / Waterfox), Suite Ofimática Completa con Gemini AI (OfficeFreeToAndroid), Servidor Web Local & Base de Datos (ApacheMysqlAndroid_Server), Google Drive Cloud Sync y Ecosistema `ingemaxwellchacon.com`.

---

## 🌟 Visión General

**MBrowser** es una plataforma móvil avanzada que revoluciona el concepto de navegador web en Android. No solo ofrece una experiencia de navegación ultrarrápida, privada y libre de rastreadores, sino que se convierte en un centro de productividad y desarrollo completo: permite redactar y editar documentos ofimáticos con asistencia de inteligencia artificial (Gemini AI), ejecutar un servidor web completo (Apache + PHP) con bases de datos relacionales (MySQL, SQLite, PostgreSQL) directamente en el dispositivo móvil con soporte de red local (LAN), y respaldar todo en Google Drive.

---

## 🏛️ Módulos Integrados y Arquitectura

```
┌─────────────────────────────────────────────────────────────────────────────┐
│                             MBrowser Super-App                              │
│                      (Estética Aero Aqua Glassmorphism)                     │
└───────┬───────────────┬───────────────────┬─────────────────┬───────────────┘
        │               │                   │                 │
┌───────▼──────┐ ┌──────▼────────────┐ ┌────▼───────────┐ ┌───▼─────────────┐
│  Navegador   │ │ OfficeFree        │ │ Servidor Local │ │  Cloud Drive &  │
│  GeckoView   │ │ ToAndroid + AI    │ │ Apache & DBs   │ │  IngeHub        │
├──────────────┤ ├───────────────────┤ ├────────────────┤ ├─────────────────┤
│• Turbo Mode  │ │• Writer (DOCX/ODT)│ │• Apache 2.4    │ │• Google Drive   │
│• GameBoost   │ │• Calc (XLSX/ODS)  │ │• PHP 8.0 - 8.3 │ │  OAuth Sync     │
│• DevTools JS │ │• Impress (PPTX)   │ │• MySQL 8.0     │ │• Portafolio     │
│• AdBlocker   │ │• PDF Preview      │ │• PostgreSQL    │ │  ingemaxwell    │
│• Hard Reload │ │• Gemini AI Assist │ │• Soporte LAN   │ │  chacon.com     │
└──────────────┘ └───────────────────┘ └────────────────┘ └─────────────────┘
```

---

## 🚀 1. Núcleo del Navegador Web (GeckoView / Waterfox)
* **Modo Super Hiper Veloz (Turbo):** Bloqueo nativo ultra agresivo a nivel de red contra más de 40 dominios de rastreo, analíticas pesadas y anuncios invasivos.
* **Modo Gaming & Cloud Play (GameBoost):** Aceleración forzada por GPU (Hardware layer), WebGL, optimización de baja latencia en WebSockets/WebRTC para Xbox Cloud Gaming, GeForce Now y emuladores web con pantalla completa inmersiva.
* **Inspector de Consola & Limpiador de Caché en Vivo:** Captura de errores JS con botón de **copiado en 1 toque** y botón de **Purga Total de Caché (Hard Reload)** para desarrolladores web.
* **Diseño Retro-Futurista Glassmorphism:** Transparencias, reflejos de cristal y efectos inspirados en Windows Vista Aero y Mac Aqua.

---

## 📄 2. Módulo Ofimático Integrado (OfficeFreeToAndroid)
* **Suite Completa para Android:** Basada en OpenOffice / LibreOffice, adaptada y migrada para Android por **Maxwell Chacón**.
* **Aplicaciones Incluidas:**
  * **Writer:** Creación y edición de documentos de texto (`.docx`, `.odt`, `.txt`).
  * **Calc:** Hojas de cálculo avanzadas (`.xlsx`, `.ods`, `.csv`).
  * **Impress:** Presentaciones multimedia (`.pptx`, `.odp`).
  * **Visor PDF & Impresión:** Vista previa y exportación directa a formato PDF.
* **Gemini AI Assistant Integrado:** Asistente inteligente para resumir textos largos, mejorar redacción, formular cálculos complejos de hojas de cálculo y traducir contenido al instante.

---

## 🖥️ 3. Módulo de Servidor Local & Bases de Datos (ApacheMysqlAndroid_Server)
* **Stack Web Portable en Android:** Servidor local portable desarrollado por **Maxwell Chacón**.
* **Servicios Gestionados:**
  * **Apache HTTP Server (v2.4)** en puerto `8080`.
  * **PHP Engine (v8.0 - v8.3)** con soporte FPM / CLI.
  * **MySQL Database (v8.0)** en puerto `3306`.
  * **PostgreSQL (v16.2)** en puerto `5432` y **SQLite (v3.45)** embebido.
* **Soporte LAN:** Enrutamiento IP local para permitir el acceso a las aplicaciones web desde cualquier dispositivo conectado a la misma red WiFi.
* **Integración Directa:** Botón de un toque para abrir `http://localhost:8080` directamente en una pestaña de MBrowser.

---

## ☁️ 4. Capa Cloud, Google Drive & Monetización
* **Google Drive Connector:** Autenticación y sincronización en la nube de documentos ofimáticos y respaldos de datos del navegador.
* **IngeHub & Página de Inicio:** Integración por defecto hacia `https://ingemaxwellchacon.com` como panel central de proyectos, herramientas y monetización.

---

## 📜 5. Licenciamiento, Créditos y Atribuciones Open Source

Este proyecto cumple estrictamente con las licencias de código abierto aplicables:

1. **Waterfox / GeckoView (`BrowserWorks/waterfox`):**
   * Licencia: **Mozilla Public License 2.0 (MPL-2.0)**.
   * Reconocimiento a Mozilla y al proyecto Waterfox por el motor de renderizado móvil.
2. **OfficeFreeToAndroid (`mchacondev24/OfficeFreeToAndroid`):**
   * Desarrollado y migrado a Android por **Maxwell Chacón**.
   * Basado en las tecnologías de OpenOffice / LibreOffice bajo licencias Apache 2.0, LGPL 3.0 y MPL 2.0.
3. **ApacheMysqlAndroid_Server (`mchacondev24/ApacheMysqlAndroid_Server`):**
   * Desarrollado para Android por **Maxwell Chacón**.
   * Servidor portable y motores de base de datos bajo licencias Apache 2.0, GPL y MIT.
4. **Google Drive API & Gemini AI:**
   * Servicios en la nube para sincronización e inteligencia artificial.

---

## 📦 Compilación y Generación del APK

### Requisitos:
* **JDK:** Java 17+
* **Android SDK:** API Level 35 (Android 15)
* **Min SDK:** API Level 26 (Android 8.0 Oreo)
* **Gradle:** 8.11.1

### Compilar APK Debug:
```bash
./gradlew assembleDebug
```
Ubicación del APK generado:
`app/build/outputs/apk/debug/app-debug.apk`

---

## 🌐 Repositorio Oficial en GitHub
* **URL:** **[https://github.com/mchacondev24/MBrowser](https://github.com/mchacondev24/MBrowser)**
* **Autor:** **Maxwell Chacón** ([ingemaxwellchacon.com](https://ingemaxwellchacon.com))
