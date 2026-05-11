import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { clearSession } from '../../utils/auth';
import { HOME_PATH, PACIENTE_PATHS } from '../../utils/portalPaths';

const DashboardPaciente = () => {
  const navigate = useNavigate();

  const handleLogout = () => {
    clearSession();
    // Redirigimos al login de paciente usando la ruta centralizada
    navigate(PACIENTE_PATHS.login); 
  };

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
      
      {/* Navbar simple y funcional */}
      <nav className="bg-white border-b border-slate-200 px-6 py-4 flex justify-between items-center shadow-sm">
        <div className="flex items-center gap-3 text-clinica-dark">
          <div className="w-10 h-10 rounded-xl bg-clinica-light flex items-center justify-center">
            <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.3" strokeLinecap="round" strokeLinejoin="round">
              <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"></path>
            </svg>
          </div>
          <span className="font-black text-xl tracking-tight">Portal Paciente</span>
        </div>
        
        <div className="flex items-center gap-2">
          <Link
            to={HOME_PATH}
            className="px-4 py-2 text-sm font-semibold text-slate-600 bg-slate-100 hover:bg-slate-200 rounded-lg transition-colors border border-slate-200"
          >
            Volver al inicio
          </Link>
          <button 
            type="button"
            onClick={handleLogout}
            className="px-4 py-2 text-sm font-bold text-red-600 bg-red-50 hover:bg-red-100 rounded-lg transition-colors border border-red-100"
          >
            Cerrar Sesión
          </button>
        </div>
      </nav>

      {/* Contenido principal del Dashboard */}
      <main className="max-w-7xl mx-auto px-6 py-10">
        <header className="mb-8">
          <h1 className="text-3xl font-black text-slate-900 mb-2">¡Hola! Bienvenido a tu panel</h1>
          <p className="text-slate-600">Desde acá podés gestionar tus turnos, estudios y perfil.</p>
        </header>

        {/* Grilla de opciones rápidas conectada a portalPaths */}
        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          
          {/* Card: Mis Turnos */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <h3 className="font-bold text-lg text-slate-900 mb-2">Mis Turnos</h3>
            <p className="text-slate-500 text-sm mb-4">Agendá nuevos turnos médicos o revisá los que ya tenés programados.</p>
            <Link 
              to={PACIENTE_PATHS.turnos}
              className="text-clinica-dark font-semibold text-sm hover:underline inline-flex items-center"
            >
              Ir a turnos <span className="ml-1">&rarr;</span>
            </Link>
          </div>
          
          {/* Card: Historial Clínico */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <h3 className="font-bold text-lg text-slate-900 mb-2">Historial Clínico</h3>
            <p className="text-slate-500 text-sm mb-4">Accedé a tus estudios, recetas y diagnósticos médicos previos.</p>
            <Link 
              to={PACIENTE_PATHS.historial}
              className="text-clinica-dark font-semibold text-sm hover:underline inline-flex items-center"
            >
              Ver historial <span className="ml-1">&rarr;</span>
            </Link>
          </div>

          {/* Card: Mi Perfil */}
          <div className="bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow">
            <h3 className="font-bold text-lg text-slate-900 mb-2">Mi Perfil</h3>
            <p className="text-slate-500 text-sm mb-4">Mantené tus datos personales y de contacto siempre actualizados.</p>
            <Link 
              to={PACIENTE_PATHS.perfil}
              className="text-clinica-dark font-semibold text-sm hover:underline inline-flex items-center"
            >
              Editar perfil <span className="ml-1">&rarr;</span>
            </Link>
          </div>

        </div>
      </main>
    </div>
  );
};

export default DashboardPaciente;