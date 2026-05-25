# API — Microservicio de usuarios (`ms-usuarios`)

Referencia de rutas HTTP expuestas por **`ms-usuarios`**. El front y Postman suelen llamar vía **API Gateway**: prefijo **`/usuarios`** (se elimina al llegar al microservicio).

| Entorno | Base útil |
|---------|-----------|
| Gateway | `http://localhost:8080/usuarios` |
| MS directo | `http://localhost:8081` |

Ejemplo: `GET http://localhost:8080/usuarios/api/provincias` → en el MS llega como `GET /api/provincias`.

---

## OpenAPI / Swagger

La lista exhaustiva de esquemas (DTOs), códigos y parámetros está en **OpenAPI**:

- **Gateway (recomendado):** Swagger UI del gateway con spec agregado de usuarios — ver [`api-gateway/src/main/resources/application.yml`](../api-gateway/src/main/resources/application.yml) (`springdoc.swagger-ui.urls`).
- **MS directo:** `http://localhost:8081/swagger-ui.html` (ruta puede variar según `springdoc`).

Este documento **complementa** OpenAPI con convenciones de **seguridad**, **cookies** y **portales** que conviene tener en un solo lugar. Detalle de modelo de amenazas y otros MS: [`SEGURIDAD.md`](SEGURIDAD.md). Enrutamiento gateway: [`API-GATEWAY.md`](API-GATEWAY.md).

---

## Convenciones de autenticación en tablas

| Valor | Significado |
|-------|-------------|
| **Público** | Sin `Authorization`; el filtro JWT no exige Bearer (y/o la cadena `SecurityFilterChain` permite la ruta). |
| **JWT** | Header `Authorization: Bearer <access_token>`. |
| **ADMIN** | Autoridad `ROLE_ADMINISTRADOR` (`@PreAuthorize`). |
| **Propiedad** | Reglas con `@authorizationRules.esMismoUsuario(#id)` (ver [`SEGURIDAD.md`](SEGURIDAD.md)). |

---

## Auth (`/api/auth`)

| Método | Ruta (MS) | Acceso | Notas |
|--------|-----------|--------|--------|
| POST | `/api/auth/login` | Público | Body: `email`, `password`, **`portal`**: `paciente` \| `profesional` \| `admin`. Set-Cookie **`refreshToken`** (HttpOnly). Respuesta JSON: **`token`** (JWT), **`email`**, **`rol`**. Origen validado si `app.allowed-origins` no está vacío. |
| POST | `/api/auth/refresh` | Público (cookie) | Cookie **`refreshToken`** obligatoria; validación de origen igual que login. |
| POST | `/api/auth/cambiar-password` | Público | Email + contraseña actual + nueva (revoca refresh tokens). |
| POST | `/api/auth/solicitar-cambio-password/paciente` | Público | Body `{ "email" }`. Según estado: **PENDIENTE** → correo confirmación (código + enlace, **72 h**); **SIN_CONTRASENA** → correo activación (solo enlace, **72 h**); **ACTIVO** → recuperación de contraseña (**72 h**); **BLOQUEADO** → **400** con mensaje explícito. |
| POST | `/api/auth/reenviar-acceso-paciente` | Público | Body `{ "email" }`. Misma lógica por estado que solicitar cambio (sin rama recuperación para ACTIVO: mensaje para usar «olvidé contraseña»). |
| GET | `/api/auth/confirmar-activacion-paciente` | Público | Query `token`. Redirect SPA → `/activar-cuenta-paciente?token=`. |
| GET | `/api/auth/datos-activacion-paciente` | Público | Query `token`. Datos del paciente en **SIN_CONTRASENA** para pantalla de activación. |
| POST | `/api/auth/establecer-password-inicial/paciente` | Público | Body `{ "token", "passwordNueva" }`. Activa cuenta (**SIN_CONTRASENA** → **ACTIVO**). |
| POST | `/api/auth/solicitar-cambio-password/profesional` | Público | Body `{ "email" }`. Usuario ACTIVO con rol profesional. Token **72 h**. |
| POST | `/api/auth/solicitar-cambio-password/admin` | Público | Igual, rol administrador. Token **72 h**. |
| GET | `/api/auth/confirmar-cambio-password` | Público | Query: `token`, `tipo` (`paciente` \| `profesional` \| `admin`). Redirect SPA: éxito → `/cambiar-password/{tipo}?token=`; error → `/recuperacion-password-error?tipo=&motivo=invalido\|expirado`. |
| POST | `/api/auth/cambiar-password-con-token/paciente` | Público | Body `{ "token", "passwordNueva" }`. Un solo uso del token; revoca refresh tokens. |
| POST | `/api/auth/cambiar-password-con-token/profesional` | Público | Igual. |
| POST | `/api/auth/cambiar-password-con-token/admin` | Público | Igual. |

