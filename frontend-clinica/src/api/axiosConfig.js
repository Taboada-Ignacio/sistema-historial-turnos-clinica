import axios from 'axios';
import Swal from 'sweetalert2';

// 1. Creamos una instancia de Axios con la URL base de tu API Gateway
const clienteAxios = axios.create({
  baseURL: 'http://localhost:8080', 
});

// 2. INTERCEPTOR DE PETICIONES (REQUEST)
// Esto inyecta el Token en CADA petición que hagas al backend automáticamente
clienteAxios.interceptors.request.use(
  (config) => {
    const token = localStorage.getItem('token');
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    return config;
  },
  (error) => {
    return Promise.reject(error);
  }
);

// 3. INTERCEPTOR DE RESPUESTAS (RESPONSE)
// Aquí detectamos si el backend nos patea por token expirado (Error 401)
clienteAxios.interceptors.response.use(
  (response) => {
    // Si todo va bien, simplemente devolvemos la respuesta
    return response;
  },
  (error) => {
    // Si hay un error, verificamos si es un 401 (No Autorizado)
    if (error.response && error.response.status === 401) {
      
      // Opcional: Si tu backend manda un código específico como "TOKEN_EXPIRED", 
      // puedes validarlo así: if (error.response.data.code === 'TOKEN_EXPIRED') { ... }

      // Disparamos la alerta bonita
      Swal.fire({
        icon: 'warning',
        title: 'Tu sesión ha expirado',
        text: 'Por seguridad, debes volver a iniciar sesión.',
        confirmButtonText: 'Ir al Login',
        confirmButtonColor: '#307b36', // El verde oscuro de tu logo
        allowOutsideClick: false // Obliga al usuario a hacer clic en el botón
      }).then((result) => {
        if (result.isConfirmed) {
          // Borramos el token viejo
          localStorage.removeItem('token');
          // Redirigimos al Login. Usamos window.location porque 
          // useNavigate() de React Router no funciona fuera de los componentes.
          window.location.href = '/'; 
        }
      });
    }
    return Promise.reject(error);
  }
);

export default clienteAxios;