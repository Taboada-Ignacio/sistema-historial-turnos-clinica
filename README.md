# Sistema de Gestión Clínica 🏥

### Descripción
Un sistema de información integral para consultorios médicos privados, diseñado bajo una Arquitectura de Microservicios. Este proyecto tiene como objetivo gestionar el historial clínico de pacientes, la asignación de turnos de los profesionales y el control de acceso basado en roles (Pacientes, Profesionales y Administradores).

### Arquitectura
El backend está dividido en dominios independientes, respetando el patrón de una base de datos por servicio (*Database-per-Service*):
1. **Microservicio de Usuarios (`ms-usuarios`):** Gestión de identidades, roles y autenticación (Spring Security).
2. **Microservicio de Turnos (`ms-turnos`):** Lógica de agendamiento y disponibilidad.
3. **Microservicio de Historial Clínico (`ms-historial-clinico`):** Datos sensibles de salud y registro de consultas.
* **API Gateway:** Punto de entrada centralizado y enrutamiento inteligente.
* **Frontend:** Desarrollado con React (Próximamente).

### Tecnologías
* **Backend:** Java 21, Spring Boot 3, Spring Data JPA, Spring Security, Maven.
* **Base de Datos:** PostgreSQL (Esquemas independientes por servicio).
* **Infraestructura:** Docker y Docker Compose.

### Primeros Pasos (Infraestructura)
Para levantar el motor de base de datos localmente, asegúrate de tener Docker iniciado y ejecuta:
```bash
cd infra
docker-compose up -d

### Modelo de Datos (Microservicio de Usuarios)
El dominio de identidades está diseñado utilizando el patrón de herencia **Class Table Inheritance** (`InheritanceType.JOINED` en JPA), lo que permite una base de datos normalizada y código limpio orientado a objetos.

**Entidades Principales:**
* **`Usuario` (Clase Padre):** Contiene los datos comunes de acceso e identidad (Email, Password, DNI, Nombre, Apellido).
  * **`Paciente` (Clase Hija):** Hereda de `Usuario` y añade datos específicos como `ObraSocial` y `numero_afiliado`.
  * **`Profesional` (Clase Hija):** Hereda de `Usuario` y añade su `Especialidad` médica y `nro_matricula`.

**Catálogos y Relaciones:**
* **`Rol`**: Define el nivel de acceso (Paciente, Profesional, Admin).
* **Ubicación**: `Provincia` y `Localidad` (Relación One-to-Many).
* **Atributos Médicos**: `Especialidad` (para profesionales) y `Obra Social` (para pacientes).
