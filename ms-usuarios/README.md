# Microservicio de usuarios (`ms-usuarios`)

Identidad, roles, JWT, refresh tokens, catálogos maestros, direcciones y confirmación de cuenta por email.

**Documentación relacionada**

| Archivo | Contenido |
|---------|-----------|
| [README-deploy.md](README-deploy.md) | Cookies, CORS, variables de despliegue |
| [../docs/API.md](../docs/API.md) | Rutas HTTP y acceso |
| [../docs/ENTIDADES.md](../docs/ENTIDADES.md) | Modelo JPA |
| [../docs/SEGURIDAD.md](../docs/SEGURIDAD.md) | JWT, filtros, `@PreAuthorize` |
| [init.sql](init.sql) | Esquema y semilla base |
| [sql/argentina-geo-data.sql](sql/argentina-geo-data.sql) | Provincias y localidades de Argentina |
| [sql/03-direcciones-sentinel.sql](sql/03-direcciones-sentinel.sql) | Dirección `SIN ESPECIFICAR` por localidad |
| [migration-drop-usuario-id-localidad.sql](migration-drop-usuario-id-localidad.sql) | Migración para BDs antiguas |

---

## Arranque local

### Con Docker (recomendado)

```bash
cd ms-usuarios
docker compose up -d
```

PostgreSQL en **5432**; el servicio en **8081**. Los scripts de inicialización se montan en este orden:

| Orden | Archivo | Contenido |
|-------|---------|-----------|
| 1 | `init.sql` → `01-init.sql` | Tablas, estados, roles, membresías, catálogos base, sentinel `SIN ESPECIFICAR` en especialidades/obras/provincias |
| 2 | `sql/argentina-geo-data.sql` | 24 provincias + localidades principales + localidad sentinel |
| 3 | `sql/03-direcciones-sentinel.sql` | Una fila `direcciones.nombre = 'SIN ESPECIFICAR'` por cada localidad |

**Importante:** los scripts de `docker-entrypoint-initdb.d` solo se ejecutan en un **volumen de datos nuevo**. Si la BD ya existía, hay que recrear el volumen o aplicar migraciones a mano.

### Sin Docker

```bash
mvn spring-boot:run
```

Configuración en `src/main/resources/application.yml`. Con `spring.jpa.hibernate.ddl-auto: update`, Hibernate puede ajustar el esquema, pero la **semilla geo** y los **sentinel** deben cargarse con los SQL anteriores.

---

## Modelo de ubicación (usuario ↔ dirección)

- Tabla **`direcciones`**: `nombre` + `id_localidad` (único por par).
- **`usuarios`** referencia solo **`id_direccion`** (no hay `id_localidad` en usuario).
- La localidad del usuario se obtiene por join: `usuario.direccion.localidad`.

### Registro de usuarios (paciente / profesional / administrador)

El body incluye **`idLocalidad`** y texto **`direccion`**. El servicio **crea o reutiliza** una fila en `direcciones` con ese nombre en esa localidad y asigna su id al usuario.

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

- Paciente → `/registro-exitoso-paciente`
- Profesional → `/aprobacion-pendiente`
- Administrador → `/registro-exitoso-admin`

---

## Seguridad admin: contraseña actual

`POST /api/seguridad/verificar-password-actual` — el panel admin lo usa antes de mutaciones sensibles (alta/edición de catálogos y direcciones).

---

## Migraciones

### Base nueva

Usar los tres scripts vía `docker compose` (ver arriba).

### Base existente con `usuarios.id_localidad`

1. Asegurar que cada usuario tenga `id_direccion` coherente con su localidad.
2. Ejecutar [migration-drop-usuario-id-localidad.sql](migration-drop-usuario-id-localidad.sql).
3. Cargar geo y sentinel si faltan (`argentina-geo-data.sql`, `03-direcciones-sentinel.sql`).

---

## Tests

```bash
mvn test
```

Perfil `test` con H2. Incluye seguridad, login con usuarios semilla, y reglas de método en pacientes.

---

## CI

Workflow `.github/workflows/ms-usuarios-startup-check.yml`: Postgres + `init.sql` + arranque con perfil `prod` para `StartupChecks`.

**Nota:** si el CI solo monta `init.sql`, no tendrá provincias argentinas ni direcciones sentinel hasta ampliar el workflow con `02` y `03` (recomendado para paridad con Docker local).
