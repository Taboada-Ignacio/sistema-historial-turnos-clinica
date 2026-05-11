# Entidades y modelo de datos

Este documento describe el **modelo de persistencia** del único microservicio de dominio presente hoy en el repo: **`ms-usuarios`**. El **API Gateway** no define entidades JPA (solo enruta).

Los futuros microservicios (**turnos**, **historial**, etc.) tendrán **su propia base de datos** (database-per-service); las referencias a “usuario” o “paciente” en otros MS deben ser por **identificador estable** (`idUsuario`, email según contrato) sin tablas duplicadas de credenciales — ver [`SEGURIDAD.md`](SEGURIDAD.md).

---

## Visión general (`ms-usuarios`)

- Herencia **JOINED**: tabla **`usuarios`** (padre) + tablas hijas **`pacientes`**, **`profesionales`**, **`administradores`** (`@PrimaryKeyJoinColumn`: mismo id que `id_usuario`).
- Identidad y RBAC viven aquí; **roles** en tabla **`roles`**, relación **N:N** con usuarios vía **`usuario_roles`**.
- Estados de cuenta: **`estados`** + histórico **`cambios_estado`** (auditoría).
- Tokens de verificación de email: tabla **`tokens_confirmacion`** (`VerificationToken`).
- Sesiones largas: **`refresh_tokens`** vinculados a **`usuarios`**.

---

## Diagrama simplificado (Mermaid)

```mermaid
erDiagram
    usuarios ||--o| pacientes : "JOINED id"
    usuarios ||--o| profesionales : "JOINED id"
    usuarios ||--o| administradores : "JOINED id"
    usuarios }o--|| estados : "id_estado_actual"
    usuarios }o--|| localidades : "id_localidad"
    usuarios }o--o{ usuario_roles : ""
    roles }o--o{ usuario_roles : ""
    provincias ||--o{ localidades : ""
    pacientes }o--|| obras_sociales : ""
    profesionales }o--|| especialidades : ""
    profesionales }o--|| membresias : "membresía actual"
    profesionales ||--o{ cambios_membresia : historial
    usuarios ||--o{ cambios_estado : historial
    usuarios ||--o{ refresh_tokens : ""
    usuarios ||--o{ tokens_confirmacion : ""
```

---

## Entidades por bloque

### Identidad y personas

| Entidad | Tabla | Descripción |
|---------|-------|-------------|
| **`Usuario`** | `usuarios` | Email, password (hash), nombre, apellido, DNI, teléfono, fecha nacimiento, dirección opcional, FK estado actual, FK localidad, roles N:N. |
| **`Paciente`** | `pacientes` | Extiende `Usuario`; FK obligatoria **obra social**, `numero_afiliado` opcional. |
| **`Profesional`** | `profesionales` | Matrícula única, FK **especialidad**, foto perfil (ruta), FK **membresía actual**, historial cambios membresía. |
| **`Administrador`** | `administradores` | Sin campos extra por ahora; extiende `Usuario`. |

### Seguridad y ciclo de vida

| Entidad | Tabla | Descripción |
|---------|-------|-------------|
| **`Rol`** | `roles` | `descripcion` única (ej. `ROLE_PACIENTE`, `ROLE_PROFESIONAL`, `ROLE_ADMINISTRADOR`). |
| **`Estado`** | `estados` | Nombre único: ej. `PENDIENTE`, `ACTIVO`, `BLOQUEADO`. |
| **`CambioEstado`** | `cambios_estado` (convención JPA) | Usuario, estado, fecha (auditoría). |
| **`VerificationToken`** | `tokens_confirmacion` | Token email confirmación / recuperación (OneToOne con usuario). |
| **`RefreshToken`** | `refresh_tokens` | Token opaco, usuario, expiración, revocado. |

### Catálogos

| Entidad | Tabla | Relaciones |
|---------|-------|------------|
| **`Provincia`** | `provincias` | 1 — N **Localidad**. |
| **`Localidad`** | `localidades` | N — 1 **Provincia**. |
| **`Especialidad`** | `especialidades` | Usada por **Profesional**. |
| **`ObraSocial`** | `obras_sociales` | Usada por **Paciente**. |
| **`Membresia`** | `membresias` | Nombre único (ej. `SIN_VERIFICAR`, `ACTIVA`); **Profesional** tiene membresía actual + historial **`CambioMembresia`**. |

---

## Claves e IDs

- **`Usuario.idUsuario`** es la clave primaria compartida en la jerarquía JOINED (paciente/profesional/administrador usan el mismo valor en su tabla hija).
- En APIs de autorización frecuente comparar **`id` de ruta** con el usuario autenticado — ver bean `authorizationRules` en [`SEGURIDAD.md`](SEGURIDAD.md).

---

## Microservicios aún no en el repo

Cuando existan **ms-turnos**, **ms-historial-clínico**, etc., conviene añadir una subsección o archivo **`docs/ENTIDADES-<servicio>.md`** por servicio con sus tablas y cómo referencian **`idUsuario`** (o IDs externos) sin replicar tablas de login.
