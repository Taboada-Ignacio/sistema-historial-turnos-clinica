# Microservicio de usuarios (`ms-usuarios`)

Identidad, roles, JWT, refresh tokens, catálogos maestros, direcciones y confirmación de cuenta por email.

**Documentación relacionada**

| Archivo | Contenido |
|---------|-----------|
| [../docs/ARQUITECTURA.md](../docs/ARQUITECTURA.md) | Estructura del repo y Docker |
| [../docs/API-GATEWAY.md](../docs/API-GATEWAY.md) | Gateway HTTP |
| [README-deploy.md](README-deploy.md) | Cookies, CORS, variables de despliegue |
| [../docs/API.md](../docs/API.md) | Rutas HTTP y acceso |
| [../docs/ENTIDADES.md](../docs/ENTIDADES.md) | Modelo JPA |
| [../docs/SEGURIDAD.md](../docs/SEGURIDAD.md) | JWT, filtros, `@PreAuthorize` |
| [../docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md](../docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md) | Búsqueda admin, CRUD pacientes/profesionales, unicidad |
| [../docs/CAMBIOS-ADMIN-ADMINISTRADORES.md](../docs/CAMBIOS-ADMIN-ADMINISTRADORES.md) | Listado admin de administradores; PUT/DELETE solo cuenta propia |
| [init.sql](init.sql) | Esquema, semilla, geo Argentina y direcciones sentinel (único script) |

---

## Estructura del código (Java)

Paquete raíz: `com.clinica.usuarios`.

### Controladores REST

| Clase | Prefijo | Responsabilidad |
|-------|---------|-----------------|
| `AuthController` | `/api/auth` | Login, refresh, cambio/recuperación contraseña |
| `PacienteController` | `/api/pacientes` | Registro, confirmación, CRUD, búsqueda admin |
| `ProfesionalController` | `/api/profesionales` | Registro multipart, membresía, búsqueda, `/me`, presentación, rechazo |
| `AdministradorController` | `/api/administradores` | Registro con `X-System-Key`, CRUD, `/me` |
| `CuentaSeguridadController` | `/api/seguridad` | Verificación contraseña actual (panel admin) |
| `ProfesionalFotoController` | `/api/profesionales/fotos` | GET foto WebP (JWT profesional o admin) |
| `ProvinciaController`, `LocalidadController`, `DireccionController` | `/api/...` | Catálogos geo |
| `EspecialidadController`, `ObraSocialController` | `/api/...` | Catálogos clínicos |
| `RolController`, `EstadoController` | `/api/...` | Solo lectura |

### Servicios principales

| Servicio | Rol |
|----------|-----|
| `AccountActivationService` | Confirmación email (token + código) |
| `VerificationTokenService` | Tokens de verificación y recuperación |
| `RefreshTokenService` | Persistencia, rotación y revocación de refresh |
| `EmailService` | Envío SMTP |
| `ProfesionalFotoStorageService` | Guardado WebP en disco, validación tamaño/nombre |
| `PacienteService`, `ProfesionalService`, `AdministradorService` | CRUD y reglas de negocio por tipo |
| `DireccionService`, `LocalidadService`, `ProvinciaService` | Catálogos geo y sentinel |
| Catálogos (`Especialidad`, `ObraSocial`, `Rol`, `Estado`) | CRUD o lectura según entidad |

### Seguridad y configuración

| Clase | Rol |
|-------|-----|
| `SecurityConfig` | Cadena stateless, rutas públicas |
| `JwtAuthFilter`, `JwtUtil` | Validación Bearer |
| `PublicRequestPaths` | Bypass JWT en registro/confirmación/auth |
| `AuthorizationRules` | Ownership `esMismoUsuario(#id)` |
| `UserDetailsServiceImpl` | Carga por email; rechaza `PENDIENTE` |
| `StartupChecks` | Validaciones en perfil `prod` |

### Búsquedas admin

- `PacienteSpecifications` + `GET /api/pacientes/buscar`
- `ProfesionalSpecifications` + `GET /api/profesionales/buscar`

---

## Correo electrónico (SMTP)

Configuración en `application.yml` → `spring.mail`:

| Variable | Descripción |
|----------|-------------|
| `MAIL_USERNAME` | Usuario SMTP |
| `MAIL_PASSWORD` | Contraseña o app password |

