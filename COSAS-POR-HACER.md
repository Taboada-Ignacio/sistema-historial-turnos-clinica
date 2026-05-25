# Cosas por hacer (backlog)

Lista priorizada de trabajo pendiente respecto a seguridad, integración front/gateway/backend, producto y calidad. Las prioridades **P0** son bloqueantes para considerar el flujo listo para producción.

---

## Crítico (antes de dar por cerrado producción)

| Prioridad | Ítem | Detalle |
|-----------|------|---------|
| **P0** | ~~Confirmación de cuenta paciente~~ | Hecho: confirmación por email en `GET /api/{pacientes|profesionales|administradores}/confirmar` con redirect a la SPA (`app.frontend-url` / `APP_FRONTEND_URL`), flujo registro → verificar email → éxito → panel o login. |
| **P0** | ~~Tests de seguridad y auth en `ms-usuarios`~~ | Cubierto en parte: `JwtUtilTest`, `SecurityAndAuthIntegrationTest`, `AuthLoginOriginIntegrationTest`, `AuthLoginWithUsersIntegrationTest`, `PacientesMethodSecurityIntegrationTest` (perfil `test` + H2). Pendiente opcional: CI con `mvn test`, login admin/profesional con fixtures, refresh con cookie rotación. |
| **P0** | ~~CORS + credenciales en el gateway~~ | Hecho: `spring.cloud.gateway.globalcors` con **`allowCredentials: true`**, origen explícito vía **`APP_GATEWAY_CORS_ALLOWED_ORIGIN`** (default Vite `http://localhost:5173`); eliminado `CorsWebFilter` duplicado. Alinear en prod con **`APP_ALLOWED_ORIGINS`** en `ms-usuarios`. |
| **P0** | ~~Recuperación de contraseña (UX + seguridad + doble POST)~~ | Hecho: enlace 30 min, redirects a `/recuperacion-password-error`, rutas públicas (`PublicRequestPaths`), `clienteAxiosPublic`, anti-doble-submit (`lockRef` + `globalApiLock`). Doc: [`docs/RECUPERACION-CONTRASENA.md`](docs/RECUPERACION-CONTRASENA.md). |

---

## Alta (estabilidad, seguridad operativa, producto coherente)

| Prioridad | Ítem | Detalle |
|-----------|------|---------|
| **P1** | ~~Catálogo público de profesionales~~ | Hecho: rutas públicas `GET /presentacion`, `GET /{id}/presentacion`, `GET /fotos/public/{fileName}`; SPA `/profesionales` y `/profesionales/:id` con `clienteAxiosPublic` y `ProfesionalPresentacionDTO`. Doc: [`docs/CAMBIOS-CATALOGO-PUBLICO-PROFESIONALES.md`](docs/CAMBIOS-CATALOGO-PUBLICO-PROFESIONALES.md). |
| **P1** | Subrutas de dashboard sin implementar (404) | `portalPaths.js` define turnos/historial/perfil (paciente) y turnos/pacientes/perfil (profesional), pero **`App.jsx` solo monta el dashboard** en la mayoría de subrutas. **Hecho (paciente):** `/dashboard-paciente/perfil`. Pendiente: turnos/historial (paciente) y turnos/pacientes/perfil (profesional). |
| **P1** | ~~Pantalla de configuración / perfil profesional~~ | Hecho: `/dashboard-profesional/perfil` con `ProfesionalPerfilPage` (`GET /me`, `PUT /{id}` JSON o multipart con foto). Tras guardar, `invalidate()` en `ProfesionalSessionContext`. |
| **P1** | Flujo de membresía profesional completo | Hoy: `SIN_VERIFICAR` → admin `PUT /verificar-matricula` → `INACTIVA`. Existen `GET /membresia/inactiva` y `PUT /{id}/acceso-indefinido` **sin UI admin**. La membresía **`ACTIVA`** está en BD pero **no hay endpoint** que la asigne. Definir reglas, implementar transiciones y pantallas admin (listado INACTIVA, activación, acceso indefinido). Ver matriz en [`docs/CAMBIOS-PORTAL-PROFESIONAL-DASHBOARD.md`](docs/CAMBIOS-PORTAL-PROFESIONAL-DASHBOARD.md). |
| **P1** | ~~Perfil del paciente autogestionado~~ | Hecho: `GET /api/pacientes/me`, `PacienteSessionContext`, dashboard con saludo dinámico, `/dashboard-paciente/perfil` con edición vía `PUT /api/pacientes/{id}`. Doc: [`docs/CAMBIOS-PORTAL-PACIENTE-PERFIL.md`](docs/CAMBIOS-PORTAL-PACIENTE-PERFIL.md). |
| **P1** | Rate limiting | Proteger login y flujos de recuperación de contraseña frente a abuso (no hay implementación actual en el repo). |
| **P1** | CI más amplio | Existe `.github/workflows/ms-usuarios-startup-check.yml`. Añadir job **`mvn test`** (perfil Spring `test` / H2) en push/PR; build/lint del frontend; opcional smoke del **api-gateway**. |
| **P1** | Tests E2E o contrato | Validar rutas con prefijo `/usuarios`, cookies y refresh en escenario gateway + ms + navegador. |
| **P1** | Docker Compose listo para entornos reales | El compose raíz no inyecta por defecto `JWT_SECRET`, `MAIL_*`, `APP_URL`, `APP_FRONTEND_URL`, `APP_COOKIE_SECURE`. Documentar `.env` de ejemplo en raíz y/o ampliar `docker-compose.yml` con variables opcionales. |
| **P1** | Healthchecks en Docker | `depends_on` sin `condition: service_healthy`; `ms-usuarios` puede arrancar antes de Postgres. Añadir `healthcheck` a `db-usuarios` y dependencia condicionada. |

