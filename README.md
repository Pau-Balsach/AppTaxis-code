# 🚖 App Taxis

Aplicación de escritorio para la administración integral de flotas de taxi. Permite gestionar conductores, organizar servicios y visualizar la logística diaria mediante un calendario interactivo, asegurando la persistencia de datos en una base de datos PostgreSQL.

## Características

- **Panel de Control Administrativo:** Acceso seguro mediante login para la gestión de la flota.
- **Gestión de Conductores (CRUD):** Registro completo de conductores con validación de matrículas (formato español 0000XXX).
- **Calendario Logístico:** Visualización interactiva de servicios organizada por días y meses.
- **Control de Viajes:** Asignación detallada de servicios (hora, recogida, destino y contacto) vinculada a cada conductor.
- **Arquitectura de Persistencia:** Uso de Hibernate para garantizar la integridad y seguridad de los datos.
- **Interfaz Moderna:** Diseño limpio con una paleta de colores corporativa (azul oscuro y amarillo taxi) optimizada para la productividad.

## 🚀 Descarga e Instalación

La forma más sencilla de usar App Taxis en Windows es descargando el instalador oficial desde nuestra sección de lanzamientos:

👉 **[Descargar App-Taxis v1.0(.exe)](https://github.com/Pau-Balsach/app-taxis-repo/releases/latest/download/AplicacioTaxis-1.0.1)**

## Requisitos

- Java 21 o superior
- PostgreSQL 16 o superior

## Tecnologías utilizadas

- **Java 21**
- **Maven** (Gestión de dependencias)
- **JavaFX 21** (Interfaz gráfica)
- **Hibernate 6.6** (ORM)
- **PostgreSQL 42.7** (Base de datos)
- **Stage Management** personalizado para navegación fluida

## Seguridad

- **Validación de Datos:** Uso de expresiones regulares (Regex) para asegurar que las matrículas y entradas sean correctas antes de guardarlas.
- **Gestión de Sesión:** Manejo de objetos `Admin` a través de las escenas para mantener la trazabilidad de las acciones.
- **Integridad Referencial:** Relaciones estrictas en la base de datos entre conductores y viajes para evitar la pérdida de información.
- **Controlador de Escenas:** Uso de `StageConfigurator` para una navegación centralizada, evitando fugas de memoria y redundancia de ventanas.
- **Protección de Datos:** Configuración de persistencia aislada mediante `persistence.xml`.

## Licencia

MIT License — ver archivo [LICENSE](LICENSE)