Usado para: confirmación de cuenta (código + enlace), recuperación de contraseña, aprobación/rechazo de profesional.

Links en correos:

- **`APP_URL`** — base del gateway (enlace que apunta al API, p. ej. confirmar token).
- **`APP_FRONTEND_URL`** — base SPA para redirects HTTP desde `GET …/confirmar`.

---

## Fotos de perfil profesional

| Propiedad / env | Default | Notas |
|-----------------|---------|--------|
| `PROFESIONAL_FOTO_DIR` | `fotosPerfilProfesionales` | En Docker raíz: volumen `profesional_fotos_data` → `/app/fotosPerfilProfesionales` |
| `app.profesional-foto.max-size-bytes` | 2097152 (2 MB) | Alineado con `multipart.max-file-size` |

- Subida en registro/edición profesional: **multipart**, conversión a **WebP** en servidor.
- Lectura: **`GET /api/profesionales/fotos/{fileName}`** — requiere JWT `ROLE_PROFESIONAL` o `ROLE_ADMINISTRADOR`.
- Vía gateway: `GET /usuarios/api/profesionales/fotos/{fileName}`.

---

## Registro de administrador (bootstrap)

`POST /api/administradores/registro` es **público** pero exige header **`X-System-Key`** con el valor de **`system.registration.secret`** (`ADMIN_REGISTRATION_SECRET` en env).

Pantalla SPA: `/internal/admin/bootstrap-setup` (`AdminRegisterSecret.jsx`).

---

## Arranque local

### Con Docker (recomendado)

```bash
cd ms-usuarios
docker compose up -d
```

PostgreSQL en **5432**; el servicio en **8081**. En volumen **nuevo**, Docker ejecuta un solo script:

| Archivo | Contenido |
|---------|-----------|
| `init.sql` | Tablas (incl. `refresh_tokens`), catálogos base, sentinel `SIN ESPECIFICAR`, 24 provincias argentinas, localidades principales y dirección `SIN ESPECIFICAR` por localidad |

**Importante:** `docker-entrypoint-initdb.d` solo corre en volumen vacío. Si cambiás el esquema en desarrollo:

```bash
docker compose down -v
docker compose up -d
```

### Sin Docker

```bash
mvn spring-boot:run
```

Configuración en `src/main/resources/application.yml`. Con `spring.jpa.hibernate.ddl-auto: update`, Hibernate puede ajustar el esquema menor, pero la **semilla completa** requiere `init.sql` en Postgres (o recrear el volumen Docker).

---

## Modelo de ubicación (usuario ↔ dirección)

- Tabla **`direcciones`**: `nombre` + `id_localidad` (único por par).
- **`usuarios`** referencia solo **`id_direccion`** (no hay `id_localidad` en usuario).
- La localidad del usuario se obtiene por join: `usuario.direccion.localidad`.

### Registro de usuarios (paciente / profesional / administrador)

El body incluye **`idLocalidad`** y texto **`direccion`**. El servicio **crea o reutiliza** una fila en `direcciones` con ese nombre en esa localidad y asigna su id al usuario.

**Paciente y profesional (alta):** email y DNI únicos a nivel `usuarios` vía **`UnicidadUsuarioValidator`**. Se evalúan **ambos** campos antes de responder; mensajes distintos si solo email, solo DNI, o ambos. Duplicado → **400** `{ "mensaje": "..." }`.

**Actualización (admin o propio usuario):** misma validación excluyendo el `idUsuario` editado; mensajes orientados al administrador en edición.

Ver [`docs/CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md`](../docs/CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md) y [`docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md`](../docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md).

### Rechazo de profesional pendiente (admin)

`POST /api/profesionales/{id}/rechazar-pendiente` — body `{ "motivo", "password" }`. Solo membresía **`SIN_VERIFICAR`**: valida contraseña del admin autenticado, envía correo de no aprobación (síncrono) y elimina tokens, historiales, foto y registro. Detalle en el mismo doc anterior.

### Valor reservado `SIN ESPECIFICAR`

Constante: `CatalogoSentinelConstants.SIN_ESPECIFICAR` (`"SIN ESPECIFICAR"`).

