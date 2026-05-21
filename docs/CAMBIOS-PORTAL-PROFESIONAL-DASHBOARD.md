# Cambios: portal profesional (dashboard y catálogo)

Resumen de las mejoras al panel del profesional de la salud, APIs de sesión/presentación y nomenclatura de especialidades.

## Frontend (`frontend-clinica`)

### Dashboard profesional (`DashboardProfesional.jsx`)

- **Título:** "Panel de Control de Profesional de la Salud" (antes "Panel de Control Médico").
- **Bienvenida:** muestra la **especialidad** del profesional (ej. "Bienvenido, Odontólogo…") en lugar de "Dr./Dra.".
- **Perfil en cabecera:** foto arriba y **apellido, nombre** debajo, alineados a la derecha; título y bienvenida agrupados a la izquierda.
- **Datos:** `GET /usuarios/api/profesionales/me` (id y membresía) + `GET /usuarios/api/profesionales/{id}/presentacion` (nombre, apellido, foto, especialidad).
- **Membresía `SIN_VERIFICAR`:** cartel amarillo de cuenta pendiente de verificación administrativa; módulos **Agenda** y **Mis Pacientes** en gris y sin enlace; **Configuración** sigue habilitada.

### Utilidades

- `src/utils/formatEspecialidad.js`: etiquetas legibles para especialidades (mayúsculas del catálogo y legado `MEDICO` → "Medicina general").
- `src/utils/auth.js`: `getSessionEmail()` para leer el email de la sesión.

### Otros componentes UI

- `RegistroProfesional.jsx`: opciones del select de especialidad con `formatEspecialidad`; errores de registro (email/DNI duplicado, etc.) en banner leyendo `response.data.mensaje`.
- `AdminCatalogoListaPage.jsx`, `ProfesionalesPendientesPage.jsx`, `ProfesionalPendienteDetallePage.jsx`: misma formateo al mostrar especialidades.
- **Rechazo de pendientes:** `ProfesionalPendienteDetallePage.jsx` (botón), `ProfesionalPendienteRechazarPage.jsx` (motivo + contraseña). Ver [`CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md`](CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md).

## Backend (`ms-usuarios`)

### Nuevos / ajustados endpoints

| Método | Ruta | Rol | Descripción |
|--------|------|-----|-------------|
| GET | `/api/profesionales/me` | `ROLE_PROFESIONAL` | Perfil completo del profesional autenticado (incluye `membresiaActual`, `especialidad`, `fotoPerfil`, etc.). |
| GET | `/api/profesionales/{id}/presentacion` | ADMIN, PACIENTE o PROFESIONAL | Ficha pública. Si el solicitante es **el mismo profesional**, se devuelve aunque no esté `ACTIVO` (p. ej. `SIN_VERIFICAR`). Para terceros, solo profesionales `ACTIVO` sin rol admin. |
| POST | `/api/profesionales/{id}/rechazar-pendiente` | ADMIN | Rechazo de solicitud pendiente: valida contraseña del admin, email al profesional, borrado en cascada. |

### Servicio

- `obtenerProfesionalSesion(email)` en `ProfesionalService` / `ProfesionalServiceImpl`.
- `obtenerParaPresentacion(id, emailSolicitante)` permite auto-consulta sin filtro de catálogo activo.

## Base de datos

- **`init.sql`:** especialidad semilla `MEDICO` renombrada a **`MEDICINA GENERAL`**.
- **`migration-rename-especialidad-medico.sql`:** migración idempotente para bases existentes (`MEDICO` → `MEDICINA GENERAL`).

### Aplicar en entorno ya levantado

```sql
UPDATE especialidades
SET descripcion = 'MEDICINA GENERAL'
WHERE descripcion = 'MEDICO'
  AND NOT EXISTS (SELECT 1 FROM especialidades WHERE descripcion = 'MEDICINA GENERAL');
```

## Flujo de membresía (contexto UI)

1. Registro → `SIN_VERIFICAR`, estado pendiente.
2. Confirmación de email → puede iniciar sesión en el portal profesional.
3. Dashboard con `SIN_VERIFICAR` → aviso amarillo y agenda/pacientes deshabilitados.
4. Admin verifica matrícula (`PUT /{id}/verificar-matricula`) → `INACTIVA` (y luego activación según reglas de negocio existentes).
5. **Alternativa:** admin rechaza la solicitud (`POST /{id}/rechazar-pendiente`) → correo con motivo y borrado del registro (solo `SIN_VERIFICAR`).

## Verificación manual sugerida

1. Profesional con email confirmado y `membresiaActual = SIN_VERIFICAR`: login → dashboard con cartel y tarjetas grises.
2. Mismo usuario tras verificación admin: sin cartel y enlaces de agenda/pacientes activos.
3. Cabecera: especialidad en bienvenida, foto y nombre visibles.
4. Catálogo admin / registro: especialidades sin texto crudo `MEDICO`.
