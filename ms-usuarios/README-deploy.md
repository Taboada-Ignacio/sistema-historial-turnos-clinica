Deployment notes — cookies & refresh tokens
=========================================

Environment variables and properties
- APP_FRONTEND_URL — base URL of the React SPA (no trailing slash), used for HTTP redirects after email confirmation (`GET …/api/*/confirmar`). Maps to `app.frontend-url`. Example: `https://app.tuclinica.com` or `http://localhost:5173` in development.
- APP_COOKIE_SECURE (boolean) — when true, refresh cookie is set with Secure flag (only sent over HTTPS).
  - Default: false (suitable for local development).
  - In production you MUST set APP_COOKIE_SECURE=true.
- JWT_SECRET, JWT_EXPIRATION, APP_URL, etc. continue to be read from env vars as before.

How to enable Secure cookies in production
-----------------------------------------
1. Ensure your environment serves the app over HTTPS (TLS termination at load balancer / ingress).
2. Set APP_COOKIE_SECURE=true (e.g., in Kubernetes secret, Heroku config var, or CI/CD pipeline).
3. Ensure APP_URL is set to your public gateway URL if used for email links.

Other recommendations
---------------------
- SameSite is set to Lax by default; change to Strict only if you are sure no external navigation requires the cookie.
- Validate Origin/Referer on sensitive endpoints (e.g., POST /api/auth/refresh) for extra CSRF protection.
- Rotate JWT_SECRET if it was committed accidentally.
 
Temporary localhost setting
--------------------------
- By default this repo allows only localhost (http://localhost:5173) in the `app.allowed-origins` property to simplify local development.
- IMPORTANT: this is a development convenience — before deploying to production remove localhost from the allowed origins and configure `APP_ALLOWED_ORIGINS` with your real frontend origin(s).

CI (GitHub Actions)
-------------------
- Workflow: `.github/workflows/ms-usuarios-startup-check.yml`
- On push/PR to `main` or `master` (when `ms-usuarios/` changes), it starts PostgreSQL, runs `ms-usuarios/init.sql`, packages the app, and runs the JVM with `spring.profiles.active=prod` and `spring.main.web-application-type=none` so `StartupChecks` runs and the process exits.
- The workflow sets a **long non-placeholder** `JWT_SECRET` in the job environment for CI only (not your production secret). For deploy pipelines that target real environments, use repository/environment secrets instead of this inline value.

