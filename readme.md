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
| ⚡ **Carga optimizada** | Una sola query por mes con caché en memoria — sin bloqueos en la UI |
| 🎨 **Interfaz moderna** | Paleta corporativa azul/amarillo taxi, colores únicos por conductor |

---

## 🚀 Instalación

### Instalador Windows

Descarga el instalador y ejecútalo directamente. No requiere instalar Java por separado.

👉 **[Descargar AplicacioTaxis-1.1.1.exe](https://github.com/Pau-Balsach/apptaxis-code/releases/latest/download/AplicacioTaxis-1.1.0.exe)**

> Compatible con Windows 10 y Windows 11.

---

## 🛠️ Tecnologías

- **Java 21** — Lenguaje principal
- **JavaFX 21** — Interfaz gráfica de escritorio
- **Hibernate 6.6** — ORM y gestión de persistencia
- **PostgreSQL / Supabase** — Base de datos y autenticación
- **Maven** — Gestión de dependencias y build
- **jpackage** — Generación del instalador nativo `.exe`
- **JUnit 5** — Framework de tests unitarios
- **Mockito 5** — Mocking de dependencias en tests
- **JaCoCo** — Cobertura de código (mínimo 80%)
- **GitHub Actions** — CI/CD automático en cada push

---

## 🧪 Tests

El proyecto incluye una suite de **96 tests unitarios** que cubren toda la lógica de negocio, ejecutados automáticamente en cada push mediante GitHub Actions.

### Cobertura por módulo

| Clase | Tests | Qué se verifica |
|---|---|---|
| `ViajeModel` | 10 | UUID automático, `cruzaMedianoche()`, getters/setters |
| `ClienteService` | 27 | Validación teléfono español, email, CRUD completo |
| `ConductorService` | 22 | Validación matrícula `0000XXX`, control de sesión, CRUD |
| `SecurityUtils` | 7 | Hash SHA-256 determinista, casos límite |
| `SessionManager` | 15 | Ciclo de vida de sesión, `checkAuth`, flags de ventana |
| `ViajeService` | 15 | Crear/editar/eliminar viajes, consultas por mes y conductor |

### Ejecutar los tests localmente

```bash
# Solo tests
mvn test

# Tests + reporte de cobertura (se genera en target/site/jacoco/index.html)
mvn verify
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
│   │   └── UI/           # Controladores JavaFX
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