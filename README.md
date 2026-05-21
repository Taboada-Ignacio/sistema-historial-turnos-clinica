# Sistema de Gestión Clínica

Sistema de información para consultorios médicos bajo arquitectura de microservicios: identidades, turnos, historial clínico y panel web (React).

## Arquitectura (resumen)

| Componente | Rol |
|------------|-----|
| **ms-usuarios** | Identidad, roles, JWT, refresh tokens, catálogos de usuarios |
| **api-gateway** | Entrada HTTP (`:8080`), prefijo `/usuarios` → `ms-usuarios` (**requerido** si el front usa `VITE_API_BASE_URL` al gateway) |
| **frontend-clinica** | Portales paciente, profesional y administración |
| **PostgreSQL** | Una base por servicio (*database-per-service*) |

Tecnologías principales: Java 21, Spring Boot 3, Spring Security, JWT, React (Vite), PostgreSQL.

**Backlog y pendientes:** [COSAS-POR-HACER.md](COSAS-POR-HACER.md) (prioridades P0–P3).

### Documentación técnica

| Documento | Contenido |
|-----------|-----------|
| [ms-usuarios/README.md](ms-usuarios/README.md) | BD (init + geo Argentina), direcciones, sentinel `SIN ESPECIFICAR`, verificación email |
| [frontend-clinica/README.md](frontend-clinica/README.md) | Panel admin: catálogos, direcciones, pacientes/profesionales/administradores, verificación de cuenta |
| [docs/API.md](docs/API.md) | Rutas HTTP de `ms-usuarios`, prefijo gateway `/usuarios`, convenciones de acceso |
| [docs/ENTIDADES.md](docs/ENTIDADES.md) | Modelo de datos y relaciones JPA |
| [docs/SEGURIDAD.md](docs/SEGURIDAD.md) | Identidad centralizada en `ms-usuarios`, JWT, contrato con otros MS |
| [docs/RECUPERACION-CONTRASENA.md](docs/RECUPERACION-CONTRASENA.md) | Recuperación de contraseña (30 min), errores en SPA, rutas públicas, anti-doble-submit |
| [docs/CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md](docs/CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md) | Registro profesional (email duplicado en UI), rechazo admin de pendientes con motivo y correo |
| [docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md](docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md) | Admin: consultar/editar/eliminar pacientes y profesionales; búsquedas combinables; unicidad email/DNI |
| [docs/CAMBIOS-ADMIN-ADMINISTRADORES.md](docs/CAMBIOS-ADMIN-ADMINISTRADORES.md) | Admin: listar y ver todos los administradores; editar/eliminar solo la cuenta propia |
| [docs/CAMBIOS-PORTAL-PROFESIONAL-DASHBOARD.md](docs/CAMBIOS-PORTAL-PROFESIONAL-DASHBOARD.md) | Dashboard profesional, `GET /me`, presentación y membresía `SIN_VERIFICAR` |

---

## Arranque rápido local

### Stack completo (recomendado)

Desde la **raíz del repo**:

```bash
docker compose up --build -d
```

El servicio `db-usuarios` aplica, en orden (solo en **volumen nuevo**):

1. `ms-usuarios/init.sql` — esquema y catálogos base  
2. `ms-usuarios/sql/argentina-geo-data.sql` — provincias y localidades de Argentina  
3. `ms-usuarios/sql/03-direcciones-sentinel.sql` — dirección `SIN ESPECIFICAR` por localidad  

Si ya tenías Postgres con datos viejos y no ves provincias/localidades:

```bash
docker compose down -v
docker compose up --build -d
```

(`-v` borra el volumen `db_usuarios_data` y vuelve a ejecutar los scripts.)

### Otras opciones de base de datos

**Solo `ms-usuarios`** (misma semilla, otro compose):

```bash
cd ms-usuarios
docker compose up -d
```

**Infra compartida** (`infra/`, otro Postgres; no incluye geo de usuarios):

```bash
cd infra
docker-compose up -d
```

Detalle: [ms-usuarios/README.md](ms-usuarios/README.md).

### Backend (`ms-usuarios`)

```bash
cd ms-usuarios
mvn spring-boot:run
```

Puerto por defecto del servicio: **8081** (ver `application.yml`). Si accedés vía **API Gateway**, suele usarse **8080** en el front.

### Frontend (`frontend-clinica`)

```bash
cd frontend-clinica
cp .env.example .env
# Editar .env: VITE_API_BASE_URL apuntando al gateway o a ms-usuarios
npm install
npm run dev
```

Variable clave: **`VITE_API_BASE_URL`** (sin barra final), p. ej. `http://localhost:8080` si el front habla con el gateway.

**CORS y credenciales:** el gateway usa **`APP_GATEWAY_CORS_ALLOWED_ORIGIN`** (un solo origen; por defecto `http://localhost:5173`). En **`ms-usuarios`**, **`APP_ALLOWED_ORIGINS`** debe coincidir con esa URL para que login y refresh validen el mismo `Origin`. En despliegue, ambas suelen apuntar a la URL HTTPS pública de la SPA. El `docker-compose.yml` raíz pasa ambas con el mismo default local.

