/** Base URL del gateway o del ms-usuarios (sin barra final). Configurar en build con VITE_API_BASE_URL. */
export const API_BASE_URL =
  import.meta.env.VITE_API_BASE_URL?.replace(/\/$/, '') || 'http://localhost:8080';
