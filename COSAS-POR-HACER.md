# Cosas por hacer (backlog)

Lista priorizada de trabajo pendiente respecto a seguridad, integración front/gateway/backend y cobertura de tests. Las prioridades **P0** son bloqueantes para considerar el flujo listo para producción.

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
| **P1** | Catálogo público de profesionales | El backend expone `GET /api/profesionales/presentacion` (y por id). El front aún no consume esas rutas; cablear la UI al DTO de presentación y evitar datos sensibles en listados públicos. |
| **P1** | Rate limiting | Proteger login y flujos de recuperación de contraseña frente a abuso (no hay implementación actual en el repo). |
| **P1** | CI más amplio | Existe `.github/workflows/ms-usuarios-startup-check.yml`. Añadir job **`mvn test`** (los tests activan el perfil Spring `test` / H2) en push/PR; opcionalmente build/lint del frontend. |
| **P1** | Tests E2E o contrato | Validar rutas con prefijo `/usuarios`, cookies y refresh en escenario gateway + ms + navegador. |

---

## Media (calidad y alcance del sistema)

| Prioridad | Ítem | Detalle |
|-----------|------|---------|
| **P2** | Tests en el frontend | No hay `*.test.*` / `*.spec.*` habitual; añadir cobertura mínima en login, interceptor de refresh y rutas protegidas. |
| **P2** | Otros microservicios | El README menciona turnos e historial; en el repo predominan `ms-usuarios` y `api-gateway`. Si forman parte del entregable, completar lógica e integración. |
| **P2** | Enlaces en correos | Revisar `APP_URL` / `app.frontend-url` y que coincidan con gateway (`/usuarios/**`) y páginas de confirmación/cambio de contraseña. Recuperación documentada en [`docs/RECUPERACION-CONTRASENA.md`](docs/RECUPERACION-CONTRASENA.md). |
| **P2** | Observabilidad | Logs estructurados, correlación de requests, métricas de auth (opcional en primera versión estable). |

---

## Baja (pulido)

| Prioridad | Ítem | Detalle |
|-----------|------|---------|
| **P3** | Limpieza de comentarios en servicios | Comentarios tipo “NUEVO” en `ProfesionalService*` / servicios; solo legibilidad. |
| **P3** | Documentación al día | Mantener README y este archivo cuando cambien puertos, CORS o endpoints. |

---

*Última revisión orientativa del estado del código; marcar ítems como hechos al cerrarlos.*
