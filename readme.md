# 🚖 App Taxis

Aplicación de escritorio para la administración integral de flotas de taxi. Permite gestionar conductores, organizar servicios y visualizar la logística diaria mediante un calendario interactivo, con persistencia de datos en PostgreSQL a través de Supabase.

![CI/CD](https://github.com/Pau-Balsach/apptaxis-code/actions/workflows/ci-cd.yml/badge.svg)
![Java](https://img.shields.io/badge/Java-21-blue)
![JavaFX](https://img.shields.io/badge/JavaFX-21-orange)
![Tests](https://img.shields.io/badge/tests-96%20passed-brightgreen)
![Coverage](https://img.shields.io/badge/coverage-%E2%89%A580%25-brightgreen)
![License](https://img.shields.io/badge/license-MIT-blue)

---

## ✨ Características

| Módulo | Descripción |
|---|---|
| 🔐 **Login seguro** | Autenticación de administradores mediante Supabase Auth |
| 👤 **Gestión de conductores** | CRUD completo con validación de matrícula española (`0000XXX`) |
| 👥 **Gestión de clientes** | CRUD con validación de teléfono español y email |
| 📅 **Calendario logístico** | Vista mensual interactiva con indicadores de viajes por conductor |
| 🗺️ **Control de viajes** | Asignación de hora, recogida, destino y teléfono cliente con validación en tiempo real |
| 📍 **Autocompletado de direcciones** | Búsqueda de calles reales via Nominatim (OpenStreetMap) con apertura directa en Google Maps |
| ⚡ **Carga optimizada** | Una sola query por mes con caché en memoria — sin bloqueos en la UI |
| 🎨 **Interfaz moderna** | Paleta corporativa azul/amarillo taxi, colores únicos por conductor |

---

## 🚀 Instalación

### Instalador Windows

Descarga el instalador y ejecútalo directamente. No requiere instalar Java por separado.

👉 **[Descargar AplicacioTaxis-1.1.3.exe](https://github.com/Pau-Balsach/apptaxis-code/releases/latest/download/AplicacioTaxis-1.1.3.exe)**

> Compatible con Windows 10 y Windows 11.

---

## 🛠️ Tecnologías

- **Java 21** — Lenguaje principal
- **JavaFX 21** — Interfaz gráfica de escritorio
- **Hibernate 6.6** — ORM y gestión de persistencia (JPA)
- **PostgreSQL / Supabase** — Base de datos relacional y autenticación
- **Supabase Auth** — Login de administradores con JWT (sin contraseñas locales)
- **Maven** — Gestión de dependencias y build
- **Nominatim (OpenStreetMap)** — Autocompletado de direcciones y geocodificación inversa (sin API key)
- **jpackage** — Generación del instalador nativo `.exe`
- **JUnit 5 + Mockito 5** — Tests unitarios con inyección de mocks por reflexión
- **JaCoCo** — Cobertura de código (mínimo 80%)
- **GitHub Actions** — CI/CD automático en cada push

---

## 🧪 Tests

El proyecto incluye **96 tests unitarios** que cubren toda la lógica de negocio, sin conexión a base de datos real — los repositorios se inyectan como mocks via reflexión. Se ejecutan automáticamente en cada push mediante GitHub Actions.

| Clase de test | Tests | Qué se verifica |
| :--- | :---: | :--- |
| **ViajeModelTest** | 10 | UUID autogenerado, `cruzaMedianoche()` (mismo día, día posterior, null, cruce de mes/año), getters/setters |
| **ClienteServiceTest** | 27 | Validación de teléfono español (`6/7/8/9xx`, `+34`, formatos inválidos), email, nombre; CRUD completo delegado al repo |
| **ConductorServiceTest** | 22 | Validación de matrícula `0000XXX` (formatos límite y erróneos), nombre, matrícula duplicada, control de sesión, CRUD |
| **SecurityUtilsTest** | 7 | Hash SHA-256 determinista, sensibilidad a mayúsculas, cadena vacía, null → `""`, texto largo, caracteres especiales |
| **SessionManagerTest** | 15 | Ciclo de vida de sesión (`iniciarSesion`, `cerrarSesion`), `checkAuth` con distintos estados, modo demo, flags de ventana |
| **ViajeServiceTest** | 15 | Crear/editar/eliminar viajes, conductor inexistente, consultas por mes/fecha/conductor, control de sesión |

```bash
mvn test                  # Solo tests
mvn verify                # Tests + reporte JaCoCo (target/site/jacoco/index.html)
```

---

## 🔄 CI/CD

Cada push a `main` o `develop` ejecuta automáticamente el pipeline de GitHub Actions:

1. **Compilación** con Java 21
2. **96 tests unitarios** con JUnit 5 + Mockito
3. **Reporte JaCoCo** — falla si la cobertura baja del 80%
4. **Publicación** de resultados de tests como Check Run en GitHub
5. **Artefacto** con el reporte HTML de cobertura (disponible 14 días)

---

## 🔒 Seguridad

- **Autenticación** via Supabase Auth — las contraseñas nunca se almacenan localmente
- **Validación de entradas** con regex antes de cualquier operación de escritura
- **Integridad referencial** en BD — los viajes no pueden existir sin conductor
- **Credenciales externas** — el `config.properties` se distribuye solo en el instalador compilado, nunca en el repositorio
- **Gestión centralizada de escenas** con `StageConfigurator` para evitar fugas de memoria
- **Coordenadas geográficas** almacenadas con cada viaje — el enlace a Google Maps usa lat/lng exactos cuando la dirección fue seleccionada del autocompletado, o búsqueda por texto como fallback

---

## 📁 Estructura del proyecto

```
src/
├── main/
│   ├── java/
│   │   ├── main/         # Punto de entrada (Launcher, AplicacionTaxis)
│   │   ├── model/        # Entidades JPA (Admin, Conductor, Cliente, Viaje)
│   │   ├── repository/   # Acceso a datos (JPA/Hibernate)
│   │   ├── service/      # Lógica de negocio y seguridad
│   │   └── UI/           # Controladores JavaFX + AddressAutocompleteField + NominatimService
│   └── resources/
│       └── aplicaciotaxis/UI/   # Archivos FXML
└── test/
    ├── java/
    │   ├── model/        # Tests del modelo (ViajeModelTest)
    │   └── service/      # Tests de servicios (5 clases, 86 tests)
    └── resources/
        └── mockito-extensions/  # Configuración MockMaker (Java 25 compat.)
```

---

## 📄 Licencia

MIT License — ver archivo [LICENSE](LICENSE)