# Frontend Clínica (React + Vite)

Aplicación web con tres portales: **paciente**, **profesional** y **administración interna**.

Stack: React 19, React Router 7, Vite 8, Tailwind CSS 4, Axios, SweetAlert2, `browser-image-compression` (foto profesional).

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

## Clientes HTTP (`src/api/axiosConfig.js`)

| Cliente | Uso |
|---------|-----|
| **`clienteAxios`** (default) | Rutas autenticadas: adjunta `Authorization: Bearer`, `withCredentials: true`, interceptor de **refresh** ante 401 |
| **`clienteAxiosPublic`** | Recuperación de contraseña y flujos sin JWT: no envía Bearer ni reintenta refresh |

Comportamiento del interceptor autenticado:

1. Ante **401**, si la URL no es login/refresh/recuperación, hace **`POST /usuarios/api/auth/refresh`** una vez.
2. Guarda el nuevo token con `updateSessionToken` y **reintenta** la petición original.
3. Si falla el refresh: `clearSession()`, modal SweetAlert2 y redirect a `/`.

---

## Sesión (`src/utils/auth.js`)

| Clave storage | Contenido |
|---------------|-----------|
| `token` | Access JWT |
| `app_portal` | `paciente` \| `profesional` \| `admin` |
| `app_role` | `ADMIN` (solo panel administración) |
| `app_email` | Email de sesión (opcional; fallback desde JWT `sub`) |
| `remember_me` | Si true, usa `localStorage`; si no, `sessionStorage` |

Funciones principales: `savePortalSession`, `saveAdminSession`, `hasActiveSession`, `isAdminSession`, `getDashboardRouteByPortal`, `clearSession`.

---

## Rutas protegidas (guards)

| Componente | Regla |
|------------|--------|
| `PacienteRoute` | Sesión activa y `app_portal === 'paciente'`; si no, redirect al login o al dashboard del portal correcto |
| `ProfesionalRoute` | Igual con portal `profesional` |
| `AdminRoute` | `isAdminSession()` (`app_role === 'ADMIN'`); si no, login admin |

Definición de rutas en `App.jsx`; constantes en **`src/utils/portalPaths.js`**.

---

## Mapa de rutas

Rutas públicas y protegidas centralizadas en `portalPaths.js` y registradas en `App.jsx`.

### Generales

| Ruta | Componente | Notas |
|------|------------|--------|
| `/` | `Landing` | Página de inicio |
| `/recuperacion-password-error` | `RecuperacionPasswordError` | Enlace de recuperación inválido/expirado |
| `/internal/admin/bootstrap-setup` | `AdminRegisterSecret` | Registro del **primer** administrador (requiere `X-System-Key` en el API) |

### Portal paciente

| Ruta | Componente | Protegida |
|------|------------|-----------|
| `/login` | `Login` | No |
| `/registro` | `Register` | No |
| `/verificar-email-paciente` | `VerificarEmailPaciente` | No |
| `/registro-exitoso-paciente` | `RegistroExitosoPaciente` | No → redirect a login |
| `/confirmacion-error` | `ConfirmacionError` | No |
| `/recuperar-password/paciente` | `SolicitarCambioPasswordPaciente` | No |
| `/cambiar-password/paciente` | `CambiarPasswordPaciente` | No (`?token=`) |
| `/dashboard-paciente` | `DashboardPaciente` | Sí (`PacienteRoute`) |
| `/dashboard-paciente/turnos` | *(sin ruta en App)* | Placeholder en `portalPaths` — pendiente ms-turnos |
| `/dashboard-paciente/historial` | *(sin ruta)* | Placeholder |
| `/dashboard-paciente/perfil` | *(sin ruta)* | Placeholder |

El dashboard muestra cards hacia turnos, historial y perfil; las subrutas **aún no están registradas** en `App.jsx`.

### Portal profesional

