import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import clienteAxios from '../api/axiosConfig';

const Login = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();
  
  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await clienteAxios.post('/usuarios/api/auth/login', {
        email,
        password
      });

      if (response.data && response.data.token) {
        // Persistencia del token para los interceptores de Axios
        localStorage.setItem('token', response.data.token);
        
        // Redirección al área privada
        navigate('/dashboard');
      } else {
        setError('Error en el formato de respuesta del servidor.');
      }

    } catch (err) {
      if (err.response) {
        // Errores controlados por el backend (401, 403, etc.)
        if (err.response.status === 401) {
          setError('Credenciales inválidas. Verificá tu email y contraseña.');
        } else {
          setError(`Error del servidor: ${err.response.status}`);
        }
      } else if (err.request) {
        // Error de conectividad o CORS
        setError('No se pudo conectar con el servidor. Revisá el estado del Gateway.');
      } else {
        setError('Ocurrió un error inesperado al intentar ingresar.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-clinica-light flex items-center justify-center p-4 font-sans">
      <div className="bg-white max-w-md w-full rounded-3xl shadow-xl overflow-hidden">
        
        <div className="p-8">
          <div className="text-center mb-8">
            <div className="flex justify-center items-center gap-2 mb-2 text-clinica-dark">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
              </svg>
              <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="currentColor" className="mt-2">
                <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"></path>
              </svg>
            </div>
            <h1 className="text-4xl font-bold text-clinica-dark tracking-tight">Salud</h1>
            <p className="text-gray-500 mt-2 text-sm text-balance leading-relaxed">Ingresá a tu portal médico</p>
          </div>

          {error && (
            <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm mb-6 text-center border border-red-100 animate-pulse">
              {error}
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-6">
            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Correo Electrónico
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-clinica-dark focus:border-transparent transition-all"
                placeholder="ejemplo@correo.com"
              />
            </div>

            <div>
              <label className="block text-sm font-medium text-gray-700 mb-1">
                Contraseña
              </label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-clinica-dark focus:border-transparent transition-all"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-clinica-dark transition-colors"
                >
                  {showPassword ? (
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                  )}
                </button>
              </div>
            </div>

            <div className="flex items-center justify-between">
              <label className="flex items-center text-sm text-gray-600 cursor-pointer group">
                <input type="checkbox" className="mr-2 rounded text-clinica-dark focus:ring-clinica-dark border-gray-300 transition-colors" />
                <span className="group-hover:text-clinica-dark">Recordarme</span>
              </label>
              <a href="#" className="text-sm text-clinica-dark hover:underline font-medium">
                ¿Olvidaste tu contraseña?
              </a>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-clinica-dark text-white font-semibold py-3 rounded-xl hover:bg-clinica-hover transition-all shadow-lg shadow-clinica-dark/30 disabled:opacity-70 disabled:cursor-wait flex justify-center items-center"
            >
              {loading ? (
                <span className="animate-pulse">Verificando...</span>
              ) : (
                "Ingresar"
              )}
            </button>
          </form>
        </div>
        
        <div className="bg-gray-50 p-6 text-center border-t border-gray-100">
          <p className="text-sm text-gray-600">
            ¿No tenés una cuenta?{' '}
            <Link to="/registro" className="text-clinica-dark font-semibold hover:underline">
              Registrate aquí
            </Link>
          </p>
        </div>

      </div>
    </div>
  );
};

export default Login;