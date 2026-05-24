# API Gateway

Documentación del componente **`api-gateway`**: punto de entrada HTTP único para el frontend y clientes externos en desarrollo.

---

## Rol en el sistema

| Función | Descripción |
|---------|-------------|
| **Enrutamiento** | Prefijo `/usuarios/**` → microservicio `ms-usuarios` |
| **StripPrefix** | Elimina `/usuarios` antes de reenviar (el MS ve rutas como `/api/...`) |
| **CORS** | Origen explícito + `allowCredentials: true` para cookies de refresh |
| **OpenAPI agregado** | Swagger UI del gateway con spec de usuarios |

El gateway **no** valida JWT ni implementa lógica de negocio; delega en `ms-usuarios`.

---

## Puertos y arranque

| Entorno | Puerto | Comando |
|---------|--------|---------|
| Docker (compose raíz) | **8080** | `docker compose up api-gateway` desde la raíz |
| Local (Maven) | **8080** | `cd api-gateway && mvn spring-boot:run` |

Configuración: [`api-gateway/src/main/resources/application.yml`](../api-gateway/src/main/resources/application.yml).

**Nota:** en `application.yml`, la URI del MS apunta a `http://ms-usuarios:8081` (red Docker). Para probar **sin** compose, cambiar temporalmente a `http://localhost:8081`.

---

## Rutas configuradas

### 1. Microservicio de usuarios

| Cliente (gateway) | Llega a ms-usuarios | Ejemplo |
|-------------------|---------------------|---------|
| `GET /usuarios/api/pacientes/buscar` | `GET /api/pacientes/buscar` | Búsqueda admin |
| `POST /usuarios/api/auth/login` | `POST /api/auth/login` | Login + cookie refresh |
| `GET /usuarios/api/profesionales/fotos/{file}.webp` | `GET /api/profesionales/fotos/{file}.webp` | Foto de perfil (JWT) |

Filtro aplicado: **`StripPrefix=1`** (quita el primer segmento de path).

### 2. Documentación OpenAPI

| Gateway | Reescritura | Destino MS |
|---------|-------------|------------|
| `GET /v3/api-docs/ms-usuarios` | → `/v3/api-docs` | Spec OpenAPI de usuarios |

Swagger UI del gateway lista **API Usuarios** apuntando a esa URL (config `springdoc.swagger-ui.urls`).

Acceso típico en local: `http://localhost:8080/swagger-ui.html` (ruta exacta según versión de springdoc).

---

## CORS

Configuración en `spring.cloud.gateway.globalcors`:

| Propiedad | Valor |
|-----------|--------|
| Origen permitido | **`APP_GATEWAY_CORS_ALLOWED_ORIGIN`** (default `http://localhost:5173`) |
| Métodos | GET, POST, PUT, DELETE, OPTIONS, PATCH |
| Credenciales | **`allowCredentials: true`** (obligatorio con cookies HttpOnly) |
| Cabeceras | `Authorization`, `Content-Type`, `X-System-Key`, `Accept`, `Origin`, `X-Requested-With` |

**Importante:** no usar `*` en `Allow-Origin` cuando el front envía `withCredentials: true`.

El valor debe **coincidir** con **`APP_ALLOWED_ORIGINS`** en `ms-usuarios` para que login y refresh pasen la validación de **Origin/Referer**.

---

## Contrato con el frontend

El front define **`VITE_API_BASE_URL`** sin barra final (p. ej. `http://localhost:8080`).

Todas las llamadas al backend de usuarios usan:

```text
{VITE_API_BASE_URL}/usuarios/api/...
```

Ejemplos:

- Login paciente: `POST http://localhost:8080/usuarios/api/auth/login`
- Refresh: `POST http://localhost:8080/usuarios/api/auth/refresh`
- Provincias: `GET http://localhost:8080/usuarios/api/provincias`

Implementación en [`frontend-clinica/src/api/axiosConfig.js`](../frontend-clinica/src/api/axiosConfig.js).

---

## Docker

Imagen: [`api-gateway/Dockerfile`](../api-gateway/Dockerfile).

En el `docker-compose.yml` raíz:

- Depende de **`ms-usuarios`**.
- Expone **8080:8080**.
- Variable: **`APP_GATEWAY_CORS_ALLOWED_ORIGIN`**.

---

## Tests

Solo test de contexto de aplicación: `ApiGatewayApplicationTests.java`. No hay tests de integración gateway + MS en el repo actual.

---

## Futuros microservicios

Cuando se agreguen **turnos** o **historial**, conviene nuevas entradas en `spring.cloud.gateway.routes`, por ejemplo:

```yaml
- id: ms-turnos
  uri: http://ms-turnos:8082
  predicates:
    - Path=/turnos/**
  filters:
    - StripPrefix=1
```

Documentar cada prefijo en un `docs/API-<servicio>.md` y actualizar `VITE_API_BASE_URL` o rutas del front según diseño.