| Ruta | Componente | Protegida |
|------|------------|-----------|
| `/login-profesional` | `LoginProfesional` | No |
| `/registro-profesional` | `RegistroProfesional` | No |
| `/verificar-email-profesional` | `VerificarEmailProfesional` | No |
| `/aprobacion-pendiente` | `AprobacionPendiente` | No (post-confirmación email) |
| `/recuperar-password/profesional` | `SolicitarCambioPasswordProfesional` | No |
| `/cambiar-password/profesional` | `CambiarPasswordProfesional` | No |
| `/dashboard-profesional` | `DashboardProfesional` | Sí (`ProfesionalRoute` + `ProfesionalSessionProvider`) |
| `/dashboard-profesional/turnos` | *(placeholder)* | Pendiente — ruta no registrada en `App.jsx` |
| `/dashboard-profesional/pacientes` | *(placeholder)* | Pendiente — ruta no registrada en `App.jsx` |
| `/dashboard-profesional/perfil` | *(placeholder)* | Pendiente — ruta no registrada en `App.jsx` |

**Caché de perfil y detalle del dashboard** (membresía `SIN_VERIFICAR`, APIs, matriz membresía/estado): [`docs/CAMBIOS-PORTAL-PROFESIONAL-DASHBOARD.md`](../docs/CAMBIOS-PORTAL-PROFESIONAL-DASHBOARD.md).

### Panel administración

Prefijo layout: **`/internal/admin/panel`**. Login: **`/internal/admin/auth`**.

| Ruta relativa al panel | Componente |
|------------------------|------------|
| *(index)* | `AdminDashboardHome` |
| `profesionales-pendientes` | `ProfesionalesPendientesPage` |
| `profesionales-pendientes/:id` | `ProfesionalPendienteDetallePage` |
| `profesionales-pendientes/:id/rechazar` | `ProfesionalPendienteRechazarPage` |
| `entidades` | `AdministrarEntidadesPage` |
| `entidades/pacientes` | `AdminPacientesBuscarPage` |
| `entidades/pacientes/:id` | `AdminPacienteDetallePage` |
| `entidades/pacientes/:id/editar` | `AdminPacienteEditPage` |
| `entidades/profesionales` | `AdminProfesionalesBuscarPage` |
| `entidades/profesionales/:id` | `AdminProfesionalDetallePage` |
| `entidades/profesionales/:id/editar` | `AdminProfesionalEditPage` |
| `entidades/administradores` | `AdminAdministradoresListaPage` |
| `entidades/administradores/:id` | `AdminAdministradorDetallePage` |
| `entidades/administradores/:id/editar` | `AdminAdministradorEditPage` |
| `catalogo/:tipo` | `AdminCatalogoListaPage` |
| `catalogo/:tipo/nuevo` | `AdminCatalogoAltaPage` |
| `catalogo/:tipo/:id` | `AdminCatalogoEditPage` |
| `direcciones` | `AdminDireccionesPage` |
| `turnos` | `AdminTurnosPage` — **placeholder** (ms-turnos) |
| `historiales-clinicos` | `AdminHistorialesClinicosPage` — **placeholder** |

Rutas admin públicas (fuera del layout): verificar email, registro exitoso, recuperación/cambio contraseña (`ADMIN_PATHS` en `portalPaths.js`).

Navegación lateral del layout: `AdminLayout.jsx` (profesionales pendientes, entidades, turnos, historiales).

---

## Componentes compartidos

| Archivo | Rol |
|---------|-----|
| `VerificarEmailForm.jsx` | Código de 6 dígitos + reenvío |
| `GeoAutocomplete.jsx` | Autocomplete genérico |
| `ProvinciaLocalidadFields.jsx` | Par provincia/localidad en registros |
| `CatalogProvinciaPicker.jsx` | Selector de provincia en catálogo admin |
| `AdminConfirmModal.jsx` | Confirmación estilo panel |
| `AdminPasswordConfirmModal.jsx` | Verificación contraseña admin antes de mutar |
| `ProfesionalFoto.jsx` | Visualización de foto con URL autenticada |
| `PasswordVisibilityToggle.jsx` | Mostrar/ocultar contraseña |

### Utilidades (`src/utils/`)

| Archivo | Rol |
|---------|-----|
| `portalPaths.js` | Constantes de rutas por portal |
| `auth.js` | Sesión, JWT decode, guards helpers |
| `adminApiError.js` | `apiErrorMessage()` — lee `data.mensaje` |
| `profesionalFotoUrl.js` | URL de foto vía gateway + JWT |
| `formatEspecialidad.js` | Etiquetas legibles de especialidades |
| `ageValidation.js` | Validación edad mínima en registros |
| `geoFilter.js` | Filtrado cliente de provincias/localidades |
| `recuperacionPasswordError.js` | Query params de pantalla de error |

