import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { ADMIN_PATHS } from '../../../utils/portalPaths';
import { clearAdminOnboardingEmail } from '../../../utils/adminOnboarding';

/** Tras confirmar email de administrador: mensaje y redirección al login del panel. */
const AdminCuentaActivadaPage = () => {
  const navigate = useNavigate();

  useEffect(() => {
    clearAdminOnboardingEmail();
    const t = setTimeout(() => navigate(ADMIN_PATHS.login, { replace: true }), 4500);
    return () => clearTimeout(t);
  }, [navigate]);

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="bg-white p-10 rounded-xl shadow-2xl max-w-md w-full text-center border-t-4 border-red-600">
        <div className="text-5xl mb-4">✓</div>
        <h1 className="text-xl font-bold text-gray-900 uppercase tracking-tight mb-2">Cuenta activada</h1>
        <p className="text-gray-600 mb-6 text-sm">
          Tu correo fue verificado. Iniciá sesión con tu email y contraseña para entrar al panel de administración.
        </p>
        <Link
          to={ADMIN_PATHS.login}
          className="inline-block w-full py-3 rounded-lg bg-red-600 text-white font-bold hover:bg-red-700 transition-colors uppercase text-sm tracking-wide"
        >
          Ir al inicio de sesión
        </Link>
        <p className="text-xs text-gray-400 mt-4">Redirección automática en unos segundos…</p>
      </div>
    </div>
  );
};

export default AdminCuentaActivadaPage;
