import React from 'react';
import { Link, Outlet, useLocation, useNavigate } from 'react-router-dom';
import { clearSession } from '../../utils/auth';
import { ADMIN_PATHS } from '../../utils/adminPaths';

const navItems = [
  {
    label: 'Profesionales pendientes',
    path: ADMIN_PATHS.profesionalesPendientes,
  },
  {
    label: 'Administrar entidades',
    path: ADMIN_PATHS.entidades,
  },
  {
    label: 'Turnos',
    path: ADMIN_PATHS.turnos,
  },
  {
    label: 'Historiales clínicos',
    path: ADMIN_PATHS.historiales,
  },
];

const AdminLayout = () => {
  const navigate = useNavigate();
  const location = useLocation();

  const handleLogout = () => {
    clearSession();
    navigate(ADMIN_PATHS.login);
  };

  return (
    <div className="min-h-screen bg-gray-50">
      <nav className="bg-slate-900 text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 py-4 flex flex-col gap-4 md:flex-row md:items-center md:justify-between">
          <div>
            <h1 className="text-lg md:text-xl font-black tracking-wide">Panel de Administración</h1>
            <p className="text-slate-300 text-sm">Gestión centralizada del sistema clínico</p>
          </div>
          <button
            type="button"
            onClick={handleLogout}
            className="bg-slate-700 hover:bg-slate-600 px-4 py-2 rounded-lg text-sm font-semibold"
          >
            Cerrar sesión
          </button>
        </div>
      </nav>

      <div className="max-w-7xl mx-auto px-4 py-8">
        <div className="grid grid-cols-1 md:grid-cols-4 gap-3 mb-8">
          {navItems.map((item) => {
            const isActive = location.pathname === item.path;
            return (
              <Link
                key={item.path}
                to={item.path}
                className={`rounded-xl border p-4 text-sm font-semibold transition-colors ${
                  isActive
                    ? 'bg-slate-900 text-white border-slate-900'
                    : 'bg-white text-slate-700 border-slate-200 hover:border-slate-400'
                }`}
              >
                {item.label}
              </Link>
            );
          })}
        </div>

        <Outlet />
      </div>
    </div>
  );
};

export default AdminLayout;
