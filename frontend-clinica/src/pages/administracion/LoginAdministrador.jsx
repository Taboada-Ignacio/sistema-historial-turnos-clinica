import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { getUserEmailFromToken, saveAdminSession, clearSession } from '../../utils/auth';
import { ADMIN_PATHS, HOME_PATH } from '../../utils/portalPaths';

import PasswordVisibilityToggle from '../../components/PasswordVisibilityToggle';

const LoginAdministrador = () => {
  const navigate = useNavigate();
  const [email, setEmail] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);

  const handleLogin = async (event) => {
    event.preventDefault();
    setError('');
    setLoading(true);

    try {
      const loginResponse = await clienteAxios.post('/usuarios/api/auth/login', {
        email,
        password,
        portal: 'admin',
      });
      const token = loginResponse?.data?.token;

      if (!token) {
        setError('La respuesta del login no incluyó token.');
        return;
      }

      const emailFromToken = getUserEmailFromToken(token);
      if (!emailFromToken) {
        setError('No se pudo validar el usuario del token.');
        return;
      }

      saveAdminSession(token, emailFromToken);
      navigate(ADMIN_PATHS.profesionalesPendientes);
    } catch (err) {
      clearSession();
      const data = err.response?.data || {};
      const message = data.message || data.mensaje || 'No se pudo iniciar sesión.';
      setError(message);
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-6">
      <div className="bg-white w-full max-w-md rounded-2xl border border-slate-200 shadow-lg p-8">
        <p className="text-xs font-bold text-slate-500 uppercase tracking-widest mb-2">Administración</p>
        <h2 className="text-2xl font-black text-slate-900 mb-6">Ingreso de administradores</h2>
        
        <Link
          to={HOME_PATH}
          className="inline-block mb-4 text-sm font-semibold text-slate-600 hover:text-slate-900 underline"
        >
          Ir al inicio
        </Link>

        {error && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">
            {error}
          </div>
        )}

        <form onSubmit={handleLogin} className="space-y-4">
          <div>
            <label className="text-sm font-semibold text-slate-700">Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full mt-1 border border-slate-300 rounded-lg px-3 py-2 focus:ring-2 focus:ring-slate-900 focus:outline-none"
              placeholder="admin@clinica.com"
            />
          </div>
          <div>
            <label className="text-sm font-semibold text-slate-700">Contraseña</label>
            <div className="relative mt-1">
              <input
                type={showPassword ? 'text' : 'password'}
                required
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                className="w-full border border-slate-300 rounded-lg pl-3 pr-12 py-2 focus:ring-2 focus:ring-slate-900 focus:outline-none"
                placeholder="********"
              />
              <PasswordVisibilityToggle visible={showPassword} onToggle={() => setShowPassword(!showPassword)} />
            </div>
          </div>

          <div className="flex justify-end">
            <Link to={ADMIN_PATHS.recuperarPassword} className="text-xs font-bold text-slate-600 hover:underline">
              ¿Olvidaste tu contraseña?
            </Link>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-slate-900 text-white py-2.5 rounded-lg font-bold hover:bg-slate-800 disabled:opacity-70 transition-opacity"
          >
            {loading ? 'Validando...' : 'Ingresar'}
          </button>
        </form>

        <p className="mt-4 text-xs text-slate-500">Acceso interno. Ruta no pública.</p>
      </div>
    </div>
  );
};

export default LoginAdministrador;