| Ámbito | Uso |
|--------|-----|
| Especialidades, obras sociales, provincias, localidades, direcciones | Fila reservada del sistema; **no** se puede crear/editar manualmente con ese nombre (admin). |
| Al crear **localidad** | Se crea automáticamente la dirección sentinel de esa localidad. |
| Al **eliminar** una dirección usada por usuarios | Los usuarios pasan a la dirección `SIN ESPECIFICAR` de **esa misma** localidad; luego se borra la fila. |
| Al eliminar otros catálogos | Los FK afectados se reasignan al sentinel del catálogo correspondiente (comportamiento previo). |

Ya **no** existe el valor `SIN CARGAR` en la semilla.

---

## API de direcciones (`/api/direcciones`)

| Método | Ruta | Acceso | Notas |
|--------|------|--------|--------|
| GET | `/localidad/{idLocalidad}` | Público | Listado para formularios y panel admin |
| GET | `/{id}` | Público | Detalle |
| POST | `/registro` | `ROLE_ADMINISTRADOR` | Body: `nombre`, `idLocalidad` |
| PUT | `/{id}` | `ROLE_ADMINISTRADOR` | Solo **`nombre`**; no cambia localidad |
| DELETE | `/{id}` | `ROLE_ADMINISTRADOR` | Reasigna usuarios al sentinel y elimina |

---

## API de catálogos (CRUD admin)

Rutas: `/api/provincias`, `/api/localidades`, `/api/especialidades`, `/api/obras-sociales`, `/api/roles`.

- **GET** (listado e id): públicos (formularios de registro).
- **POST /registro**, **PUT /{id}**, **DELETE /{id}**: `ROLE_ADMINISTRADOR` (roles: solo lectura en API).

**Localidades:**

- `GET /api/localidades` — query opcionales: `provinciaId`, `nombre` (búsqueda parcial; reutilizable en admin y formularios).
- `GET /api/localidades/provincia/{idProvincia}` — alias; admite `?nombre=`.

Al registrar una localidad, el backend crea la dirección sentinel asociada.

---

## Verificación de email

Tras el registro (estado `PENDIENTE`), se envía un correo con:

- Código de **6 dígitos** (válido **3 minutos**).
- Enlace con **token** (`GET …/confirmar?token=` → redirect a la SPA).

Endpoints adicionales:

| Método | Ruta | Descripción |
|--------|------|-------------|
| POST | `/api/pacientes/confirmar-codigo` | Body: `email`, `codigo` |
| POST | `/api/profesionales/confirmar-codigo` | Igual |
| POST | `/api/administradores/confirmar-codigo` | Igual |

Tabla `tokens_confirmacion`: columnas `token`, `codigo`, `fecha_expiracion`, `id_usuario`.

El reenvío de confirmación **no** se permite si el usuario ya está `ACTIVO`.

Redirects post-confirmación (SPA, `app.frontend-url`):

- Paciente → `/registro-exitoso-paciente` (la SPA redirige al **login** del paciente)
- Profesional → `/aprobacion-pendiente`
- Administrador → `/registro-exitoso-admin` (la SPA redirige al login admin)

---

## Recuperación de contraseña

Flujo en `AuthController` (sin JWT en el cliente de recuperación):

| Paso | Endpoint | Notas |
|------|----------|--------|
| 1 | `POST /api/auth/solicitar-cambio-password/{paciente\|profesional\|admin}` | Body `{ "email" }`. Token nuevo en `tokens_confirmacion`, **30 min**. |
| 2 | `GET /api/auth/confirmar-cambio-password?token=&tipo=` | Redirect a la SPA o a `/recuperacion-password-error`. |
| 3 | `POST /api/auth/cambiar-password-con-token/{portal}` | Body `{ "token", "passwordNueva" }`. Borra el token y revoca refresh tokens. |

Variables de enlace en el correo:

- `APP_URL` — base del gateway (ej. `http://localhost:8080`) para el link del mail.
- `app.frontend-url` — base de la SPA para redirects.

Seguridad: `SecurityConfig` + `PublicRequestPaths` + omisión en `JwtAuthFilter`. Documentación ampliada: [`docs/RECUPERACION-CONTRASENA.md`](../docs/RECUPERACION-CONTRASENA.md).

---

## Seguridad admin: contraseña actual

