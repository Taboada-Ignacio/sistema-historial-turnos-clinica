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

   Sin barra final. En desarrollo suele ser el **gateway**; si llamás directo a `ms-usuarios`, usá su puerto (p. ej. `8081` según `application.yml`).

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

## Documentación del proyecto

Ver el README en la raíz del repositorio: política de seguridad, variables de entorno y CI.
