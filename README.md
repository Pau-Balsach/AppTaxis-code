# 🚖 App Taxis

Aplicación de escritorio para la administración integral de flotas de taxi. Permite gestionar conductores, organizar servicios y visualizar la logística diaria mediante un calendario interactivo, con persistencia de datos en PostgreSQL a través de Supabase.

---

## ✨ Características

| Módulo | Descripción |
|---|---|
| 🔐 **Login seguro** | Autenticación de administradores mediante Supabase Auth |
| 👤 **Gestión de conductores** | CRUD completo con validación de matrícula española (`0000XXX`) |
| 📅 **Calendario logístico** | Vista mensual interactiva con indicadores de viajes por conductor |
| 🗺️ **Control de viajes** | Asignación de hora, recogida, destino y teléfono cliente con validación en tiempo real |
| ⚡ **Carga optimizada** | Una sola query por mes con caché en memoria — sin bloqueos en la UI |
| 🎨 **Interfaz moderna** | Paleta corporativa azul/amarillo taxi, colores únicos por conductor |

---

## 🚀 Instalación

### Opción A — Instalador Windows (recomendado)

Descarga el instalador y ejecútalo directamente. No requiere instalar Java por separado.

👉 **[Descargar AplicacioTaxis-1.0.2.exe](https://github.com/Pau-Balsach/apptaxis-code/releases/latest/download/AplicacioTaxis-1.0.2.exe)**

> Compatible con Windows 10 y Windows 11.

### Opción B — Compilar desde el código fuente

**Requisitos previos:**
- JDK 21+
- Maven 3.8+

```

> ⚠️ Este archivo **nunca** debe subirse al repositorio. Está incluido en `.gitignore`.

---

## 🗄️ Estructura de la base de datos

```sql
admins      (id UUID, email)
conductores (id SERIAL, matricula, nombre, cond_admin UUID)
viajes      (id UUID, dia, hora, puntorecogida, puntodejada, telefonocliente, conductor_id)
```

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
- **Validación de entradas** con regex antes de cualquier operación de escritura
- **Integridad referencial** en BD — los viajes no pueden existir sin conductor
- **Credenciales externas** — el `config.properties` se distribuye solo en el instalador compilado, nunca en el repositorio
- **Gestión centralizada de escenas** con `StageConfigurator` para evitar fugas de memoria

---

## 📁 Estructura del proyecto

```
src/main/
├── java/
│   ├── main/         # Punto de entrada (Launcher, AplicacionTaxis)
│   ├── model/        # Entidades JPA (Admin, Conductor, Viaje)
│   ├── repository/   # Acceso a datos (JPA/Hibernate)
│   ├── service/      # Lógica de negocio
│   └── ui/           # Controladores JavaFX
└── resources/
    └── aplicaciotaxis/UI/   # Archivos FXML
```

## 📄 Licencia

MIT License — ver archivo [LICENSE](LICENSE)
