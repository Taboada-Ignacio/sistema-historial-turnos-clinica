import axios from 'axios';
import Swal from 'sweetalert2';

const clienteAxios = axios.create({
  baseURL: 'http://localhost:8080', 
});

// 2. INTERCEPTOR DE PETICIONES
clienteAxios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    
    // Verificamos que el token exista y no sea una cadena "null" o "undefined"
    if (token && token !== 'null' && token !== 'undefined') {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    return config;
  },
  (error) => Promise.reject(error)
);

// 3. INTERCEPTOR DE RESPUESTAS
clienteAxios.interceptors.response.use(
  (response) => response,
  (error) => {
    // Si el error es 401 (No autorizado)
    if (error.response && error.response.status === 401) {
      
      // IMPORTANTE: Solo disparamos la alerta si NO estamos intentando loguearnos
      // para no interrumpir el flujo de login fallido normal.
      const isLoginRequest = error.config.url.includes('/api/auth/login');

      if (!isLoginRequest) {
        Swal.fire({
          icon: 'warning',
          title: 'Sesión expirada',
          text: 'Por seguridad, debes volver a ingresar.',
          confirmButtonText: 'Ir al Login',
          confirmButtonColor: '#307b36',
          allowOutsideClick: false
        }).then((result) => {
          if (result.isConfirmed) {
            localStorage.removeItem('token');
            // Redirigimos al inicio
            window.location.href = '/'; 
          }
        });
      }
    }
    
    // Manejo de error 400 para debugging (puedes quitarlo luego)
    if (error.response && error.response.status === 400) {
      console.error("Error 400 - Detalles:", error.response.data);
    }

    return Promise.reject(error);
  }
);

export default clienteAxios;