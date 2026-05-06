import React, { useState } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import loginPacientesBg from '../../assets/images/login-pacientes.webp';

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
        localStorage.setItem('token', response.data.token);
        navigate('/dashboard');
      } else {
        setError('Error en el formato de respuesta del servidor.');
      }
    } catch (err) {
      if (err.response) {
        const serverMessage = err.response.data?.message || "";
        if (serverMessage.toLowerCase().includes("activada") || serverMessage.toLowerCase().includes("confirme")) {
          setError(serverMessage);
        } else if (err.response.status === 401) {
          setError('Credenciales inválidas. Verificá tu email y contraseña.');
        } else {
          setError(serverMessage || `Error del servidor: ${err.response.status}`);
        }
      } else {
        setError('No se pudo conectar con el servidor.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div
      className="min-h-screen font-sans bg-cover bg-center bg-no-repeat"
      style={{ backgroundImage: `url(${loginPacientesBg})` }}
    >
      <div className="min-h-screen max-w-7xl mx-auto px-4 sm:px-8 lg:px-10 flex items-center justify-center lg:justify-end">
        <div className="w-full max-w-md rounded-3xl overflow-hidden border border-white/70 bg-white/85 backdrop-blur-md shadow-[0_20px_70px_rgba(0,0,0,0.3)]">
          <div className="p-10">
          {/* Cabecera / Logo */}
          <div className="text-center mb-8">
            <div className="inline-flex justify-center items-center gap-2 mb-3 text-clinica-dark">
              <div className="h-12 w-12 rounded-xl bg-clinica-light flex items-center justify-center">
                <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.3" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
                </svg>
              </div>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="currentColor" className="mt-1">
                <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"></path>
              </svg>
            </div>
            <h1 className="text-4xl font-extrabold text-clinica-dark tracking-tight">Portal Paciente</h1>
            <p className="text-gray-600 mt-2 text-sm font-medium">Ingresá para gestionar turnos y seguimientos</p>
          </div>

          {error && (
            <div className={`p-4 rounded-xl text-sm mb-6 text-center border ${
              error.includes("activada") 
                ? "bg-amber-50 text-amber-700 border-amber-200" 
                : "bg-red-50 text-red-600 border-red-100"
            }`}>
              {error}
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-5">
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                Correo Electrónico
              </label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-5 py-3.5 rounded-2xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-clinica-dark focus:border-transparent transition-all bg-white/80"
                placeholder="ejemplo@correo.com"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase tracking-wider mb-2 ml-1">
                Contraseña
              </label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-5 py-3.5 rounded-2xl border border-gray-200 focus:outline-none focus:ring-2 focus:ring-clinica-dark focus:border-transparent transition-all bg-white/80"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-clinica-dark transition-colors"
                >
                  {showPassword ? (
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                  )}
                </button>
              </div>
            </div>

            <div className="flex items-center justify-between px-1">
              <label className="flex items-center text-xs text-gray-500 cursor-pointer group">
                <input type="checkbox" className="mr-2 rounded text-clinica-dark focus:ring-clinica-dark border-gray-300" />
                <span className="group-hover:text-clinica-dark transition-colors">Recordarme</span>
              </label>
              <a href="#" className="text-xs text-clinica-dark hover:underline font-bold">
                ¿Olvidaste tu contraseña?
              </a>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-clinica-dark text-white font-bold py-4 rounded-2xl hover:bg-clinica-hover transition-all shadow-lg shadow-clinica-dark/20 disabled:opacity-70 flex justify-center items-center mt-4 hover:-translate-y-0.5"
            >
              {loading ? "Verificando..." : "Ingresar"}
            </button>
          </form>
        </div>
        
        <div className="bg-white/55 p-6 text-center border-t border-gray-200">
          <p className="text-sm text-gray-600">
            ¿No tenés una cuenta?{' '}
            <Link to="/registro" className="text-clinica-dark font-bold hover:underline ml-1">
              Registrate aquí
            </Link>
          </p>
        </div>

        </div>
      </div>
    </div>
  );
};

export default Login;