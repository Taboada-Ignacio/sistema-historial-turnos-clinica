import React from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { clearSession } from '../utils/auth';
import { HOME_PATH, PROFESIONAL_PATHS } from '../utils/portalPaths';

const ProfesionalPortalNav = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    clearSession();
    navigate(PROFESIONAL_PATHS.login);
  };

  return (
    <nav className="bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center shadow-sm">
      <Link to={PROFESIONAL_PATHS.dashboard} className="flex items-center gap-3 text-blue-600">
        <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
          <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
            <circle cx="9" cy="7" r="4" />
            <path d="M22 21v-2a4 4 0 0 0-3-3.87" />
          </svg>
        </div>
        <span className="font-black text-xl tracking-tight uppercase">Portal Staff</span>
      </Link>

      <div className="flex items-center gap-2">
        <Link
          to={HOME_PATH}
          className="px-4 py-2 text-sm font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg transition-all border border-slate-200"
        >
          Volver al inicio
        </Link>
        <button
          type="button"
          onClick={handleLogout}
          className="px-4 py-2 text-sm font-bold text-blue-700 bg-blue-50 hover:bg-blue-100 rounded-lg transition-all border border-blue-100"
        >
          Cerrar Sesión
        </button>
      </div>
    </nav>
  );
};

export default ProfesionalPortalNav;
