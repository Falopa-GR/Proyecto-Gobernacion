-Sistema de Gestión de Talento Humano

Sistema de escritorio desarrollado en **Java Swing** para la gestión integral del talento humano en entidades públicas. Permite administrar la planta de personal, situaciones administrativas, vacaciones, bienestar, salud ocupacional y evaluaciones de desempeño, con exportación de reportes a Excel y PDF.

---

-Tabla de Contenidos

- [Funcionalidades](#-funcionalidades)
- [Tecnologías](#-tecnologías)
- [Requisitos Previos](#-requisitos-previos)
- [Instalación y Configuración](#-instalación-y-configuración)
- [Credenciales por Defecto](#-credenciales-por-defecto)
- [Roles y Permisos](#-roles-y-permisos)
- [Estructura del Proyecto](#-estructura-del-proyecto)

---
-Funcionalidades

RF-01 — Planta de Personal
Gestión completa del registro de servidores públicos vinculados a la entidad.

- Registrar, editar y dar de baja servidores públicos.
- Campos gestionados: cédula, nombres, apellidos, fecha de nacimiento, género, estado civil, grupo sanguíneo, teléfono, correo, dependencia, cargo, código del cargo, tipo de vinculación, fecha de ingreso y salario mensual.
- Tabla con todos los servidores activos, con filas de solo lectura.
- **Doble clic** sobre un servidor en la tabla para abrir su perfil integrado (RF-08).

---

RF-02 — Situaciones Administrativas
Registro y seguimiento del historial de novedades de cada servidor.

- Tipos de situación disponibles: Vacaciones, Permiso 1 día, Permiso 2-3 días, Licencia remunerada, Licencia no remunerada, Maternidad, Paternidad, Enfermedad, Comisión, Traslado y Asignación especial.
- Selección por cédula del servidor y validación de fechas para evitar situaciones traslapadas (`OverlappingSituationException`).
- Panel superior con la situación activa actual del servidor cargado.
- Tabla con código de color: **verde** = situación activa hoy, **gris** = situación pasada.
- Botón "Limpiar" para resetear el formulario sin cerrar la ventana.
- Registro del acto administrativo de respaldo y notas.

---

RF-03 — Control de Vacaciones
Control detallado del saldo vacacional de cada servidor.

- Registro de períodos de vacaciones por año: días acumulados, días usados y días pendientes.
- Panel de resumen con el total de días causados, usados y pendientes del servidor.
- Historial completo de períodos con tabla de colores por estado.
- **Tabla de alertas**: identifica automáticamente los servidores que adeudan más de un período de vacaciones sin disfrutar.
- Cálculo automático de años de servicio desde la fecha de ingreso.

---

RF-07 — Dashboard Principal
Panel de control central con métricas en tiempo real y acceso a todas las funcionalidades.

- **Cards de resumen**: total de servidores activos, ausentes hoy, en deuda de vacaciones y en comisión.
- **Situaciones de hoy**: tabla con todos los servidores que tienen una situación administrativa activa en la fecha actual.
- **Vacaciones en deuda**: listado de servidores que acumulan períodos sin disfrutar.
- **Historial**: búsqueda por cédula o nombre con tabla de situaciones del servidor seleccionado.
- **Reportes**: exportación a Excel (.xlsx) y PDF de los datos de planta, situaciones y vacaciones.
- **Búsqueda global**: campo de búsqueda con autocompletado y doble clic en resultados para abrir el perfil del servidor.
- Carga de datos en segundo plano con `SwingWorker` para no bloquear la interfaz.
- Sidebar de navegación con acceso directo a cada módulo.

---

RF-08 — Perfil Integrado del Servidor
Vista consolidada de toda la información de un servidor público en una sola ventana con pestañas.

- **Pestaña Datos Personales**: información completa del servidor (cargo, dependencia, tipo de vinculación, antigüedad calculada automáticamente).
- **Pestaña Situación Actual**: situación administrativa vigente, con indicador visual de estado.
- **Pestaña Historial**: tabla completa de todas las situaciones administrativas pasadas y presentes.
- **Pestaña Vacaciones**: resumen del saldo vacacional total (causadas, usadas, pendientes).
- Se accede haciendo **doble clic** en cualquier resultado de búsqueda del Dashboard, o directamente desde la tabla de Planta de Personal.

---

-Exportación de Reportes
Generación de reportes listos para imprimir o archivar.

- Exportar a **Excel (.xlsx)** con formato de cabeceras resaltadas (Apache POI).
- Exportar a **PDF** con logo institucional, fecha del reporte y tabla formateada (iText).
- Reportes disponibles: planta activa, situaciones del día, historial por servidor y servidores en deuda de vacaciones.

---

 Autenticación y Seguridad
- Pantalla de inicio de sesión con validación de credenciales contra la base de datos.
- Contraseñas almacenadas con hash **SHA-256** (nunca en texto plano).
- Creación automática de usuarios por defecto al iniciar por primera vez.

---

-Tecnologías

| Componente | Tecnología |
|---|---|
| Lenguaje | Java 17+ |
| Interfaz gráfica | Java Swing |
| Persistencia | Hibernate 6.4 + JPA 3.1 |
| Base de datos | PostgreSQL 42.7 |
| Build | Gradle 8.8 |
| Exportación Excel | Apache POI 5.2 |
| Exportación PDF | iText 5.5 |
| Selector de fechas | JCalendar 1.4 |

---

-Requisitos Previos

- Java 17 o superior
- PostgreSQL instalado y en ejecución
- Gradle (o usar el wrapper incluido `./gradlew`)

---

-Instalación y Configuración

**1. Clonar el repositorio**
```bash
git clone <url-del-repositorio>
cd integrador
```

**2. Crear la base de datos en PostgreSQL**
```sql
CREATE DATABASE talento_humano;
```

**3. Configurar la conexión** en `src/main/resources/META-INF/persistence.xml`:
```xml
<property name="jakarta.persistence.jdbc.url"      value="jdbc:postgresql://localhost:5432/talento_humano"/>
<property name="jakarta.persistence.jdbc.user"     value="postgres"/>
<property name="jakarta.persistence.jdbc.password" value="TU_CONTRASEÑA"/>
```

**4. Compilar y ejecutar**
```bash
./gradlew run
```

Hibernate creará automáticamente todas las tablas al iniciar (`hbm2ddl.auto=update`). Los usuarios por defecto también se crean solos si la base de datos está vacía.



-Credenciales por Defecto

| Usuario | Contraseña | Rol |
|---|---|---|
| `admin` | `admin123` | Administrador |
| `gestor` | `gestor123` | Gestor |
| `consulta` | `consulta123` | Consulta |

-Roles y Permisos

| Acción | ADMIN | GESTOR | CONSULTA |
|---|:---:|:---:|:---:|
| Ver planta de personal | X | X | X |
| Registrar / editar servidores | X | X |   |
| Gestionar situaciones administrativas | X | X |   |
| Gestionar vacaciones | X | X |   |
| Exportar reportes | X | X | X |
| Crear usuarios | X |   |   |

