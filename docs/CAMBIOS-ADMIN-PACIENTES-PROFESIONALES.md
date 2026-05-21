# Cambios: administración de pacientes y profesionales

Documentación de las mejoras al **panel de administración** (consulta, detalle, edición y baja) y ajustes de **registro / unicidad** en `ms-usuarios` y `frontend-clinica`.

Fecha de referencia: mayo 2026.

---

## 1. Resumen

| Área | Qué se agregó |
|------|----------------|
| **Pacientes (admin)** | Búsqueda combinable, listado, detalle, edición y eliminación con contraseña |
| **Profesionales (admin)** | Misma mecánica; listado con **foto**, DNI y especialidad |
| **Registro paciente** | Mensajes claros si email, DNI o ambos duplicados; lectura de `mensaje` en la SPA |
| **Registro exitoso paciente** | Tras confirmar email → redirect a **login** (no dashboard) |
| **Unicidad** | `UnicidadUsuarioValidator` compartido (alta y actualización) |
| **UI admin** | Modales de confirmación con estilo del panel (`AdminConfirmModal`, `AdminPasswordConfirmModal`) |

---

## 2. Backend (`ms-usuarios`)

### 2.1 Validación de unicidad — `UnicidadUsuarioValidator`

Paquete: `com.clinica.usuarios.service.support.UnicidadUsuarioValidator`

| Método | Uso |
|--------|-----|
| `validarAlta(repository, email, dni)` | Registro paciente y profesional |
| `validarActualizacion(repository, idUsuario, email, dni)` | Actualización paciente y profesional (excluye al usuario editado) |

Mensajes en **alta** (orientados al usuario que se registra) y en **actualización** (orientados al admin: “otro usuario”).

### 2.2 Pacientes — búsqueda admin

| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/api/pacientes/buscar` | `ROLE_ADMINISTRADOR` |

**Query (opcionales, combinables):**

| Parámetro | Descripción |
|-----------|-------------|
| `q` | Apellido y/o nombre (parcial; admite “Apellido Nombre”) |
| `idProvincia` | Filtra por provincia de la dirección del paciente |
| `idLocalidad` | Requiere `idProvincia`; filtra por localidad |

**Reglas:** al menos `q` o `idProvincia`. Localidad sin provincia → **400**.

**Respuesta:** `{ "total", "pacientes": [ PacienteListadoDTO ], "criteriosAplicados" }` (máx. 500).

Implementación: `PacienteSpecifications.busquedaAdmin`, `PacienteRepository` + `JpaSpecificationExecutor`.

### 2.3 Profesionales — búsqueda admin

| Método | Ruta | Rol |
|--------|------|-----|
| GET | `/api/profesionales/buscar` | `ROLE_ADMINISTRADOR` |

**Query (opcionales, combinables):**

| Parámetro | Descripción |
|-----------|-------------|
| `q` | Apellido y/o nombre |
| `idEspecialidad` | Especialidad del profesional |
| `idProvincia` | Provincia de su dirección |
| `idLocalidad` | Requiere `idProvincia` |

**Reglas:** al menos `q`, `idEspecialidad` o `idProvincia`.

**Respuesta:** `{ "total", "profesionales": [ ProfesionalListadoDTO ], "criteriosAplicados" }` (máx. 500).

`ProfesionalListadoDTO`: `idUsuario`, `fotoPerfil`, `apellido`, `nombre`, `dni`, `especialidad` (texto).

Implementación: `ProfesionalSpecifications.busquedaAdmin`, `ProfesionalRepository` + `JpaSpecificationExecutor`.

### 2.4 Actualización y detalle

- **Paciente:** `PUT /api/pacientes/{id}` — unicidad email/DNI; `PacienteResponseDTO` incluye `idProvincia`, `idLocalidad`, `idObraSocial`.
- **Profesional:** `PUT /api/profesionales/{id}` — unicidad email/DNI, matrícula única, actualización de `idEspecialidad`; `ProfesionalResponseDTO` incluye `idEspecialidad`, `idProvincia`, `idLocalidad`.
- Detalle con ubicación: `findWithUbicacionById` en ambos repositorios.

### 2.5 Eliminación

| Recurso | Ruta | Notas |
|---------|------|--------|
| Paciente | `DELETE /api/pacientes/{id}` | Integridad (turnos, etc.) → `ReglaDeNegocioException` |
| Profesional | `DELETE /api/profesionales/{id}` | Igual |

El front valida antes la contraseña con `POST /api/seguridad/verificar-password-actual`.

### 2.6 Confirmación de cuenta — redirect paciente

Tras `GET /api/pacientes/confirmar?token=` → SPA **`/registro-exitoso-paciente`**, que redirige al **login** (`/login`), no al dashboard.

---

## 3. Frontend (`frontend-clinica`)

### 3.1 Utilidades

| Archivo | Uso |
|---------|-----|
| `src/utils/adminApiError.js` | `apiErrorMessage(err)` — lee `response.data.mensaje` |
| `src/utils/profesionalFotoUrl.js` | URL absoluta de foto vía gateway (`/usuarios/fotosPerfilProfesionales/...`) |
| `src/utils/formatEspecialidad.js` | Etiquetas legibles en listados |
| `src/utils/portalPaths.js` | Rutas admin pacientes y profesionales |

### 3.2 Componentes compartidos (admin)

| Componente | Descripción |
|------------|-------------|
| `AdminPasswordConfirmModal` | Contraseña del admin antes de guardar/eliminar; props `confirmLabel`, `submittingLabel` |
| `AdminConfirmModal` | Sustituye `window.confirm` del navegador (mismo estilo que el panel) |
| `ProvinciaLocalidadFields` | Props `provinciaRequired` / `localidadRequired` (opcionales en búsqueda) |

### 3.3 Consultar pacientes

| Ruta | Componente |
|------|------------|
| `…/entidades/pacientes` | `AdminPacientesBuscarPage.jsx` |
| `…/entidades/pacientes/:idPaciente` | `AdminPacienteDetallePage.jsx` |
| `…/entidades/pacientes/:idPaciente/editar` | `AdminPacienteEditPage.jsx` |

**Búsqueda:** 5 modos (nombre; provincia; provincia+localidad; provincia+nombre; los tres).

**UI:** botones (no links) para “Ver paciente” y “Volver a consultar pacientes”. Eliminación: `AdminConfirmModal` → `AdminPasswordConfirmModal`.

### 3.4 Consultar profesionales

| Ruta | Componente |
|------|------------|
| `…/entidades/profesionales` | `AdminProfesionalesBuscarPage.jsx` |
| `…/entidades/profesionales/:idProfesional` | `AdminProfesionalDetallePage.jsx` |
| `…/entidades/profesionales/:idProfesional/editar` | `AdminProfesionalEditPage.jsx` |

**Búsqueda:** 11 modos documentados en la pantalla (combinaciones de nombre, especialidad, provincia y localidad).

**Listado (columnas):** Foto → Apellido y nombre → DNI → Especialidad → Ver profesional.

**Detalle:** foto grande, datos profesionales (matrícula, membresía), ubicación, roles. Misma UX de baja que pacientes.

### 3.5 Registro paciente

- `Register.jsx`: errores vía `apiErrorMessage` (campo `mensaje`).
- `RegistroExitosoPaciente.jsx`: mensaje de cuenta activada y redirect a `PACIENTE_PATHS.login`.

### 3.6 Índice de entidades

`AdministrarEntidadesPage.jsx`: enlaces a **Consultar pacientes**, **Consultar profesionales** y **Consultar administradores** (ver [CAMBIOS-ADMIN-ADMINISTRADORES.md](CAMBIOS-ADMIN-ADMINISTRADORES.md)).

---

## 4. Modos de búsqueda (referencia rápida)

### Pacientes

1. Solo apellido/nombre  
2. Solo provincia  
3. Provincia + localidad  
4. Provincia + apellido/nombre  
5. Provincia + localidad + apellido/nombre  

### Profesionales

1. Solo apellido/nombre  
2. Solo especialidad  
3. Solo provincia  
4. Provincia + localidad  
5. Provincia + apellido/nombre  
6. Provincia + localidad + apellido/nombre  
7. Especialidad + apellido/nombre  
8. Especialidad + provincia  
9. Especialidad + provincia + localidad  
10. Especialidad + provincia + apellido/nombre  
11. Especialidad + provincia + localidad + apellido/nombre  

---

## 5. Contrato de errores (SPA admin y registro)

| HTTP | Campo | Ejemplo |
|------|--------|---------|
| 400 | `mensaje` | Unicidad, reglas de búsqueda, contraseña incorrecta |
| 404 | `mensaje` | Recurso no encontrado |

Siempre usar **`mensaje`** en el front (`adminApiErrorMessage`), no `message`.

---

## 6. Documentación relacionada

- [CAMBIOS-ADMIN-ADMINISTRADORES.md](CAMBIOS-ADMIN-ADMINISTRADORES.md) — consultar administradores; edición/baja solo cuenta propia  
- [CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md](CAMBIOS-REGISTRO-Y-RECHAZO-PROFESIONAL.md) — registro profesional, rechazo pendientes  
- [API.md](API.md) — tabla de rutas HTTP  
- [frontend-clinica/README.md](../frontend-clinica/README.md) — rutas y componentes del panel  
- [ms-usuarios/README.md](../ms-usuarios/README.md) — servicios y validaciones en backend  