Flujo detallado, front y anti-doble-submit: [`RECUPERACION-CONTRASENA.md`](RECUPERACION-CONTRASENA.md).

---

## Pacientes (`/api/pacientes`)

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/registro` | Público |
| GET | `/confirmar` | Público | Query `token`; redirect a SPA (`app.frontend-url`). |
| POST | `/confirmar-codigo` | Público | Body: `email`, `codigo` (6 dígitos). |
| POST | `/reenviar-confirmacion` | Público | Query `email`. No aplica si usuario ya `ACTIVO`. |
| GET | `/` | ADMIN | Listado completo. |
| GET | `/buscar` | ADMIN | Query opcionales: `q`, `idProvincia`, `idLocalidad` (localidad exige provincia). Al menos `q` o `idProvincia`. Respuesta: `{ total, pacientes[], criteriosAplicados }` (máx. 500). Ver [`CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md`](CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md). |
| GET | `/me` | PACIENTE | Perfil de sesión del portal paciente. Respuesta `PacientePortalSesionDTO` con `perfilEditable`, `tipoCuenta` (`PACIENTE` o `PROFESIONAL_EN_PORTAL_PACIENTE`). Profesional con rol paciente: UI limitada, sin edición vía este endpoint. Ver [`CAMBIOS-PORTAL-PACIENTE-PERFIL.md`](CAMBIOS-PORTAL-PACIENTE-PERFIL.md). |
| GET | `/{id}` | ADMIN o PACIENTE **y** mismo usuario |
| PUT | `/{id}` | ADMIN o PACIENTE **y** mismo usuario | Unicidad email/DNI al editar (`UnicidadUsuarioValidator`). |
| DELETE | `/{id}` | ADMIN |

---

## Profesionales (`/api/profesionales`)

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/registro` | Público | `multipart/form-data`: parte `datos` (JSON), opcional `foto`. Email/DNI duplicados → **400** `{ "mensaje": "..." }`. |
| GET | `/confirmar` | Público | Query `token`; redirect SPA. |
| POST | `/confirmar-codigo` | Público | Body: `email`, `codigo`. |
| POST | `/reenviar-confirmacion` | Público | Query `email`. |
| GET | `/` | ADMIN | Sin query: todos. Query **`membresia=<NOMBRE>`** (p. ej. `SIN_VERIFICAR`, `INACTIVA`, `ACTIVA`): filtra por membresía actual; el nombre se normaliza a mayúsculas y debe existir en catálogo → si no existe **404**. `membresia` vacío → **400**. |
| GET | `/buscar` | ADMIN | Query opcionales: `q`, `idEspecialidad`, `idProvincia`, `idLocalidad` (localidad exige provincia). Al menos `q`, `idEspecialidad` o `idProvincia`. Respuesta: `{ total, profesionales[], criteriosAplicados }` con `fotoPerfil` en cada ítem (máx. 500). |
| GET | `/me` | PROFESIONAL | Perfil del usuario autenticado (`membresiaActual`, especialidad, foto, etc.). |
| GET | `/me/pacientes/buscar` | PROFESIONAL | Query **`q`** obligatorio: apellido, nombre o DNI. Filtra **pacientes, profesionales y administradores** de la **misma provincia y localidad** que el profesional. **Pacientes y profesionales con estado `BLOQUEADO` quedan excluidos**; administradores sin filtro de estado. Membresía del profesional logueado `SIN_VERIFICAR` → **400**. Respuesta: `{ total, personas[], criteriosAplicados }` con `tipoCuenta` (máx. 500). |
| GET | `/me/pacientes/buscar-general` | PROFESIONAL | Igual criterio de personas y exclusión de bloqueados, pero **`q`**, **`idProvincia`** e **`idLocalidad`** obligatorios. |
| GET | `/me/pacientes/{id}` | PROFESIONAL | Detalle en una zona: sin query usa la ciudad del profesional; con **`idProvincia`** + **`idLocalidad`** valida la zona de búsqueda general. Paciente/profesional **BLOQUEADO** → **404**. Campos: nombre, apellido, DNI, teléfono, fecha de nacimiento, dirección, localidad, provincia, `tipoCuenta`. Fuera de zona → **404**. |
| POST | `/me/pacientes` | PROFESIONAL | Alta de paciente **sin contraseña** (`SIN_CONTRASENA`). Body: datos de registro (sin password). Envía correo de activación (**72 h**). Respuesta **201**: `{ idUsuario, mensaje }`. Membresía `SIN_VERIFICAR` → **400**. |
| GET | `/presentacion` | **Público** | Catálogo reducido (profesionales con estado ACTIVO, sin rol admin). |
| GET | `/{id}/presentacion` | **Público** (ficha en catálogo) | JWT opcional: el **propio** profesional puede consultar su ficha aunque no esté ACTIVO (p. ej. `SIN_VERIFICAR`). |
| GET | `/{id}` | ADMIN o PROFESIONAL **y** mismo usuario |
| GET | `/membresia/inactiva` | ADMIN |
| PUT | `/{id}` | ADMIN o PROFESIONAL **y** mismo usuario | JSON o multipart con foto; unicidad email/DNI, matrícula única, `idEspecialidad`. |
| DELETE | `/{id}` | ADMIN |
| PUT | `/{id}/verificar-matricula` | ADMIN | Membresía → `INACTIVA`; email de aprobación. |
| PUT | `/{id}/acceso-indefinido` | ADMIN |
| POST | `/{id}/rechazar-pendiente` | ADMIN | Body: `motivo` (10–1000 chars), `password` (admin actual). Solo **`SIN_VERIFICAR`**: email de rechazo (síncrono) y borrado físico del profesional. Contraseña incorrecta → **400** `mensaje`. Ver [`CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md`](CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md). |

