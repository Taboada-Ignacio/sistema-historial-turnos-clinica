import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { clearSession } from '../../utils/auth';
import { HOME_PATH, PROFESIONAL_PATHS } from '../../utils/portalPaths';

const DashboardProfesional = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    clearSession();
    // Redirigimos específicamente al login de profesionales
    navigate(PROFESIONAL_PATHS.login);
  };

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      
      {/* Navbar con estilo Staff */}
      <nav className="bg-white border-b border-gray-200 px-6 py-4 flex justify-between items-center shadow-sm">
        <div className="flex items-center gap-3 text-blue-600">
          <div className="w-10 h-10 rounded-xl bg-blue-50 flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
              <circle cx="9" cy="7" r="4"></circle>
              <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
            </svg>
          </div>
          <span className="font-black text-xl tracking-tight uppercase">Portal Staff</span>
        </div>
        
        <div className="flex items-center gap-3">
          <Link
            to={HOME_PATH}
            className="px-4 py-2 text-sm font-bold text-slate-700 bg-slate-100 hover:bg-slate-200 rounded-lg transition-all border border-slate-200 active:scale-95"
          >
            Volver al inicio
          </Link>
          <button 
            onClick={handleLogout}
            className="px-4 py-2 text-sm font-bold text-blue-700 bg-blue-50 hover:bg-blue-100 rounded-lg transition-all border border-blue-100 active:scale-95"
          >
            Cerrar Sesión
          </button>
        </div>
      </nav>

      {/* Contenido principal */}
      <main className="max-w-7xl mx-auto px-6 py-10">
        <header className="mb-10">
          <h1 className="text-4xl font-black text-gray-900 mb-2">Panel de Control Médico</h1>
          <p className="text-gray-500 font-medium">Bienvenido, Dr./Dra. Gestioná tu agenda y pacientes desde aquí.</p>
        </header>

        {/* Módulos de gestión profesional conectados a portalPaths */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          
          {/* Card: Agenda de Hoy */}
          <div className="bg-white p-8 rounded-3xl border border-gray-200 shadow-sm hover:shadow-lg transition-all group">
            <div className="bg-blue-50 w-12 h-12 rounded-2xl flex items-center justify-center mb-6 text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
            </div>
            <h3 className="font-black text-xl text-gray-900 mb-2">Agenda de Hoy</h3>
            <p className="text-gray-500 text-sm mb-6 leading-relaxed">Consultá tus turnos programados y gestioná tus horarios de atención.</p>
            <Link 
              to={PROFESIONAL_PATHS.turnos}
              className="text-blue-600 font-bold text-sm flex items-center gap-2 group-hover:translate-x-2 transition-transform"
            >
              Ver agenda <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </Link>
          </div>
          
          {/* Card: Mis Pacientes */}
          <div className="bg-white p-8 rounded-3xl border border-gray-200 shadow-sm hover:shadow-lg transition-all group">
            <div className="bg-blue-50 w-12 h-12 rounded-2xl flex items-center justify-center mb-6 text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            </div>
            <h3 className="font-black text-xl text-gray-900 mb-2">Mis Pacientes</h3>
            <p className="text-gray-500 text-sm mb-6 leading-relaxed">Accedé a las historias clínicas, estudios y evoluciones de tus pacientes.</p>
            <Link 
              to={PROFESIONAL_PATHS.pacientes}
              className="text-blue-600 font-bold text-sm flex items-center gap-2 group-hover:translate-x-2 transition-transform"
            >
              Gestionar pacientes <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </Link>
          </div>

          {/* Card: Configuración */}
          <div className="bg-white p-8 rounded-3xl border border-gray-200 shadow-sm hover:shadow-lg transition-all group">
            <div className="bg-blue-50 w-12 h-12 rounded-2xl flex items-center justify-center mb-6 text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>
            </div>
            <h3 className="font-black text-xl text-gray-900 mb-2">Configuración</h3>
            <p className="text-gray-500 text-sm mb-6 leading-relaxed">Ajustá tus datos profesionales, especialidades y preferencias de cuenta.</p>
            <Link 
              to={PROFESIONAL_PATHS.perfil}
              className="text-blue-600 font-bold text-sm flex items-center gap-2 group-hover:translate-x-2 transition-transform"
            >
              Editar ajustes <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round"><path d="M5 12h14M12 5l7 7-7 7"/></svg>
            </Link>
          </div>

        </div>
      </main>
    </div>
  );
};

export default DashboardProfesional;