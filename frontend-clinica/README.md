# Frontend Clínica (React + Vite)

Aplicación web con tres portales: **paciente**, **profesional** y **administración interna**.

## Requisitos

- Node.js 18+ (recomendado LTS)
- npm

## Configuración

1. Copiá el ejemplo de entorno:

   ```bash
   cp .env.example .env
   ```

2. Editá **`.env`** y definí la URL base del API (gateway o microservicio):

   ```env
   VITE_API_BASE_URL=http://localhost:8080
   ```

   Sin barra final. En desarrollo suele ser el **gateway**; si llamás directo a `ms-usuarios`, usá su puerto (p. ej. `8081`).

## Scripts

| Comando | Descripción |
|---------|-------------|
| `npm run dev` | Servidor de desarrollo (Vite) |
| `npm run build` | Build de producción |
| `npm run preview` | Vista previa del build |
| `npm run lint` | ESLint (tras `npm install`) |

## Autenticación

- **Login:** cada pantalla envía `portal`: `paciente`, `profesional` o `admin` junto con email y contraseña (`POST .../api/auth/login`).
- **Access token:** se guarda en `localStorage` o `sessionStorage` según “recordarme”.
- **Refresh token:** cookie **HttpOnly**; el cliente usa **`withCredentials: true`** en Axios.
- Si una petición devuelve **401**, el interceptor intenta **`POST .../api/auth/refresh`** y reintenta la petición una vez.

## Recuperación de contraseña

Flujo **sin JWT** (`clienteAxiosPublic` en `src/api/axiosConfig.js`).

| Portal | Solicitar correo | Nueva contraseña | Error de enlace |
|--------|------------------|------------------|-----------------|
| Paciente | `/recuperar-password/paciente` | `/cambiar-password/paciente?token=` | `/recuperacion-password-error` |
| Profesional | `/recuperar-password/profesional` | `/cambiar-password/profesional?token=` | `?tipo=profesional&motivo=invalido\|expirado` |
| Admin | `/recuperar-password/admin` | `/cambiar-password/admin?token=` | `?tipo=admin&…` |

- Enlace del correo válido **30 minutos**; el token es de **un solo uso**.
- Hook: `src/hooks/useSubmitCambioPasswordConToken.js` — un solo `POST`; candado `lockRef` en la página + `globalApiLock` en el hook para evitar doble envío.
- Documentación completa: [`docs/RECUPERACION-CONTRASENA.md`](../docs/RECUPERACION-CONTRASENA.md).

**Importante:** `VITE_API_BASE_URL` debe apuntar al **gateway** (`http://localhost:8080`) con `api-gateway` levantado en Docker, porque las rutas del front usan el prefijo `/usuarios/api/...`.

---

## Verificación de email (registro)

Componente compartido: `src/components/VerificarEmailForm.jsx`.

- El usuario ingresa el **código de 6 dígitos** recibido por correo (`POST …/confirmar-codigo` con `email` y `codigo`).
- También puede confirmar con el **enlace** del mail (`GET …/confirmar?token=` en el backend → redirect a la SPA).

Páginas por portal:

| Portal | Ruta (ejemplo) | Tras confirmar |
|--------|----------------|----------------|
| Paciente | `/verificar-email-paciente` | `/registro-exitoso-paciente` |
| Profesional | `/verificar-email-profesional` | `/aprobacion-pendiente` |
| Admin | ruta en `ADMIN_PATHS.verificarEmail` | `/registro-exitoso-admin` |

---

## Panel de administración

Rutas bajo el layout admin (`AdminRoute` + `AdminLayout`). Prefijo típico: **`/internal/admin/panel`**.

### Administrar entidades

| Ruta | Componente | Descripción |
|------|--------------|-------------|
| `…/entidades` | `AdministrarEntidadesPage` | Índice de catálogos y enlace a direcciones |

### Catálogos maestros

Configuración en `src/pages/administracion/adminCatalogConfig.js`.

| Tipo en URL | API | Listado | Alta | Edición | Borrado |
|-------------|-----|---------|------|---------|---------|
| `roles` | `/api/roles` | Sí | No (solo lectura) | No | No |
| `obras-sociales` | `/api/obras-sociales` | Sí | Sí | Sí | Sí |
| `especialidades` | `/api/especialidades` | Sí | Sí | Sí | Sí |
| `provincias` | `/api/provincias` | Sí | Sí | Sí | Sí |
| `localidades` | `/api/localidades` | Sí (+ filtro provincia y búsqueda por nombre) | Sí | Sí | Sí |

