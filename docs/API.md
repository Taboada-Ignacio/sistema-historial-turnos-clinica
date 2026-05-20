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
| POST | `/api/auth/solicitar-cambio-password/paciente` | Público | Body `{ "email" }`. Usuario ACTIVO con rol paciente. Invalida tokens previos; nuevo token **30 min**. |
| POST | `/api/auth/solicitar-cambio-password/profesional` | Público | Igual, rol profesional. |
| POST | `/api/auth/solicitar-cambio-password/admin` | Público | Igual, rol administrador. |
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
| POST | `/confirmar-codigo` | Público | Body: `email`, `codigo`. |
| POST | `/reenviar-confirmacion` | Público | Query `email`. |
| GET | `/` | ADMIN | Sin query: todos. Query **`membresia=<NOMBRE>`** (p. ej. `SIN_VERIFICAR`, `INACTIVA`, `ACTIVA`): filtra por membresía actual; el nombre se normaliza a mayúsculas y debe existir en catálogo → si no existe **404**. `membresia` vacío → **400**. |
| GET | `/me` | PROFESIONAL | Perfil del usuario autenticado (`membresiaActual`, especialidad, foto, etc.). |
| GET | `/presentacion` | ADMIN, PACIENTE o PROFESIONAL | Catálogo reducido (solo profesionales ACTIVO). |
| GET | `/{id}/presentacion` | ADMIN, PACIENTE o PROFESIONAL | Ficha pública; el **propio** profesional puede consultar su ficha aunque no esté ACTIVO (p. ej. `SIN_VERIFICAR`). |
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
| POST | `/confirmar-codigo` | Público | Body: `email`, `codigo`. |
| POST | `/reenviar-confirmacion` | Público | Query `email`. |
| GET | `/` | ADMIN |
| GET | `/{id}` | ADMIN |
| PUT | `/{id}` | ADMIN |
| DELETE | `/{id}` | ADMIN |

---

## Catálogos y maestros

Rutas base: **`/api/provincias`**, **`/api/localidades`**, **`/api/direcciones`**, **`/api/especialidades`**, **`/api/obras-sociales`**, **`/api/roles`**.

Patrón habitual:

- **GET** listado y **GET** por id: **públicos** para lectura (JWT filter los ignora en GET; útiles para formularios de registro).
- **POST /registro**, **PUT /{id}**, **DELETE /{id}**: **`ROLE_ADMINISTRADOR`**.

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

Los **registros** de paciente, profesional y administrador incluyen **`direccion`** (texto, obligatorio, máx. 500 caracteres) junto con **`idLocalidad`**. El backend **crea o reutiliza** una fila en `direcciones` y asigna **`id_direccion`** al usuario (no hay `id_localidad` en `usuarios`).

**Seguridad admin (panel):** `POST /api/seguridad/verificar-password-actual` — body `{ "password": "..." }` antes de mutaciones sensibles en el front.

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
