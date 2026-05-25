# Cambios: portal paciente (perfil y sesión)

Resumen del perfil autogestionado del paciente, caché en el front, y **profesional con rol paciente** (UI limitada en portal paciente).

## Profesional con doble rol (UI limitada)

Muchos profesionales se registran con **`ROLE_PROFESIONAL`** y **`ROLE_PACIENTE`** (misma cuenta, entidad `Profesional`).

| Acción | Comportamiento |
|--------|----------------|
| Login **portal paciente** (`portal: paciente`) | Permitido si tiene `ROLE_PACIENTE`. JWT incluye claim `portal: paciente`. |
| Login **portal profesional** | Permitido si es `Profesional` con `ROLE_PROFESIONAL`. |
| `GET /api/pacientes/me` | **200** con `PacientePortalSesionDTO`: `perfilEditable: false`, `tipoCuenta: PROFESIONAL_EN_PORTAL_PACIENTE`, nombre/apellido/email para saludo en dashboard. |
| `PUT /api/pacientes/{id}` | **400** `code: PERFIL_PACIENTE_NO_DISPONIBLE` — editar datos en portal profesional. |
| Front dashboard paciente | Turnos/historial habilitados; card perfil → botón **Ir al portal profesional**. |
| Front `/dashboard-paciente/perfil` | Pantalla informativa (sin formulario de edición). |

Código de error estable: **`PERFIL_PACIENTE_NO_DISPONIBLE`**.

---

## Backend (`ms-usuarios`)

### Endpoint de sesión

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/api/pacientes/me` | `ROLE_PACIENTE` | `PacientePortalSesionDTO` (ver arriba). |

### Servicio

- `obtenerPacienteSesion(email)` → `PacientePortalSesionDTO`.
- `actualizarPaciente`: rechaza `Profesional` con `PERFIL_PACIENTE_NO_DISPONIBLE`.
- Login: `validarTipoEntidadParaPortal` permite `Profesional` + `ROLE_PACIENTE` en portal paciente.

### Tests

- Paciente real: `perfilEditable: true`, `tipoCuenta: PACIENTE`.
- Profesional dual: login paciente → `GET /me` → `perfilEditable: false`; `PUT` → `PERFIL_PACIENTE_NO_DISPONIBLE`.

---

## Frontend (`frontend-clinica`)

### Caché (`PacienteSessionContext`)

- `sesion`, `perfilEditable`, `esProfesionalDual`, `paciente` (alias de `sesion`).
- Utilidad: `src/utils/portalPaciente.js`.

### Dashboard y perfil

- Saludo con datos de `sesion` aunque sea profesional dual.
- Aviso azul si `esProfesionalDual`.
- Perfil: bloqueo con link a `/login-profesional` si no es editable.

---

## Verificación manual

1. **Paciente puro:** login paciente → editar perfil OK.
2. **Profesional dual:** login paciente → dashboard con nombre → **Mi perfil** muestra mensaje y link al portal profesional; `PUT` no aplica.
3. Mismo usuario: login profesional → `/dashboard-profesional/perfil` → edición OK.

---

## Referencias

- [`CAMBIOS-PORTAL-PROFESIONAL-DASHBOARD.md`](CAMBIOS-PORTAL-PROFESIONAL-DASHBOARD.md)
- [`API.md`](API.md)
- [`COSAS-POR-HACER.md`](../COSAS-POR-HACER.md)