### Hooks

| Hook | Rol |
|------|-----|
| `useGeoCatalog.js` | Carga provincias/localidades una vez |
| `useAdminSesion.js` | `GET /administradores/me` para panel admin |
| `useSubmitCambioPasswordConToken.js` | Un solo POST al cambiar contraseña con token |

---

## Verificación de email (registro)

Componente compartido: `src/components/VerificarEmailForm.jsx`.

- El usuario ingresa el **código de 6 dígitos** recibido por correo (`POST …/confirmar-codigo` con `email` y `codigo`).
- También puede confirmar con el **enlace** del mail (`GET …/confirmar?token=` en el backend → redirect a la SPA).

Páginas por portal:

| Portal | Ruta (ejemplo) | Tras confirmar |
|--------|----------------|----------------|
| Paciente | `/verificar-email-paciente` | `/registro-exitoso-paciente` → redirect a `/login` |
| Profesional | `/verificar-email-profesional` | `/aprobacion-pendiente` |
| Admin | `/verificar-email-admin` | `/registro-exitoso-admin` |

---

## Panel de administración

Rutas bajo el layout admin (`AdminRoute` + `AdminLayout`). Prefijo típico: **`/internal/admin/panel`**.

### Administrar entidades

| Ruta | Componente | Descripción |
|------|--------------|-------------|
| `…/entidades` | `AdministrarEntidadesPage` | Índice: catálogos → pacientes/profesionales/administradores → direcciones |

### Catálogos maestros

Configuración en `src/pages/administracion/adminCatalogConfig.js`.

| Tipo en URL | API | Listado | Alta | Edición | Borrado |
|-------------|-----|---------|------|---------|---------|
| `roles` | `/api/roles` | Sí | No (solo lectura) | No | No |
| `obras-sociales` | `/api/obras-sociales` | Sí | Sí | Sí | Sí |
| `especialidades` | `/api/especialidades` | Sí | Sí | Sí | Sí |
| `provincias` | `/api/provincias` | Sí | Sí | Sí | Sí |
| `localidades` | `/api/localidades` | Sí (+ filtro provincia y búsqueda por nombre) | Sí | Sí | Sí |
| `estados` | `/api/estados` | Sí (solo lectura; PENDIENTE, ACTIVO, BLOQUEADO) | No | No | No |

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

### Utilidades y componentes admin (pacientes / profesionales)

| Pieza | Archivo |
|-------|---------|
| Mensajes del API | `src/utils/adminApiError.js` — `apiErrorMessage()` lee `data.mensaje` |
| Foto en listados | `src/utils/profesionalFotoUrl.js` |
| Confirmación (estilo panel) | `src/components/AdminConfirmModal.jsx` |
| Contraseña admin | `src/components/AdminPasswordConfirmModal.jsx` |
| Rutas | `portalPaths.js` — `pacientesBuscar`, `pacienteDetalle`, `profesionalDetalle`, etc. |

### Consultar pacientes

| Ruta | Pantalla |
|------|----------|
| `…/entidades/pacientes` | `AdminPacientesBuscarPage` — búsqueda combinable (5 modos) |
| `…/entidades/pacientes/:idPaciente` | `AdminPacienteDetallePage` — detalle, modificar, eliminar |
| `…/entidades/pacientes/:idPaciente/editar` | `AdminPacienteEditPage` — edición con contraseña al guardar |

**Búsqueda:** `GET /usuarios/api/pacientes/buscar` con `q`, `idProvincia`, `idLocalidad`.

**Edición:** errores de email/DNI duplicados mostrados con `adminApiErrorMessage`.

**Eliminación:** `AdminConfirmModal` (no `window.confirm`) → modal de contraseña → `DELETE`.

### Consultar profesionales

| Ruta | Pantalla |
|------|----------|
| `…/entidades/profesionales` | `AdminProfesionalesBuscarPage` — búsqueda combinable (11 modos) |
| `…/entidades/profesionales/:idProfesional` | `AdminProfesionalDetallePage` |
| `…/entidades/profesionales/:idProfesional/editar` | `AdminProfesionalEditPage` |

**Búsqueda:** `GET /usuarios/api/profesionales/buscar` con `q`, `idEspecialidad`, `idProvincia`, `idLocalidad`.

