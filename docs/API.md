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

Este documento **complementa** OpenAPI con convenciones de **seguridad**, **cookies** y **portales** que conviene tener en un solo lugar. Detalle de modelo de amenazas y otros MS: [`SEGURIDAD.md`](SEGURIDAD.md).

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
| POST | `/api/auth/solicitar-cambio-password/paciente` | Público | |
| POST | `/api/auth/solicitar-cambio-password/profesional` | Público | |
| GET | `/api/auth/confirmar-cambio-password` | Público | Query: `token`, `tipo` — redirect a SPA. |
| POST | `/api/auth/cambiar-password-con-token/paciente` | Público | |
| POST | `/api/auth/cambiar-password-con-token/profesional` | Público | |

---

## Pacientes (`/api/pacientes`)

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/registro` | Público |
| GET | `/confirmar` | Público | Query `token`; redirect a SPA (`app.frontend-url`). |
| POST | `/reenviar-confirmacion` | Público | Query `email`. |
| GET | `/{id}` | ADMIN o PACIENTE **y** mismo usuario |
| GET | `/` | ADMIN |
| PUT | `/{id}` | ADMIN o PACIENTE **y** mismo usuario |
| DELETE | `/{id}` | ADMIN |

---

## Profesionales (`/api/profesionales`)

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/registro` | Público | `multipart/form-data`: parte `datos` (JSON), opcional `foto`. |
| GET | `/confirmar` | Público | Query `token`; redirect SPA. |
| POST | `/reenviar-confirmacion` | Público | Query `email`. |
| GET | `/` | ADMIN | Sin query: todos. Query **`membresia=<NOMBRE>`** (p. ej. `SIN_VERIFICAR`, `INACTIVA`, `ACTIVA`): filtra por membresía actual; el nombre se normaliza a mayúsculas y debe existir en catálogo → si no existe **404**. `membresia` vacío → **400**. |
| GET | `/presentacion` | ADMIN, PACIENTE o PROFESIONAL | Catálogo reducido. |
| GET | `/{id}/presentacion` | ADMIN, PACIENTE o PROFESIONAL | |
| GET | `/{id}` | ADMIN o PROFESIONAL **y** mismo usuario |
| GET | `/membresia/inactiva` | ADMIN |
| PUT | `/{id}` | ADMIN o PROFESIONAL **y** mismo usuario |
| DELETE | `/{id}` | ADMIN |
| PUT | `/{id}/verificar-matricula` | ADMIN |
| PUT | `/{id}/acceso-indefinido` | ADMIN |

---

## Administradores (`/api/administradores`)

| Método | Ruta | Acceso |
|--------|------|--------|
| POST | `/registro` | Público | Header **`X-System-Key`** obligatorio (valor en config). |
| GET | `/confirmar` | Público | Query `token`; redirect SPA. |
| POST | `/reenviar-confirmacion` | Público | Query `email`. |
| GET | `/` | ADMIN |
| GET | `/{id}` | ADMIN |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |

---

## Catálogos y maestros

Rutas base: **`/api/provincias`**, **`/api/localidades`**, **`/api/especialidades`**, **`/api/obras-sociales`**, **`/api/roles`**.

Patrón habitual:

- **GET** listado y **GET** por id: **públicos** para lectura (JWT filter los ignora en GET; útiles para formularios de registro).
- **POST /registro**, **PUT /{id}**, **DELETE /{id}**: **`ROLE_ADMINISTRADOR`**.

**Localidades:** además **`GET /api/localidades/provincia/{provinciaId}`** (GET público).

---

## Recursos estáticos

| Método | Ruta | Acceso |
|--------|------|--------|
| GET | `/fotosPerfilProfesionales/**` | Público |

---

## Otros microservicios (futuros)

**Turnos**, **historial clínico** u otros MS **no están documentados aquí** hasta existir en el repositorio. Su diseño de API debe alinearse al **contrato de identidad** descrito en [`SEGURIDAD.md`](SEGURIDAD.md) (JWT emitido por `ms-usuarios`, sin duplicar login).

---

## Tests

Comportamiento de seguridad y auth cubierto por tests en `ms-usuarios/src/test/...` — ver README principal del repo.