---

## Autenticación y seguridad

### Política de identidad (resumen)

- Un usuario puede tener **varios roles** en base de datos.
- En el **login** se envía el campo **`portal`**: `paciente` | `profesional` | `admin`. El backend exige el rol mínimo (`ROLE_PACIENTE`, `ROLE_PROFESIONAL`, `ROLE_ADMINISTRADOR`) para ese portal.
- El **JWT** incluye el claim **`authorities`**. La autorización fina se aplica en cada endpoint (`@PreAuthorize`, ownership, etc.); el portal en el cliente es solo UX.

### Endpoints relevantes (`ms-usuarios`)

| Método | Ruta | Notas |
|--------|------|--------|
| POST | `/api/auth/login` | Body: `email`, `password`, `portal`. Setea cookie **HttpOnly** `refreshToken` (si corresponde). |
| POST | `/api/auth/refresh` | Renueva access token; usa cookie de refresh; **rotación** del refresh en BD. |
| GET | `/api/profesionales/presentacion` | Catálogo reducido (sin datos sensibles); excluye cuentas con rol administrador y no ACTIVOS. |
| GET | `/api/profesionales/{id}/presentacion` | Ficha pública. |

Listados completos de profesionales con datos sensibles: solo **`ROLE_ADMINISTRADOR`** en las rutas definidas.

### Refresh token

- Almacenamiento en cookie **HttpOnly** + **SameSite=Lax**; flag **Secure** controlado por entorno (`APP_COOKIE_SECURE` / `app.cookie.secure`).
- Tras **cambio de contraseña** (con contraseña actual o con token de recuperación) se revocan refresh tokens del usuario.

### Filtro JWT

Respuestas JSON con códigos de error: **`TOKEN_EXPIRED`**, **`TOKEN_INVALID`**, etc., para que el front distinga expiración vs token inválido.

### Origen (CSRF / navegador)

En login y refresh se valida **Origin** / **Referer** frente a **`app.allowed-origins`** (lista separada por comas). En desarrollo el default incluye `http://localhost:5173`; en producción debe configurarse el dominio real del front (sin depender de localhost).

### Arranque en producción (`StartupChecks`)

Con perfil **`prod`** y **`app.startup.strict=true`** (por defecto), al iniciar se validan entre otras cosas: cookie secure, orígenes, `JWT_SECRET`, conexión a BD y datos mínimos semilla (roles, estados, membresías).

---

## Frontend (React + Vite)

- **Axios** usa **`withCredentials: true`** para enviar la cookie de refresh al mismo API origin.
- Ante **401** en rutas protegidas intenta **`POST .../api/auth/refresh`**, guarda el nuevo access token y **reintenta** la petición una vez.
- URLs del API centralizadas en **`VITE_API_BASE_URL`** (`src/config/env.js`).
- Ver **`frontend-clinica/.env.example`**.

**CORS:** el servidor debe exponer el origen del front y permitir credenciales si login/refresh son cross-origin.

---

## Variables de entorno (referencia)

### `ms-usuarios`

| Variable / propiedad | Descripción |
|----------------------|-------------|
| `SPRING_DATASOURCE_*` | URL, usuario y contraseña PostgreSQL |
| `JWT_SECRET` | Secreto firmado JWT (≥ 32 caracteres; no usar el placeholder de ejemplo en prod) |
| `JWT_EXPIRATION` / `jwt.expiration` | Vida del access token |
| `jwt.refreshExpiration` | Vida del refresh (ms) |
| `APP_COOKIE_SECURE` / `app.cookie.secure` | `true` en producción (HTTPS) |
| `APP_ALLOWED_ORIGINS` / `app.allowed-origins` | Orígenes permitidos para login/refresh |
| `APP_URL` | Base del gateway en links de email hacia la API |
| `APP_FRONTEND_URL` / `app.frontend-url` | Base de la SPA para redirects tras confirmar cuenta por email |
| `app.startup.strict` | Si `false`, relaja validaciones de arranque (no recomendado en prod) |

### `frontend-clinica` (build)

| Variable | Descripción |
|----------|-------------|
| `VITE_API_BASE_URL` | Base URL del gateway o API (sin `/` final) |

---

## Tests (`ms-usuarios`)

Perfil **`test`** y **H2** en memoria (`ms-usuarios/src/test/resources/application-test.yml`):

```bash
cd ms-usuarios && mvn test
```

Cobertura principal: **`JwtUtil`** (claims y expiración), cadena de seguridad con **MockMvc** (rutas públicas vs protegidas, JWT inválido, login sin portal / portal inválido, refresh sin cookie, validación de **Origin**), **login real** con pacientes ACTIVOS creados vía repositorios (`AuthLoginWithUsersIntegrationTest`: token + cookie, `PORTAL_NO_PERMITIDO`, ownership `GET /api/pacientes/{id}`), y **`@PreAuthorize`** en listado de pacientes con **`@WithMockUser`** (`PacientesMethodSecurityIntegrationTest`).

