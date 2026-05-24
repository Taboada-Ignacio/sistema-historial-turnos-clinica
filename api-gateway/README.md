# API Gateway

Punto de entrada HTTP (**puerto 8080**) entre el frontend y los microservicios.

## Arranque

```bash
# Con stack completo (recomendado)
docker compose up api-gateway

# Solo gateway (requiere ms-usuarios accesible)
cd api-gateway && mvn spring-boot:run
```

## Documentación

- [docs/API-GATEWAY.md](../docs/API-GATEWAY.md) — rutas, CORS, Swagger, contrato con el front
- [docs/ARQUITECTURA.md](../docs/ARQUITECTURA.md) — diagrama y puertos del sistema

## Resumen

| Cliente | Destino MS |
|---------|------------|
| `/usuarios/api/**` | `ms-usuarios` → `/api/**` (StripPrefix) |
| `/v3/api-docs/ms-usuarios` | OpenAPI de usuarios |

Variable clave: **`APP_GATEWAY_CORS_ALLOWED_ORIGIN`** (default `http://localhost:5173`).