**Listado (columnas):** Foto de perfil → Apellido y nombre → DNI → Especialidad → botón **Ver profesional**.

**Detalle:** foto, matrícula, membresía, ubicación, roles. Misma UX de confirmación y baja que pacientes.

Documentación: [`docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md`](../docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md).

### Consultar administradores

| Ruta | Pantalla |
|------|----------|
| `…/entidades/administradores` | `AdminAdministradoresListaPage` — listado completo (`GET /administradores`) |
| `…/entidades/administradores/:idAdministrador` | `AdminAdministradorDetallePage` — detalle; solo lectura en gris si no es tu cuenta |
| `…/entidades/administradores/:idAdministrador/editar` | `AdminAdministradorEditPage` — solo el administrador logueado |

**Sesión:** hook `useAdminSesion` → `GET /usuarios/api/administradores/me` para comparar `idUsuario`.

**Listado:** filas ajenas en gris; la propia con badge **Tu cuenta**.

**Detalle ajeno:** banner de solo lectura; sin modificar ni eliminar.

**Detalle propio:** modificar datos o eliminar cuenta (confirmación + contraseña); tras eliminar → `clearSession()` y login admin.

Documentación: [`docs/CAMBIOS-ADMIN-ADMINISTRADORES.md`](../docs/CAMBIOS-ADMIN-ADMINISTRADORES.md).

### Profesionales pendientes de revisión

| Ruta | Pantalla |
|------|----------|
| `…/profesionales-pendientes` | Listado (`membresia=SIN_VERIFICAR`) |
| `…/profesionales-pendientes/:id` | Detalle, verificación de matrícula o enlace a rechazo |
| `…/profesionales-pendientes/:id/rechazar` | Motivo del rechazo + contraseña del admin → `POST …/rechazar-pendiente` |

Documentación completa: [`docs/CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md`](../docs/CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md).

### Otras pantallas admin

- Turnos e historiales: rutas placeholder según evolución del proyecto

---

## Llamadas al API

Todas las peticiones usan el prefijo del gateway:

```text
{VITE_API_BASE_URL}/usuarios/api/...
```

Ejemplo: listar provincias → `GET /usuarios/api/provincias`.

---

## Registro paciente

### Email o DNI ya registrados

`Register.jsx` usa `apiErrorMessage` del helper compartido. El backend informa por separado: solo email, solo DNI, o ambos (`UnicidadUsuarioValidator`).

### Tras confirmar el correo

`RegistroExitosoPaciente.jsx` muestra “Cuenta activada” y redirige a **`/login`** (no al dashboard).

---

## Registro profesional

### Email o DNI ya registrados

Si el email (o DNI) ya existe, el backend responde **400** con `{ "mensaje": "..." }`. La SPA muestra ese texto en un banner en el formulario (no un `alert` genérico).

### Foto de perfil

En el paso **Seguridad y Perfil** (`RegistroProfesional.jsx`), el usuario ve un aviso antes de subir la foto:

- Debe ser de **ámbito profesional** (rostro visible, fondo neutro, vestimenta acorde).
- La imagen será **visible para todos los pacientes y usuarios** del sistema.

Formato aceptado: **JPG**; el front comprime y convierte a WebP antes del `multipart` al backend.

---

## Documentación del proyecto

- [docs/ARQUITECTURA.md](../docs/ARQUITECTURA.md): estructura del repo, Docker, puertos.
- [docs/API-GATEWAY.md](../docs/API-GATEWAY.md): gateway y prefijo `/usuarios`.
- README raíz: arquitectura, seguridad, variables de entorno, CI.
- [`ms-usuarios/README.md`](../ms-usuarios/README.md): base de datos, sentinel, direcciones, verificación de email.
- [`docs/API.md`](../docs/API.md): contrato HTTP completo.
- [`docs/RECUPERACION-CONTRASENA.md`](../docs/RECUPERACION-CONTRASENA.md): recuperación de contraseña, errores de enlace y anti-doble-submit.
- [`docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md`](../docs/CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md): consulta, edición y baja de pacientes/profesionales en el panel admin.
- [`docs/CAMBIOS-ADMIN-ADMINISTRADORES.md`](../docs/CAMBIOS-ADMIN-ADMINISTRADORES.md): consulta de administradores; edición/baja solo de la cuenta propia.
