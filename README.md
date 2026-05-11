# Sistema de Gestión Clínica

Sistema de información para consultorios médicos bajo arquitectura de microservicios: identidades, turnos, historial clínico y panel web (React).

## Arquitectura (resumen)

| Componente | Rol |
|------------|-----|
| **ms-usuarios** | Identidad, roles, JWT, refresh tokens, catálogos de usuarios |
| **api-gateway** | Entrada HTTP / enrutamiento (si aplica) |
| **frontend-clinica** | Portales paciente, profesional y administración |
| **PostgreSQL** | Una base por servicio (*database-per-service*) |

Tecnologías principales: Java 21, Spring Boot 3, Spring Security, JWT, React (Vite), PostgreSQL.

**Backlog y pendientes:** [COSAS-POR-HACER.md](COSAS-POR-HACER.md) (prioridades P0–P3).

### Documentación técnica

| Documento | Contenido |
|-----------|-----------|
| [docs/API.md](docs/API.md) | Rutas HTTP de `ms-usuarios`, prefijo gateway `/usuarios`, convenciones de acceso |
| [docs/ENTIDADES.md](docs/ENTIDADES.md) | Modelo de datos y relaciones JPA |
| [docs/SEGURIDAD.md](docs/SEGURIDAD.md) | Identidad centralizada en `ms-usuarios`, JWT, contrato con otros MS |

---

## Arranque rápido local

### Base de datos (Docker)

```bash
cd infra
docker-compose up -d
```

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
- Tras **cambio de contraseña** se revocan refresh tokens del usuario.

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

Workflow **`.github/workflows/ms-usuarios-startup-check.yml`**: levanta PostgreSQL, aplica `ms-usuarios/init.sql`, empaqueta el servicio y ejecuta la JVM en modo no-web con perfil `prod` para validar `StartupChecks`. Para ejecutar también **`mvn test`** en CI, se puede añadir un job que use el perfil `test` y omita Postgres si solo corre los tests unitarios/integración ligeros.

---

## Documentación adicional

- **Despliegue y cookies (detalle):** [`ms-usuarios/README-deploy.md`](ms-usuarios/README-deploy.md)
- **SQL inicial / semilla:** [`ms-usuarios/init.sql`](ms-usuarios/init.sql)

---

## Modelo de datos (`ms-usuarios`)

Herencia **JOINED** (`Usuario` → `Paciente` / `Profesional` / `Administrador`). Catálogos: roles, provincias, localidades, especialidades, obras sociales, estados, membresías.
