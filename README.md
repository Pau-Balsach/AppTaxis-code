#🚖 App Taxis - Sistema de Gestión
Una aplicación de escritorio robusta desarrollada en JavaFX para la administración eficiente de servicios de taxi. Permite gestionar conductores y organizar viajes mediante un calendario interactivo, todo respaldado por una base de datos persistente.

#✨ Características Principales
Autenticación Segura: Sistema de Login para administradores con validación de credenciales.

Gestión de Conductores (CRUD): Registro, edición, visualización y eliminación de conductores (validación de matrículas incluida).

Calendario Dinámico: Visualización de servicios por día y mes.

Control de Viajes: Asignación de servicios a conductores específicos, incluyendo horarios, puntos de recogida/dejada y contacto del cliente.

Interfaz Moderna: Diseño limpio con una paleta de colores profesional (azul oscuro y amarillo taxi) y componentes de UI optimizados.

#🛠️ Stack Tecnológico
Lenguaje: Java 21

Framework UI: JavaFX 21 (FXML)

Gestión de Dependencias: Maven

Persistencia: Hibernate 6.6 (JPA)

Base de Datos: PostgreSQL 42.7

Logging: Jakarta Persistence & SLF4J

#🚀 Instalación y Configuración
1. Requisitos Previos
JDK 21 instalado.

Maven configurado en el sistema.

Instancia de PostgreSQL en ejecución.

#📂 Estructura del Proyecto
src/main/java/main: Contiene la clase de entrada (AplicacionTaxis.java).

src/main/java/ui: Controladores de JavaFX (LoginController, CalendarioController, etc.) y lógica de navegación (StageConfigurator).

src/main/java/model: Entidades de datos (Admin, Conductor, Viaje).

src/main/java/service: Lógica de negocio y acceso a datos.

src/main/resources/aplicaciotaxis/UI: Archivos .fxml que definen la interfaz visual.
