# Seguridad — visión del sistema

## Rol central de `ms-usuarios`

**Este microservicio (`ms-usuarios`) concentra la seguridad de identidad del ecosistema:**

- Registro y confirmación de cuenta (paciente, profesional, administrador).
- **Login** por credenciales con validación de **portal** (`paciente` | `profesional` | `admin`).
- Emisión del **JWT de acceso** y gestión de **refresh tokens** (persistidos, rotación, revocación al cambiar contraseña).
- Recuperación / cambio de contraseña coordinado con correo y tokens.
- **Roles y estados** (`ROLE_*`, `ACTIVO`, `PENDIENTE`, etc.) y reglas `@PreAuthorize` en sus propios endpoints.

**Los demás microservicios deben diseñarse “alrededor” de este modelo:** no deben implementar un segundo sistema de usuarios/contraseñas ni emitir tokens propios en paralelo. Consumen la **identidad ya establecida** por `ms-usuarios` (típicamente validando el mismo JWT o confiando en un gateway que ya validó al usuario).

---

## Contrato hacia otros microservicios

1. **Access token (JWT)**  
   - Emitido solo tras login exitoso en `ms-usuarios`.  
   - Incluye claim **`authorities`** (lista de `ROLE_*`). El **subject** es el **email** del usuario (convención actual con `UserDetails`).  
   - Los otros MS deben usar estas autoridades solo para **autorización de negocio local** (ej. “¿puede cancelar este turno?”), no para volver a autenticar contraseña.

2. **Refresh token**  
   - Cookie **HttpOnly** gestionada en flujos expuestos por **`ms-usuarios`** (`/api/auth/login`, `/api/auth/refresh`). Los otros MS **no** deben duplicar este mecanismo.

3. **Referencias a usuarios**  
   - IDs estables (`idUsuario`) o email según contrato de API interna; **sin duplicar** tablas `usuarios` / passwords en otros bounded contexts.

4. **Decisiones de despliegue (a documentar cuando se cierre)**  
   - Validación JWT **en cada MS** (misma clave compartida / secret vía config) **vs** validación **solo en API Gateway** y red interna “trusted”. Ambos son válidos; hay que elegir uno y documentarlo en operaciones para no tener dos políticas contradictorias.

---

## Componentes en `ms-usuarios`

| Pieza | Función |
|-------|---------|
| **`SecurityConfig`** | Cadena stateless, JWT filter, rutas públicas (auth, registro, confirmaciones, GET maestros, etc.). |
| **`JwtAuthFilter`** | Lee `Authorization: Bearer`; si falta, continúa (anon); si hay token, valida firma/exp y rellena `SecurityContext`. Respuestas JSON con códigos **`TOKEN_EXPIRED`** / **`TOKEN_INVALID`** cuando aplica. Rutas `/api/auth/*`, `registro`, `confirmar`, `reenviar-confirmacion` **no** pasan por validación JWT obligatoria (filtro omitido). |
| **`JwtUtil`** | Firma HS256, claims `authorities`, expiración desde config. |
| **`UserDetailsServiceImpl`** | Carga usuario por email; rechaza estado **`PENDIENTE`** (`DisabledException`); mapea roles a `GrantedAuthority`. |
| **`@EnableMethodSecurity` + `@PreAuthorize`** | Autorización fina en controladores (ADMIN, ownership con `@authorizationRules.esMismoUsuario(#id)`). |
| **`AuthorizationRules`** | Compara `idUsuario` de la ruta con el usuario autenticado (email → `UsuarioRepository`). |
| **`AuthController`** | Login valida **`portal`** frente a roles; validación opcional **Origin/Referer** vs `app.allowed-origins` en login y refresh. |

---

## Portal de login

El cliente envía **`portal`**: `paciente`, `profesional` o `admin`. El servidor exige que el usuario tenga el **`ROLE_*`** correspondiente; si no, responde **`PORTAL_NO_PERMITIDO`**. Esto **no sustituye** `@PreAuthorize` en cada endpoint.

---

## Cookies y entorno

- **`APP_COOKIE_SECURE`**: cookies marcadas Secure en producción (HTTPS).
- **`APP_ALLOWED_ORIGINS`**: lista separada por comas; si no está vacía, login y refresh validan **Origin** o **Referer**.
- **`APP_FRONTEND_URL` / `app.frontend-url`**: base de la SPA para redirects tras confirmar email (`GET .../confirmar`).

Detalle de variables: [`ms-usuarios/README-deploy.md`](../ms-usuarios/README-deploy.md) y README raíz.

---

## API Gateway

El gateway **enruta** (`/usuarios/**` → `ms-usuarios`) y expone documentación OpenAPI agregada. **CORS** está centralizado en **`spring.cloud.gateway.globalcors`**: un solo origen permitido vía variable de entorno **`APP_GATEWAY_CORS_ALLOWED_ORIGIN`** (por defecto `http://localhost:5173`), **`allowCredentials: true`** para compatibilidad con **`withCredentials`** del front y cookies de refresh, y cabeceras explícitas (no `*` en `Allow-Origin`). Debe coincidir con **`APP_ALLOWED_ORIGINS`** en `ms-usuarios` para login y refresh.

---

## Arranque en producción

**`StartupChecks`** se ejecuta en modo **estricto** cuando el perfil activo es **`prod`** o **`production`** y **`app.startup.strict`** es `true` (por defecto **true**): cookie Secure, **`APP_ALLOWED_ORIGINS`** sin localhost, secreto JWT ≥ 32 caracteres y distinto del placeholder, conectividad a BD y presencia de roles (`ROLE_*`), estados (`PENDIENTE`, `ACTIVO`, `BLOQUEADO`) y membresías mínimas (`SIN_VERIFICAR`, `INACTIVA`).

---

## Tests

Cobertura automática en `ms-usuarios/src/test/java/...`: JWT unitario, MockMvc (público vs protegido, login/portal/refresh/origen), login con usuarios de prueba, `@WithMockUser` en listados. Ver README del repositorio.

---

## Resumen

| Responsabilidad | Dónde |
|-----------------|--------|
| Quién soy / contraseña / roles / refresh | **`ms-usuarios`** |
| Reglas de negocio por dominio (turnos, historial…) | **Cada MS**, usando identidad ya emitida por **`ms-usuarios`** |
| Documentación de rutas HTTP | [`API.md`](API.md) + OpenAPI |
