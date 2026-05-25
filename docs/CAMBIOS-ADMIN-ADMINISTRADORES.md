# Cambios: consultar administradores (panel admin)

Documentación de la pantalla **Consultar administradores** en el panel de administración: listado completo, detalle de cualquier cuenta y **edición/baja solo de la propia**.

Fecha de referencia: mayo 2026.

---

## 1. Resumen

| Área | Qué se agregó |
|------|----------------|
| **Listado** | `GET /api/administradores` — todos los administradores (sin búsqueda paginada; son pocos) |
| **Sesión** | `GET /api/administradores/me` — perfil del admin autenticado (`idUsuario` para la UI) |
| **Detalle** | `GET /api/administradores/{id}` — cualquier admin logueado puede ver datos completos |
| **Edición / baja** | `PUT` y `DELETE /{id}` solo si `@authorizationRules.esMismoUsuario(#id)` |
| **Frontend** | Listado, detalle (solo lectura en gris para otros), edición y eliminación propia con contraseña |
| **Unicidad** | `UnicidadUsuarioValidator` en actualización; dirección/localidad como paciente |

---

## 2. Backend (`ms-usuarios`)

### 2.1 Rutas

| Método | Ruta | Acceso | Notas |
|--------|------|--------|--------|
| GET | `/api/administradores/me` | `ROLE_ADMINISTRADOR` | Email del JWT → administrador con ubicación |
| GET | `/api/administradores` | `ROLE_ADMINISTRADOR` | Listado ordenado apellido, nombre |
| GET | `/api/administradores/{id}` | `ROLE_ADMINISTRADOR` | Detalle con `idProvincia`, `idLocalidad`, nombres geo |
| PUT | `/api/administradores/{id}` | `ROLE_ADMINISTRADOR` **y** mismo `idUsuario` | Body: `AdministradorUpdateDTO` (+ `direccion`) |
| DELETE | `/api/administradores/{id}` | `ROLE_ADMINISTRADOR` **y** mismo `idUsuario` | Borra tokens, refresh y historial de estados |

Registro público (`POST /registro`, confirmación, etc.) sin cambios de contrato.

### 2.2 Repositorio y DTO

- `AdministradorRepository.findWithUbicacionById` — fetch de dirección, localidad y provincia.
- `AdministradorRepository.findByEmail` — resolución para `/me`.
- `AdministradorResponseDTO`: `idProvincia`, `idLocalidad`, `nombreProvincia` (además de `nombreLocalidad`, `direccion`).

### 2.3 Servicio

- `obtenerAdministradorPorEmail(String email)`.
- `actualizarAdministrador`: unicidad email/DNI, actualización de dirección vía `DireccionService`.
- `eliminarAdministrador`: `VerificationTokenRepository`, `RefreshTokenRepository`, `CambioEstadoRepository` antes del delete físico.

### 2.4 Seguridad

`AuthorizationRules.esMismoUsuario(id)` compara el email del `Authentication` con `Usuario.idUsuario` (el `id` de ruta es `idUsuario`; modelo actual: composición 1:1 — ver [`CAMBIOS-MODELO-COMPOSICION-USUARIOS.md`](CAMBIOS-MODELO-COMPOSICION-USUARIOS.md)).

---

## 3. Frontend (`frontend-clinica`)

### 3.1 Hook

| Archivo | Uso |
|---------|-----|
| `src/hooks/useAdminSesion.js` | `GET /usuarios/api/administradores/me` → `idUsuario`, helper `esPropio(id)` |

### 3.2 Rutas (`portalPaths.js`)

| Ruta | Componente |
|------|------------|
| `…/entidades/administradores` | `AdminAdministradoresListaPage.jsx` |
| `…/entidades/administradores/:idAdministrador` | `AdminAdministradorDetallePage.jsx` |
| `…/entidades/administradores/:idAdministrador/editar` | `AdminAdministradorEditPage.jsx` |

### 3.3 UX

- **Listado:** filas de otros administradores en estilo gris; la fila propia con badge **Tu cuenta** y botón verde.
- **Detalle ajeno:** banner “Solo lectura”; contenido con `opacity` reducida; sin botones modificar/eliminar.
- **Detalle propio:** “Modificar mis datos” y “Eliminar mi cuenta” → `AdminConfirmModal` + `AdminPasswordConfirmModal`.
- **Edición:** si `id` ≠ sesión, redirect al detalle; guardado con verificación de contraseña.
- **Eliminación propia:** `clearSession()` y redirect a login admin.

### 3.4 Índice de entidades

`AdministrarEntidadesPage.jsx`: tarjeta **Consultar administradores** (ya no placeholder en “Otras entidades”).

---

## 4. Contrato de errores

Igual que el resto del panel: campo **`mensaje`** en 400/404; el front usa `adminApiErrorMessage`.

- `PUT`/`DELETE` de otro admin → **403** (SpEL en `@PreAuthorize`).
- Unicidad email/DNI en edición → **400** con texto de `UnicidadUsuarioValidator`.

---

## 5. Documentación relacionada

- [CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md](CAMBIOS-ADMIN-PACIENTES-PROFESIONALES.md) — pacientes y profesionales (búsqueda admin)
- [API.md](API.md) — tabla HTTP de administradores
- [frontend-clinica/README.md](../frontend-clinica/README.md) — rutas del panel
- [ms-usuarios/README.md](../ms-usuarios/README.md) — servicios backend
