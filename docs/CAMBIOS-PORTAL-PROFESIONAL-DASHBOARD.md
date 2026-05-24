# Cambios: portal profesional (dashboard y catálogo)

Resumen de las mejoras al panel del profesional de la salud, APIs de sesión/presentación, caché de perfil en el front y nomenclatura de especialidades.

## Frontend (`frontend-clinica`)

### Caché de sesión (`ProfesionalSessionContext`)

- **`src/context/ProfesionalSessionContext.jsx`**: provider que carga una vez por sesión del portal `GET /me` + `GET /{id}/presentacion` y guarda en memoria `presentacion`, `membresiaActual`, `idUsuario`.
- Montado en **`ProfesionalRoute.jsx`** (envuelve `<Outlet />`), de modo que al navegar entre subpáginas del portal **no se repiten** esos GET al volver al dashboard.
- Hook **`useProfesionalSession()`** expone también **`refetch()`** (fuerza recarga en segundo plano) e **`invalidate()`** (limpia caché y vuelve a cargar). Usar **`invalidate()`** tras editar perfil cuando exista la pantalla de configuración.
- Al cerrar sesión o salir del área protegida, el provider se desmonta y la caché se pierde (comportamiento esperado).

### Dashboard profesional (`DashboardProfesional.jsx`)

- **Título:** "Panel de Control de Profesional de la Salud" (antes "Panel de Control Médico").
- **Bienvenida:** muestra la **especialidad** del profesional (ej. "Bienvenido, Odontólogo…") en lugar de "Dr./Dra.".
- **Perfil en cabecera:** foto arriba y **apellido, nombre** debajo, alineados a la derecha; título y bienvenida agrupados a la izquierda.
- **Datos:** consume `useProfesionalSession()` (ya no hace fetch local en cada montaje del dashboard).
- **Membresía `SIN_VERIFICAR`:** cartel amarillo de cuenta pendiente de verificación administrativa; módulos **Agenda** y **Mis Pacientes** en gris y sin enlace; **Configuración** sigue habilitada.

### Registro profesional — foto en formulario multi-paso

- **`RegistroProfesional.jsx`:** preview de foto con `URL.createObjectURL`; la imagen se conserva en `formData.foto` al volver a un paso anterior; `required` del file input solo si aún no hay foto.

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
| GET | `/api/profesionales/{id}/presentacion` | ADMIN, PACIENTE o PROFESIONAL | Ficha pública. Si el solicitante es **el mismo profesional**, se devuelve aunque no esté `ACTIVO` (p. ej. `SIN_VERIFICAR`). Para terceros, solo profesionales con **estado `ACTIVO`** sin rol admin. |
| PUT | `/api/profesionales/{id}` | ADMIN o propio profesional | Actualización de datos (JSON o multipart con foto). |
| POST | `/api/profesionales/{id}/rechazar-pendiente` | ADMIN | Rechazo de solicitud pendiente: valida contraseña del admin, email al profesional, borrado en cascada. |
| PUT | `/api/profesionales/{id}/verificar-matricula` | ADMIN | Membresía → `INACTIVA`; email de aprobación. |
| GET | `/api/profesionales/membresia/inactiva` | ADMIN | Listado de profesionales con membresía `INACTIVA` (sin UI en el front aún). |
| PUT | `/api/profesionales/{id}/acceso-indefinido` | ADMIN | Membresía → `ACCESO_INDEFINIDO` (sin UI en el front aún). |

### Servicio

- `obtenerProfesionalSesion(email)` en `ProfesionalService` / `ProfesionalServiceImpl`.
- `obtenerParaPresentacion(id, emailSolicitante)` permite auto-consulta sin filtro de catálogo activo.

## Base de datos

- **`init.sql`:** especialidad semilla **`MEDICINA GENERAL`** (no `MEDICO`); geo y sentinel incluidos en el mismo archivo.

## Membresía vs estado (matriz de referencia)

Son conceptos **distintos** en el modelo:

| Concepto | Tabla / campo | Valores ejemplo | Uso principal |
|----------|---------------|-----------------|---------------|
| **Estado de cuenta** | `estado_actual` en usuario | `PENDIENTE`, `ACTIVO`, `BLOQUEADO` | Login, confirmación de email, bloqueos. |
| **Membresía profesional** | `membresia_actual` en profesional | `SIN_VERIFICAR`, `INACTIVA`, `ACTIVA`, `ACCESO_INDEFINIDO` | Ciclo de alta administrativa y acceso al staff. |

### Reglas actuales en código

| Condición | Portal profesional (UI) | Catálogo público `GET /presentacion` |
|-----------|-------------------------|--------------------------------------|
| Email no confirmado (`PENDIENTE`) | No puede login completo | No listado |
| `membresiaActual = SIN_VERIFICAR` + email OK | Login OK; dashboard con aviso; agenda/pacientes deshabilitados | No listado (salvo auto-consulta del propio profesional) |
| Admin verifica matrícula → `INACTIVA` | Dashboard sin aviso `SIN_VERIFICAR`; módulos habilitados en UI | Listado solo si **`estadoActual = ACTIVO`** |
| `membresiaActual = ACTIVA` | *(sin endpoint dedicado aún)* | Misma regla de estado `ACTIVO` |
| `ACCESO_INDEFINIDO` | Endpoint admin existe; sin pantalla | Misma regla de estado `ACTIVO` |

**Pendiente de producto:** definir cuándo pasar a membresía `ACTIVA`, quién lo dispara (admin / pago / job) y si `INACTIVA` debe seguir restringiendo algo en el portal. Ver backlog [`COSAS-POR-HACER.md`](../COSAS-POR-HACER.md) — ítem “Flujo de membresía profesional completo”.

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
4. Navegar dashboard → otra ruta del portal → dashboard: foto y nombre **sin** nuevo skeleton ni requests duplicados (Network tab).
5. Registro profesional: subir foto en paso 3, volver al paso 2 y regresar — preview visible.
6. Catálogo admin / registro: especialidades sin texto crudo `MEDICO`.