---

## Media (calidad y alcance del sistema)

| Prioridad | Ítem | Detalle |
|-----------|------|---------|
| **P2** | Otros microservicios | El README menciona turnos e historial; en el repo predominan `ms-usuarios` y `api-gateway`. Completar lógica e integración si forman parte del entregable (`AdminTurnosPage`, `AdminHistorialesClinicosPage`, etc.). |
| **P2** | ~~`PacienteSessionContext`~~ | Hecho: mismo patrón que `ProfesionalSessionContext` — caché en memoria del perfil al navegar subpáginas del portal paciente. Ver [`docs/CAMBIOS-PORTAL-PACIENTE-PERFIL.md`](docs/CAMBIOS-PORTAL-PACIENTE-PERFIL.md). |
| **P2** | Layout compartido en portales paciente/profesional | Admin usa `AdminLayout` con nav persistente. Paciente y profesional repiten navbar en cada página; falta shell (`ProfesionalLayout` / `PacienteLayout`) para subrutas. |
| **P2** | UX dashboard según membresía `INACTIVA` | Solo se bloquea agenda/pacientes con `SIN_VERIFICAR`. Tras verificar matrícula (`INACTIVA`) los módulos quedan habilitados sin mensaje intermedio; alinear UI con reglas de negocio definitivas. |
| **P2** | Unificar carga de perfil profesional (API) | El contexto hace `GET /me` + `GET /{id}/presentacion`. Evaluar un solo endpoint o enriquecer `/me` para reducir latencia. |
| **P2** | Tests backend dominio profesional/admin | Ampliar cobertura: presentación pública, verificar matrícula, rechazo pendientes, registro con foto, `ProfesionalUpdateDTO`, listados por membresía. |
| **P2** | Tests en el frontend | No hay `*.test.*` / `*.spec.*` habitual; cobertura mínima en login, interceptor de refresh y rutas protegidas. |
| **P2** | Enlaces en correos | Revisar `APP_URL` / `app.frontend-url` y que coincidan con gateway (`/usuarios/**`) y páginas de confirmación/cambio de contraseña. |
| **P2** | Observabilidad | Logs estructurados, correlación de requests, métricas de auth (opcional en primera versión estable). |
| **P2** | Restringir Swagger/OpenAPI en producción | El gateway expone documentación OpenAPI de `ms-usuarios`; deshabilitar o proteger en prod. |

---

## Baja (pulido)

| Prioridad | Ítem | Detalle |
|-----------|------|---------|
| **P3** | Unificar feedback de errores (Swal vs `alert`) | `RegistroProfesional` y algunas pantallas admin usan `alert()` nativo; el resto usa SweetAlert2 (`axiosConfig.js`). |
| **P3** | Quitar logs `[DEBUG]` en registro profesional | `RegistroProfesional.jsx` tiene `console.log` de depuración; eliminar o guardar tras `import.meta.env.DEV`. |
| **P3** | Redirect de sesión expirada por portal | El interceptor Axios redirige a `/` genérico; debería ir al login del portal activo (`app_portal`). |
| **P3** | Landing con sesión activa | Los logins redirigen si ya hay sesión; la landing no ofrece “continuar al panel” ni redirección automática. |
| **P3** | Página 404 (catch-all) | No hay ruta `*` en `App.jsx`; URLs inválidas no muestran pantalla de error amigable. |
| **P3** | DNI como campo de texto | `input type="number"` en registros pierde ceros a la izquierda; usar `type="text"` + validación numérica. |
| **P3** | Error Boundaries en React | Un error no capturado en una pantalla puede tumbar toda la SPA. |
| **P3** | Validar `VITE_API_BASE_URL` en build | `env.js` hace fallback silencioso a `localhost:8080`; fallar el build o advertir si falta la variable en CI/prod. |
| **P3** | Accesibilidad básica | Mejorar labels, foco y `aria-live` en formularios multi-paso, toggles de contraseña y mensajes de error. |
| **P3** | Navbar / logout compartido por portal | Duplicación de nav y `clearSession` en dashboards; extraer componente común. |
| **P3** | Mejorar `AprobacionPendiente` | Tras confirmar email el profesional solo ve mensaje estático; podría enlazar al login o mostrar estado consultable. |
| **P3** | Limpieza de comentarios en servicios | Comentarios tipo “NUEVO” en `ProfesionalService*` / servicios; solo legibilidad. |

---

*Última revisión: mayo 2026. Marcar ítems como hechos al cerrarlos.*