---

## CI (GitHub Actions)

Workflow **`.github/workflows/ms-usuarios-startup-check.yml`**: levanta PostgreSQL, aplica `ms-usuarios/init.sql`, empaqueta el servicio y ejecuta la JVM en modo no-web con perfil `prod` para validar `StartupChecks`. En Docker local del MS se recomienda además `sql/argentina-geo-data.sql` y `sql/03-direcciones-sentinel.sql` (ver [ms-usuarios/README.md](ms-usuarios/README.md)). Para ejecutar también **`mvn test`** en CI, se puede añadir un job con perfil `test` (H2).

---

## Documentación adicional

- **Microservicio usuarios:** [`ms-usuarios/README.md`](ms-usuarios/README.md)
- **Frontend (panel admin, verificación email, recuperación):** [`frontend-clinica/README.md`](frontend-clinica/README.md)
- **Recuperación de contraseña (flujo completo):** [`docs/RECUPERACION-CONTRASENA.md`](docs/RECUPERACION-CONTRASENA.md)
- **Despliegue y cookies:** [`ms-usuarios/README-deploy.md`](ms-usuarios/README-deploy.md)
- **SQL:** [`ms-usuarios/init.sql`](ms-usuarios/init.sql), [`ms-usuarios/sql/`](ms-usuarios/sql/)

---

## Funcionalidades recientes (`ms-usuarios` + front)

### Ubicación y direcciones

- Catálogo **`direcciones`** por localidad; el **usuario** solo guarda **`id_direccion`** (la localidad se deduce de la dirección).
- Valor reservado **`SIN ESPECIFICAR`** en catálogos y en una dirección por localidad (integridad al borrar).
- API: CRUD de direcciones para admin; GET públicos para formularios de registro.

### Panel admin (catálogos)

Ruta base: `/internal/admin/panel/entidades` → listados, **alta** (`…/catalogo/:tipo/nuevo`), edición y baja con confirmación de **contraseña actual**. Pantalla dedicada **Direcciones** (`…/direcciones`).

### Consultar pacientes y profesionales (admin)

- **Pacientes:** `GET /api/pacientes/buscar` — filtros `q`, `idProvincia`, `idLocalidad` (combinables). Detalle, `PUT` y `DELETE` con contraseña admin.
- **Profesionales:** `GET /api/profesionales/buscar` — filtros `q`, `idEspecialidad`, `idProvincia`, `idLocalidad`. Listado con foto de perfil; misma UX de detalle/edición/baja.
- Validación compartida **email/DNI** en alta y edición: `UnicidadUsuarioValidator`.
- Detalle: [docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md](docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md).

### Consultar administradores (admin)

- **`GET /api/administradores`**: listado completo (sin filtros; pocos registros).
- **`GET /api/administradores/me`**: administrador de la sesión (la SPA usa esto para saber qué fila es “la tuya”).
- **Detalle** de cualquier administrador: `GET /api/administradores/{id}`.
- **`PUT` y `DELETE /{id}`**: solo el propio usuario (`esMismoUsuario`); contraseña actual en el front antes de mutar.
- Pantallas: `…/entidades/administradores` (listado), `…/:id` (detalle; otros en gris, solo lectura), `…/:id/editar` (solo propia).
- Detalle: [docs/CAMBIOS-ADMIN-ADMINISTRADORES.md](docs/CAMBIOS-ADMIN-ADMINISTRADORES.md).

### Registro paciente — confirmación y duplicados

- Errores de registro (email, DNI o ambos) en campo **`mensaje`**; la SPA usa `apiErrorMessage`.
- Tras confirmar email: `/registro-exitoso-paciente` → redirect a **login** del paciente.

### Verificación de email

Código de 6 dígitos (3 min) + enlace; endpoints `POST …/confirmar-codigo` por tipo de usuario. Detalle en [ms-usuarios/README.md](ms-usuarios/README.md).

### Recuperación de contraseña

Flujo por correo para paciente, profesional y administrador: solicitud → enlace (**30 min**, un solo uso) → formulario en la SPA → revocación de refresh tokens. Errores de enlace usado/expirado en `/recuperacion-password-error`. El front evita doble `POST` al guardar (`lockRef` + hook compartido). Documentación: [docs/RECUPERACION-CONTRASENA.md](docs/RECUPERACION-CONTRASENA.md).

### Registro profesional — foto

Aviso en el formulario: foto de ámbito profesional, visible para todos los pacientes y usuarios del sistema.

---

## Modelo de datos (`ms-usuarios`)

Herencia **JOINED** (`Usuario` → `Paciente` / `Profesional` / `Administrador`). Catálogos: roles, provincias, localidades, **direcciones**, especialidades, obras sociales, estados, membresías. Diagrama y FK en [docs/ENTIDADES.md](docs/ENTIDADES.md).
