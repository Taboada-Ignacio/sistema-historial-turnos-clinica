import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import imgProfesionales from "../../assets/images/staff-medico.webp";
import {
  savePortalSession,
  getUserEmailFromToken,
  hasActiveSession,
  getSessionPortal,
  getDashboardRouteByPortal,
} from '../../utils/auth';
// Importamos también HOME_PATH
import { PROFESIONAL_PATHS, HOME_PATH } from '../../utils/portalPaths';

const LoginProfesional = () => {
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const navigate = useNavigate();

  // Redirección automática si ya hay una sesión activa en el portal de profesionales
  useEffect(() => {
    if (hasActiveSession() && getSessionPortal() === 'profesional') {
      navigate(getDashboardRouteByPortal('profesional'));
    }
  }, [navigate]);

  const handleLogin = async (e) => {
    e.preventDefault();
    setError('');
    setLoading(true);

    try {
      const response = await clienteAxios.post('/usuarios/api/auth/login', {
        email,
        password,
        portal: 'profesional',
      });

      if (response.data && response.data.token) {
        const token = response.data.token;
        const emailFromToken = getUserEmailFromToken(token) || email;
        
        savePortalSession({
          token,
          portal: 'profesional',
          rememberMe,
          email: emailFromToken,
        });
        
        navigate(getDashboardRouteByPortal('profesional'));
      }
    } catch (err) {
      const data = err.response?.data || {};
      const msg = data.message || data.mensaje || 'Error de conexión.';
      const code = data.code;
      if (code === 'PORTAL_NO_PERMITIDO') {
        setError(msg || 'Esta cuenta no tiene acceso al portal profesional.');
      } else if (msg.toLowerCase().includes('activada')) {
        setError(msg);
      } else if (err.response?.status === 401) {
        setError('Credenciales de profesional inválidas.');
      } else {
        setError(msg);
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-white flex flex-col lg:flex-row font-sans">
      
      {/* LADO IZQUIERDO */}
      <div className="hidden lg:flex lg:w-1/2 relative bg-gradient-to-br from-blue-50 via-white to-blue-100 items-center justify-center overflow-hidden">
        <img 
          src={imgProfesionales} 
          alt="Staff Médico" 
          className="w-full h-full object-contain drop-shadow-2xl" 
        />
      </div>

      <div className="w-full lg:w-1/2 flex flex-col justify-center px-8 sm:px-16 md:px-24 lg:px-32 py-12 h-screen overflow-y-auto relative">
        
        {/* BOTÓN VOLVER USANDO RUTA CENTRALIZADA */}
        <Link to={HOME_PATH} className="absolute top-8 left-8 text-gray-400 hover:text-blue-600 flex items-center gap-2 font-medium transition-colors text-sm">
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M19 12H5M12 19l-7-7 7-7"/></svg>
          Volver
        </Link>

        <div className="max-w-md w-full mx-auto mt-8">
          
          <div className="mb-10">
            <div className="flex items-center gap-2 text-blue-600 mb-3">
               <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
              </svg>
              <span className="font-black uppercase tracking-widest text-sm">Portal Staff</span>
            </div>
            <h3 className="text-4xl md:text-5xl font-black text-gray-900 leading-tight">Bienvenido, Profesional</h3>
          </div>

          {error && (
            <div className="bg-amber-50 text-amber-700 p-4 rounded-2xl text-sm mb-6 border border-amber-200">
              {error}
            </div>
          )}

          <form onSubmit={handleLogin} className="space-y-6">
            <div>
              <label className="block text-xs font-bold text-gray-400 uppercase mb-2 ml-1">Email</label>
              <input
                type="email"
                required
                value={email}
                onChange={(e) => setEmail(e.target.value)}
                className="w-full px-5 py-4 rounded-2xl border border-gray-200 bg-gray-50/50 focus:bg-white focus:ring-4 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all"
                placeholder="nombre@clinica.com"
              />
            </div>

            <div>
              <label className="block text-xs font-bold text-gray-400 uppercase mb-2 ml-1">Contraseña</label>
              <div className="relative">
                <input
                  type={showPassword ? "text" : "password"}
                  required
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full px-5 py-4 rounded-2xl border border-gray-200 bg-gray-50/50 focus:bg-white focus:ring-4 focus:ring-blue-500/20 focus:border-blue-500 outline-none transition-all"
                  placeholder="••••••••"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-blue-600 transition-colors"
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
              <label className="flex items-center text-xs text-gray-500 cursor-pointer">
                <input
                  type="checkbox"
                  checked={rememberMe}
                  onChange={(e) => setRememberMe(e.target.checked)}
                  className="mr-2 rounded border-gray-300 text-blue-600 focus:ring-blue-600"
                />
                Recordarme
              </label>
              <Link to={PROFESIONAL_PATHS.recuperarPassword} className="text-xs font-bold text-blue-700 hover:underline">
                ¿Olvidaste tu contraseña?
              </Link>
            </div>

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 text-white font-black text-lg py-4 rounded-2xl hover:bg-blue-700 transition-all shadow-lg shadow-blue-600/30 active:scale-[0.98]"
            >
              {loading ? "Iniciando..." : "Ingresar al Portal"}
            </button>
          </form>

          <div className="mt-14 pt-10 border-t border-gray-200">
            <h4 className="text-gray-900 font-black text-2xl mb-3">¿Aún no sos parte del equipo?</h4>
            <p className="text-gray-500 text-base mb-8 leading-relaxed">
              Descubrí las herramientas avanzadas que tenemos para gestionar tu práctica médica, historia clínica de pacientes y turnos.
            </p>
            
            <Link 
              to={PROFESIONAL_PATHS.registro} 
              className="flex items-center justify-center w-full px-6 py-4 bg-blue-50 text-blue-700 font-black text-lg rounded-2xl hover:bg-blue-100 transition-colors border border-blue-200"
            >
              Registrarme como Profesional
            </Link>
          </div>

        </div>
      </div>
    </div>
  );
};

export default LoginProfesional;