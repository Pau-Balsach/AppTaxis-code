# 🚖 App Taxis

Aplicación de escritorio para la administración integral de flotas de taxi. Permite gestionar conductores, organizar servicios y visualizar la logística diaria mediante un calendario interactivo, con persistencia de datos en PostgreSQL a través de Supabase.

---

## ✨ Características

| Módulo | Descripción |
|---|---|
| 🔐 **Login seguro** | Autenticación de administradores mediante Supabase Auth con gestión de sesión via JWT |
| 👤 **Gestión de conductores** | CRUD completo con validación de matrícula española (`0000XXX`) |
| 📅 **Calendario logístico** | Vista mensual interactiva con indicadores de viajes por conductor |
| 🗺️ **Control de viajes** | Asignación de hora de inicio/fin, recogida, destino y teléfono cliente con validación en tiempo real |
| ⚡ **Carga optimizada** | Una sola query por mes con caché en memoria — sin bloqueos en la UI |
| 🎨 **Interfaz moderna** | Paleta corporativa azul/amarillo taxi, colores únicos por conductor |

---

## 🌐 Ecosistema y API REST

Este proyecto cuenta con una **API REST complementaria** (desarrollada en Spring Boot) que ataca a la misma base de datos. Permite gestionar la flota de forma programática o integrarla con otras plataformas web/móviles.

👉 **[Ver Repositorio de la API](https://github.com/Pau-Balsach/apptaxis-api)**

---

## 🚀 Instalación

### 🟡 Versión pública de demostración (descargable)

La versión descargable es una **versión de prueba pública** pensada para explorar la aplicación libremente. Utiliza una base de datos compartida de demostración y **no incluye todas las funcionalidades del sistema completo** por razones de seguridad:

- ❌ Sin sistema de API keys — no hay aislamiento de datos por empresa
- ❌ Acceso a un entorno de demo compartido, no a datos reales de producción

> Esta versión es ideal para evaluar la interfaz y el flujo general de la aplicación antes de solicitar acceso completo.

👉 **[Descargar AplicacioTaxis-1.0.3.exe](https://github.com/Pau-Balsach/apptaxis-code/releases/latest/download/AplicacioTaxis-1.0.3.exe)**

> Compatible con Windows 10 y Windows 11. No requiere instalar Java por separado.

---

### 🔵 Versión completa (acceso privado)

La versión completa del sistema incluye todas las funcionalidades y está pensada para empresas que quieran gestionar su propia flota de forma segura e independiente:

- ✅ **API keys por empresa** — cada cliente accede únicamente a sus propios datos
- ✅ Base de datos de producción aislada
- ✅ Acceso completo a la API REST
- ✅ Soporte y actualizaciones

Para solicitar acceso, contacta con el autor del proyecto.

---

## 🛠️ Tecnologías

- **Java 21** — Lenguaje principal
- **JavaFX 21** — Interfaz gráfica de escritorio
- **Hibernate 6.6** — ORM y gestión de persistencia
- **PostgreSQL / Supabase** — Base de datos y autenticación
- **Maven** — Gestión de dependencias y build
- **jpackage** — Generación del instalador nativo `.exe`

---

## 🔒 Seguridad

- **Autenticación** via Supabase Auth — las contraseñas nunca se almacenan localmente
- **Gestión de sesión con JWT** — al hacer login, Supabase devuelve un token que se guarda en memoria (`SessionManager`) y se invalida al cerrar la app o al salir
- **Sistema de API keys** — en la versión completa, cada empresa opera sobre su propio espacio de datos aislado
- **Validación de entradas** con regex antes de cualquier operación de escritura — matrículas, horas (`HH:mm`) y teléfonos españoles
- **Integridad referencial** en BD — los viajes no pueden existir sin conductor
- **Credenciales externas** — el `config.properties` se distribuye solo en el instalador compilado, nunca en el repositorio
- **Gestión centralizada de escenas** con `StageConfigurator` para evitar fugas de memoria

---

## 📁 Estructura del Proyecto

```tree
src/main/
├── java/com/app/taxis/
│   ├── core/                # Punto de entrada (Launcher, AppConfig)
│   ├── model/               # Entidades JPA (Admin, Conductor, Viaje)
│   ├── repository/          # Interfaces de acceso a datos (JPA/Hibernate)
│   ├── service/             # Lógica de negocio
│   │   └── security/        # Gestión de sesiones y JWT (SessionManager)
│   └── controller/          # Controladores de JavaFX (Lógica de vista)
└── resources/
    ├── views/               # Archivos FXML
    ├── styles/              # Hojas de estilo CSS
    └── assets/              # Imágenes e iconos


## 📄 Licencia

MIT License — ver archivo [LICENSE](LICENSE)
