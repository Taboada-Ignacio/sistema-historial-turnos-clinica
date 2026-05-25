# Catálogo público de profesionales

Implementación del ítem P1 del backlog: UI pública que consume `ProfesionalPresentacionDTO` sin exponer datos sensibles.

## Alcance

- Listado y ficha de profesionales **sin login** (landing y rutas `/profesionales`).
- Solo profesionales con **estado `ACTIVO`** y **sin rol `ROLE_ADMINISTRADOR`** (misma regla que `listarParaPresentacion` en backend).
- DTO de presentación: `idUsuario`, `nombre`, `apellido`, `especialidad`, `direccion` (texto compuesto), `fotoPerfil` (ruta relativa). **No** incluye email, DNI, teléfono, matrícula ni membresía.

## Backend (`ms-usuarios`)

### Endpoints públicos (sin JWT)

| Método | Ruta (MS) | Descripción |
|--------|-----------|-------------|
| GET | `/api/profesionales/presentacion` | Listado para catálogo. |
| GET | `/api/profesionales/{id}/presentacion` | Ficha individual. Usuario autenticado puede ver **su propia** ficha aunque no esté ACTIVO. |
| GET | `/api/profesionales/fotos/public/{fileName}` | Imagen WebP solo si pertenece a un profesional listable en el catálogo. |

### Seguridad

- `SecurityConfig`: `permitAll` en las rutas anteriores.
- `PublicRequestPaths`: bypass del filtro JWT en esos GET.
- Se eliminó `@PreAuthorize` de los métodos de presentación en `ProfesionalController` (antes exigían rol).
- Fotos autenticadas siguen en `GET /api/profesionales/fotos/{fileName}` (profesional/admin).
- `ProfesionalService.esFotoVisibleEnCatalogoPublico(fileName)` valida nombre de archivo (sin `..` ni `/`) y reglas de catálogo.

### Test

- `SecurityAndAuthIntegrationTest.getProfesionalesPresentacion_withoutAuth_isOk`

## Frontend (`frontend-clinica`)

### Rutas (`PUBLIC_PATHS` en `portalPaths.js`)

| Ruta | Componente |
|------|------------|
| `/profesionales` | `CatalogoProfesionalesPage` |
| `/profesionales/:idProfesional` | `ProfesionalPublicoDetallePage` |

### Componentes y utilidades

- `CatalogoProfesionalesPage.jsx`: `clienteAxiosPublic.get(.../presentacion)`, búsqueda por texto y filtro por especialidad.
- `ProfesionalPublicoDetallePage.jsx`: ficha por id.
- `ProfesionalFotoPublic.jsx`: blob vía `GET .../fotos/public/{fileName}` sin JWT.
- `profesionalFotoPublicRequestPath()` en `profesionalFotoUrl.js`.
- `nombreProfesionalPresentacion()` en `profesionalPresentacion.js`.

### Landing

- Nav y CTA **Ver profesionales** → `/profesionales`.

## Verificación manual

1. Sin sesión: abrir `http://localhost:5173/profesionales` → listado (vacío o con profesionales ACTIVOS en BD).
2. Network: solo GET públicos a `presentacion` y `fotos/public/...` (sin `Authorization`).
3. Clic en una card → ficha en `/profesionales/{id}`.
4. Profesional `PENDIENTE` / `SIN_VERIFICAR` no debe aparecer en el listado público.
5. Con login paciente/profesional, el catálogo público sigue funcionando igual (mismas rutas).

## Gateway

Prefijo habitual: `GET {VITE_API_BASE_URL}/usuarios/api/profesionales/presentacion`.
