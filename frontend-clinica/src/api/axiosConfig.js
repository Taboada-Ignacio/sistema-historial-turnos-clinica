import axios from 'axios';
import Swal from 'sweetalert2';
import { API_BASE_URL } from '../config/env';
import { getSessionToken, clearSession, updateSessionToken } from '../utils/auth';

const clienteAxios = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

/** Sin interceptores: evita bucles al refrescar el access token (cookie HttpOnly). */
const axiosSinInterceptores = axios.create({
  baseURL: API_BASE_URL,
  withCredentials: true,
});

const SKIP_REFRESH_ON_401 = [
  '/api/auth/login',
  '/api/auth/refresh',
  '/api/auth/cambiar-password',
  '/api/auth/solicitar-cambio-password',
  '/api/auth/cambiar-password-con-token',
  '/api/auth/confirmar-cambio-password',
];

const MODAL_SKIP_ON_401 = SKIP_REFRESH_ON_401;

// 1. INTERCEPTOR DE PETICIONES
clienteAxios.interceptors.request.use(
  (config) => {
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
    const requestUrl = originalRequest?.url || '';

    if (status !== 401 || !originalRequest) {
      if (error.response?.status === 400) {
        console.error('Error 400 - Detalles:', error.response.data);
      }
      return Promise.reject(error);
    }

    const skipRefresh = SKIP_REFRESH_ON_401.some((path) => requestUrl.includes(path));

    if (skipRefresh) {
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

    const showModal = !MODAL_SKIP_ON_401.some((path) => requestUrl.includes(path));
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
