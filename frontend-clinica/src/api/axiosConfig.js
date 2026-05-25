import axios from 'axios';
import Swal from 'sweetalert2';
import { API_BASE_URL } from '../config/env';
import { getSessionToken, clearSession, updateSessionToken } from '../utils/auth';

const clienteAxios = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

/** Sin interceptores: refresh manual y flujos 100% públicos (sin Bearer). */
const axiosSinInterceptores = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

/** Evita que un JWT guardado en el navegador provoque 401 en rutas públicas de auth. */
axiosSinInterceptores.interceptors.request.use((config) => {
  if (config.headers) {
    delete config.headers.Authorization;
    delete config.headers.authorization;
  }
  return config;
});

/**
 * Cliente para rutas públicas (recuperación de contraseña, etc.): no envía JWT ni reintenta refresh en 401.
 */
export const clienteAxiosPublic = axiosSinInterceptores;

/** Resuelve path completo para matchear con URLs relativas o absolutas según versión de Axios. */
function requestFullPath(config) {
  if (!config) return '';
  const url = config.url || '';
  if (url.startsWith('http')) return url;
  const base = (config.baseURL || '').replace(/\/$/, '');
  const path = url.startsWith('/') ? url : `/${url}`;
  return `${base}${path}`;
}

const SKIP_REFRESH_ON_401 = [
  '/api/auth/login',
  '/api/auth/refresh',
  '/api/auth/cambiar-password',
  '/api/auth/solicitar-cambio-password',
  '/api/auth/cambiar-password-con-token',
  '/api/auth/confirmar-cambio-password',
  '/api/auth/reenviar-acceso-paciente',
  '/api/auth/datos-activacion-paciente',
  '/api/auth/establecer-password-inicial',
  '/api/seguridad/verificar-password-actual',
];

/** Rutas que no deben enviar JWT ni disparar refresh/modal de sesión en 401. */
function isPublicUsuarioApiPath(config) {
  const p = requestFullPath(config);
  if (!p) return false;
  if (SKIP_REFRESH_ON_401.some((fragment) => p.includes(fragment))) {
    return true;
  }
  if (p.includes('/api/onboarding/admin')) {
    return true;
  }
  if (p.includes('/registro') || p.includes('/confirmar') || p.includes('/reenviar-confirmacion')) {
    return true;
  }
  if (
    p.includes('/api/provincias') ||
    p.includes('/api/localidades') ||
    p.includes('/api/direcciones') ||
    p.includes('/api/especialidades') ||
    p.includes('/api/obras-sociales') ||
    p.includes('/api/roles') ||
    p.includes('/api/estados') ||
    p.includes('/api/profesionales/presentacion') ||
    p.includes('/api/profesionales/fotos/public/')
  ) {
    return true;
  }
  return false;
}

function shouldSkipAuthRecoveryHandling(config) {
  const p = requestFullPath(config);
  return (
    p.includes('/api/auth/solicitar-cambio-password') ||
    p.includes('/api/auth/cambiar-password-con-token')
  );
}

// 1. INTERCEPTOR DE PETICIONES
clienteAxios.interceptors.request.use(
  (config) => {
    if (isPublicUsuarioApiPath(config)) {
      if (config.headers) {
        delete config.headers.Authorization;
        delete config.headers.authorization;
      }
      return config;
    }

    const token = getSessionToken();

    if (token && token !== 'null' && token !== 'undefined') {
      config.headers.Authorization = `Bearer ${token}`;
    }

    return config;
  },
  (error) => Promise.reject(error)
);

// 2. INTERCEPTOR DE RESPUESTAS — refresh con cookie + reintento una vez
clienteAxios.interceptors.response.use(
  (response) => response,
  async (error) => {
    const originalRequest = error.config;
    const status = error.response?.status;

    if (status !== 401 || !originalRequest) {
      if (error.response?.status === 400 && !shouldSkipAuthRecoveryHandling(originalRequest)) {
        console.error('Error 400 - Detalles:', error.response.data);
      }
      return Promise.reject(error);
    }

    if (isPublicUsuarioApiPath(originalRequest)) {
      return Promise.reject(error);
    }

    if (!originalRequest._retry) {
      originalRequest._retry = true;
      try {
        const { data } = await axiosSinInterceptores.post('/usuarios/api/auth/refresh');
        const newToken = data?.token;
        if (!newToken) {
          throw new Error('Sin token en respuesta de refresh');
        }
        updateSessionToken(newToken);
        originalRequest.headers = originalRequest.headers || {};
        originalRequest.headers.Authorization = `Bearer ${newToken}`;
        return clienteAxios(originalRequest);
      } catch {
        clearSession();
        await Swal.fire({
          icon: 'warning',
          title: 'Sesión expirada',
          text: 'Por seguridad, debés volver a iniciar sesión.',
          confirmButtonText: 'Ir al inicio',
          confirmButtonColor: '#307b36',
          allowOutsideClick: false,
        });
        window.location.href = '/';
        return Promise.reject(error);
      }
    }

    const showModal = !isPublicUsuarioApiPath(originalRequest);
    if (showModal) {
      const code = error.response?.data?.code;
      const title = code === 'TOKEN_INVALID' ? 'Sesión no válida' : 'Sesión expirada';
      const text =
        code === 'TOKEN_INVALID'
          ? 'Tu sesión no pudo validarse. Volvé a iniciar sesión.'
          : 'Por seguridad, debés volver a ingresar.';
      Swal.fire({
        icon: 'warning',
        title,
        text,
        confirmButtonText: 'Ir al Login',
        confirmButtonColor: '#307b36',
        allowOutsideClick: false,
      }).then((result) => {
        if (result.isConfirmed) {
          clearSession();
          window.location.href = '/';
        }
      });
    }

    return Promise.reject(error);
  }
);

export default clienteAxios;
export { API_BASE_URL };