---

## Administradores (`/api/administradores`)

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/registro` | Público | Header **`X-System-Key`** obligatorio (valor en config). |
| GET | `/confirmar` | Público | Query `token`; redirect SPA. |
| POST | `/confirmar-codigo` | Público | Body: `email`, `codigo`. |
| POST | `/reenviar-confirmacion` | Público | Query `email`. |
| GET | `/me` | ADMIN | Perfil del administrador autenticado (email del JWT). |
| GET | `/` | ADMIN | Listado completo (orden apellido, nombre). |
| GET | `/{id}` | ADMIN | Detalle; `AdministradorResponseDTO` incluye `idProvincia`, `idLocalidad`. |
| PUT | `/{id}` | ADMIN **solo si** `id` = `idUsuario` del JWT (`esMismoUsuario`) | Body: `AdministradorUpdateDTO` (+ `direccion`, `idLocalidad`). |
| DELETE | `/{id}` | ADMIN **solo si** `id` = `idUsuario` del JWT | Borrado físico tras limpiar tokens e historial de estados. |

Panel SPA: listado y detalle de cualquier admin; edición/baja solo de la propia cuenta. Ver [`CAMBIOS-ADMIN-ADMINISTRADORES.md`](CAMBIOS-ADMIN-ADMINISTRADORES.md).

---

## Catálogos y maestros

Rutas base: **`/api/provincias`**, **`/api/localidades`**, **`/api/direcciones`**, **`/api/especialidades`**, **`/api/obras-sociales`**, **`/api/roles`**, **`/api/estados`**.

Patrón habitual:

- **GET** listado y **GET** por id: **públicos** para lectura (JWT filter los ignora en GET; útiles para formularios de registro).
- **POST /registro**, **PUT /{id}**, **DELETE /{id}**: **`ROLE_ADMINISTRADOR`** (no aplica a roles ni estados).

**Roles y estados (solo lectura, GET público):**

| Método | Ruta | Notas |
|--------|------|--------|
| GET | `/api/roles`, `/api/roles/{id}` | Catálogo de roles. |
| GET | `/api/estados`, `/api/estados/{id}` | Estados de cuenta (`PENDIENTE`, `ACTIVO`, `BLOQUEADO`). Sin alta/edición/baja por API. |

**Localidades (listado con filtros opcionales, GET público):**

| Método | Ruta | Query params | Notas |
|--------|------|--------------|--------|
| GET | `/api/localidades` | `provinciaId`, `nombre` | Sin params: todas. `provinciaId`: solo esa provincia (404 si no existe). `nombre`: búsqueda parcial (case-insensitive). Combinables. Orden: provincia, localidad. |
| GET | `/api/localidades/provincia/{provinciaId}` | `nombre` (opc.) | Alias de `?provinciaId=`; misma lógica de búsqueda. |

**Direcciones (catálogo por localidad):**

| Método | Ruta | Acceso | Notas |
|--------|------|--------|--------|
| GET | `/api/direcciones/localidad/{idLocalidad}` | Público | Listado en esa localidad. |
| GET | `/api/direcciones/{id}` | Público | Detalle. |
| POST | `/api/direcciones/registro` | ADMIN | Body: `nombre`, `idLocalidad`. Prohibido nombre `SIN ESPECIFICAR`. |
| PUT | `/api/direcciones/{id}` | ADMIN | Solo `nombre` (no cambia localidad). |
| DELETE | `/api/direcciones/{id}` | ADMIN | Usuarios → dirección `SIN ESPECIFICAR` de esa localidad; luego borra la fila. |

Los **registros** de paciente, profesional y administrador incluyen **`sexo`** (`MASCULINO` o `FEMENINO`, obligatorio en altas y ediciones) y **`direccion`** (texto, obligatorio, máx. 500 caracteres) junto con **`idLocalidad`**. El backend **crea o reutiliza** una fila en `direcciones` y asigna **`id_direccion`** al usuario (no hay `id_localidad` en `usuarios`).

**Seguridad admin (panel):** `POST /api/seguridad/verificar-password-actual` — body `{ "password": "..." }` antes de mutaciones sensibles en el front.

---

## Recursos estáticos

| Método | Ruta | Acceso |
|--------|------|--------|
| GET | `/api/profesionales/fotos/public/{fileName}` | **Público** | Solo si la foto pertenece a un profesional visible en el catálogo (`esFotoVisibleEnCatalogoPublico`). |
| GET | `/api/profesionales/fotos/{fileName}` | `ROLE_PROFESIONAL` o `ROLE_ADMINISTRADOR` (JWT). Solo nombres `{uuid}.webp`. Máx. **2 MB** en subida. |

---

## Otros microservicios (futuros)

**Turnos**, **historial clínico** u otros MS **no están documentados aquí** hasta existir en el repositorio. Su diseño de API debe alinearse al **contrato de identidad** descrito en [`SEGURIDAD.md`](SEGURIDAD.md) (JWT emitido por `ms-usuarios`, sin duplicar login).

---

## Tests

Comportamiento de seguridad y auth cubierto por tests en `ms-usuarios/src/test/...` — ver README principal del repo.