`POST /api/seguridad/verificar-password-actual` — el panel admin lo usa antes de mutaciones sensibles (alta/edición de catálogos, direcciones, pacientes, profesionales y la propia cuenta de administrador).

---

## Búsqueda admin de pacientes y profesionales

| Método | Ruta | Criterios (al menos uno obligatorio según tabla) |
|--------|------|--------------------------------------------------|
| GET | `/api/pacientes/buscar` | `q` **o** `idProvincia`; opcional `idLocalidad` (con provincia) |
| GET | `/api/profesionales/buscar` | `q` **o** `idEspecialidad` **o** `idProvincia`; opcional `idLocalidad` (con provincia) |

Respuesta: `{ total, pacientes|profesionales[], criteriosAplicados }` (máximo **500** resultados).

- **Pacientes:** `PacienteSpecifications`, `PacienteListadoDTO`.
- **Profesionales:** `ProfesionalSpecifications`, `ProfesionalListadoDTO` (incluye `fotoPerfil`, `especialidad`).

`PUT` y `DELETE` por id: solo **`ROLE_ADMINISTRADOR`** (salvo `PUT` del propio paciente/profesional según reglas existentes).

Documentación de modos de búsqueda y pantallas SPA: [`docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md`](../docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md).

---

## Consulta de administradores (panel admin)

| Método | Ruta | Acceso |
|--------|------|--------|
| GET | `/api/administradores/me` | `ROLE_ADMINISTRADOR` — perfil del JWT (email) |
| GET | `/api/administradores` | `ROLE_ADMINISTRADOR` — listado completo, orden apellido/nombre |
| GET | `/api/administradores/{id}` | `ROLE_ADMINISTRADOR` — detalle con `idProvincia`, `idLocalidad` |
| PUT | `/api/administradores/{id}` | `ROLE_ADMINISTRADOR` **y** `@authorizationRules.esMismoUsuario(#id)` |
| DELETE | `/api/administradores/{id}` | Igual que PUT |

- Detalle: `AdministradorRepository.findWithUbicacionById`.
- Actualización: `UnicidadUsuarioValidator.validarActualizacion`, dirección/localidad como paciente (`AdministradorUpdateDTO.direccion`).
- Eliminación: borra verification tokens, refresh tokens e historial de estados antes del delete.

Documentación y UX del front: [`docs/CAMBIOS-ADMIN-ADMINISTRADORES.md`](../docs/CAMBIOS-ADMIN-ADMINISTRADORES.md).

---

## Base de datos

El proyecto **no está en producción**: un único **[init.sql](init.sql)** define esquema y datos iniciales. No hay scripts de migración incrementales.

### Volumen nuevo (Docker)

```bash
docker compose up -d   # desde ms-usuarios/ o raíz del repo
```

Monta `init.sql` en `docker-entrypoint-initdb.d/init.sql`.

### Cambiar el esquema en desarrollo

Recrear el volumen (borra datos locales):

```bash
docker compose down -v
docker compose up --build -d
```

## Tests

```bash
mvn test
```

Perfil `test` con H2 (`src/test/resources/application-test.yml`).

| Clase de test | Cobertura |
|---------------|-----------|
| `JwtUtilTest` | Claims, firma, expiración |
| `SecurityAndAuthIntegrationTest` | MockMvc: rutas públicas vs protegidas, JWT inválido |
| `AuthLoginOriginIntegrationTest` | Validación Origin/Referer en login |
| `AuthLoginWithUsersIntegrationTest` | Login real, cookie refresh, `PORTAL_NO_PERMITIDO`, ownership paciente |
| `PacientesMethodSecurityIntegrationTest` | `@PreAuthorize` en listado pacientes |
| `DireccionServiceImplTest` | Borrado con reasignación sentinel |
| `LocalidadServiceImplTest` | Alta localidad + dirección sentinel |
| `DireccionTextoNormalizerTest` | Normalización texto dirección |
| `MsUsuariosApplicationTests` | Contexto Spring |

---

## CI

Workflow `.github/workflows/ms-usuarios-startup-check.yml`: Postgres + `init.sql` + arranque con perfil `prod` para `StartupChecks`.

**Nota:** el CI aplica solo `init.sql` (incluye geo y sentinel). Para paridad con Docker local no hace falta ningún script adicional.
