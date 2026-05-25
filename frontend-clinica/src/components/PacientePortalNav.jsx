import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { clearSession } from '../utils/auth';
import { HOME_PATH, PACIENTE_PATHS } from '../utils/portalPaths';

const PacientePortalNav = () => {
  const navigate = useNavigate();

  const handleIrInicio = () => {
    navigate(HOME_PATH);
  };

  const handleLogout = () => {
    clearSession();
    navigate(PACIENTE_PATHS.login);
  };

  return (
    <nav className="bg-white border-b border-slate-200 px-6 py-4 flex justify-between items-center shadow-sm">
      <Link to={PACIENTE_PATHS.dashboard} className="flex items-center gap-3 text-clinica-dark">
        <div className="w-10 h-10 rounded-xl bg-clinica-light flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.3" strokeLinecap="round" strokeLinejoin="round">
            <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z" />
          </svg>
        </div>
        <span className="font-black text-xl tracking-tight">Portal Paciente</span>
      </Link>

      <div className="flex items-center gap-2">
        <button
          type="button"
          onClick={handleIrInicio}
          className="px-4 py-2 text-sm font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors border border-slate-200"
        >
          Volver al inicio
        </button>
        <button
          type="button"
          onClick={handleLogout}
          className="px-4 py-2 text-sm font-bold text-red-600 bg-red-50 hover:bg-red-100 rounded-lg transition-colors border border-red-100"
        >
          Cerrar Sesión
        </button>
      </div>
    </nav>
  );
};

export default PacientePortalNav;