Rutas del front:

- Listado: `/internal/admin/panel/catalogo/:tipo`
- Alta: `/internal/admin/panel/catalogo/:tipo/nuevo` → `AdminCatalogoAltaPage`
- Edición: `/internal/admin/panel/catalogo/:tipo/:id` → `AdminCatalogoEditPage`

**Alta y edición** llaman primero a `POST /api/seguridad/verificar-password-actual` y luego al `POST /registro` o `PUT /{id}` del catálogo.

**Localidades:** selector de provincia en alta y edición (`CatalogProvinciaPicker`). En el **listado**: filtro provincia + nombre **en el cliente** (catálogo cargado una vez).

### Provincia y localidad (autocomplete)

Componentes en `src/components/` y hook `src/hooks/useGeoCatalog.js`:

| Pieza | Uso |
|-------|-----|
| `GeoAutocomplete` | Input + lista; filtra opciones al escribir (cliente). |
| `ProvinciaLocalidadFields` | Par provincia + localidad (registro paciente/profesional/admin, direcciones admin). |
| `CatalogProvinciaPicker` | Solo provincia (alta/edición/filtro de localidades). |
| `useGeoCatalog` | Carga `GET /provincias` y `GET /localidades` una vez; expone `filtrarProvincias`, `filtrarLocalidades`, `reload`. |

En formularios de registro se oculta el sentinel `SIN ESPECIFICAR`. El listado admin de localidades filtra en memoria (no usa query `?nombre=` al tipear).

**API backend (opcional):** `GET /usuarios/api/localidades?provinciaId=&nombre=` sigue disponible para otros clientes; el front de registro/lista admin usa filtrado local.

**Registro reservado:** filas con nombre/descripción `SIN ESPECIFICAR` no se editan ni eliminan (`filaEsReservada` en `adminCatalogConfig.js`).

Payloads: `src/pages/administracion/catalogPayloadHelpers.js`.

### Direcciones

| Ruta | Componente |
|------|------------|
| `…/direcciones` | `AdminDireccionesPage` |

Flujo: elegir **provincia** → **localidad** → listar direcciones (`GET /api/direcciones/localidad/{id}`).

- **Alta:** `POST /api/direcciones/registro` (nombre en mayúsculas).
- **Edición:** solo el nombre (`PUT /api/direcciones/{id}`).
- **Baja:** `DELETE /api/direcciones/{id}` (usuarios afectados pasan a `SIN ESPECIFICAR` en esa localidad).

Todas las mutaciones piden **contraseña de administrador** antes de ejecutarse.

No se puede crear ni renombrar una dirección a `SIN ESPECIFICAR` desde el panel.

### Otras pantallas admin

- Profesionales pendientes: `…/profesionales-pendientes`
- Turnos e historiales: rutas placeholder según evolución del proyecto

---

## Llamadas al API

Todas las peticiones usan el prefijo del gateway:

```text
{VITE_API_BASE_URL}/usuarios/api/...
```

Ejemplo: listar provincias → `GET /usuarios/api/provincias`.

---

## Registro profesional — foto de perfil

En el paso **Seguridad y Perfil** (`RegistroProfesional.jsx`), el usuario ve un aviso antes de subir la foto:

- Debe ser de **ámbito profesional** (rostro visible, fondo neutro, vestimenta acorde).
- La imagen será **visible para todos los pacientes y usuarios** del sistema.

Formato aceptado: **JPG**; el front comprime y convierte a WebP antes del `multipart` al backend.

---

## Documentación del proyecto

- README raíz: arquitectura, seguridad, variables de entorno, CI.
- [`ms-usuarios/README.md`](../ms-usuarios/README.md): base de datos, sentinel, direcciones, verificación de email.
- [`docs/API.md`](../docs/API.md): contrato HTTP completo.
- [`docs/RECUPERACION-CONTRASENA.md`](../docs/RECUPERACION-CONTRASENA.md): recuperación de contraseña, errores de enlace y anti-doble-submit.
