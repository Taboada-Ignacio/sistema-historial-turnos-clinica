import axios from 'axios';
import Swal from 'sweetalert2';
import { getSessionToken, clearSession } from '../utils/auth';

const clienteAxios = axios.create({
  baseURL: 'http://localhost:8080', 
});

// 1. INTERCEPTOR DE PETICIONES
// Se encarga de adjuntar el token JWT a cada solicitud saliente
clienteAxios.interceptors.request.use(
  (config) => {
    const token = getSessionToken();
    
    // Verificamos que el token sea válido antes de enviarlo
    if (token && token !== 'null' && token !== 'undefined') {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

// 2. INTERCEPTOR DE RESPUESTAS
// Maneja errores globales, especialmente la expiración de sesión (401)
clienteAxios.interceptors.response.use(
  (response) => response,
  (error) => {
    // Hardening: Extraemos la URL de forma segura para evitar errores de referencia
    const requestUrl = error?.config?.url || '';

    // Si el error es 401 (No autorizado)
    if (error.response && error.response.status === 401) {
      
      // Lista de rutas que NO deben disparar el modal (ej: flujos de auth o recuperación)
      // Nota: .includes() cubrirá subrutas como /paciente o /profesional automáticamente
      const AUTH_EXCEPTIONS = [
        '/api/auth/login',
        '/api/auth/cambiar-password',
        '/api/auth/solicitar-cambio-password',
        '/api/auth/cambiar-password-con-token',
        '/api/auth/confirmar-cambio-password'
      ];

      const isAuthException = AUTH_EXCEPTIONS.some(path => requestUrl.includes(path));

      if (!isAuthException) {
        Swal.fire({
          icon: 'warning',
          title: 'Sesión expirada',
          text: 'Por seguridad, debes volver a ingresar.',
          confirmButtonText: 'Ir al Login',
          confirmButtonColor: '#307b36',
          allowOutsideClick: false
        }).then((result) => {
          if (result.isConfirmed) {
            clearSession(); // Limpieza unificada de storages
            window.location.href = '/'; 
          }
        });
      }
    }
    
    // Manejo de error 400 para debugging en desarrollo
    if (error.response && error.response.status === 400) {
      console.error("Error 400 - Detalles:", error.response.data);
    }

    return Promise.reject(error);
  }
);

export default clienteAxios;