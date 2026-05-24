import React from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { clearSession } from '../../utils/auth';
import { formatEspecialidad } from '../../utils/formatEspecialidad';
import ProfesionalFoto from '../../components/ProfesionalFoto';
import { HOME_PATH, PROFESIONAL_PATHS } from '../../utils/portalPaths';
import { useProfesionalSession } from '../../context/ProfesionalSessionContext';

const MEMBRESIA_SIN_VERIFICAR = 'SIN_VERIFICAR';

const ModuloDashboard = ({ disabled, titulo, descripcion, linkTo, linkLabel, children }) => {
  const cardClass = disabled
    ? 'bg-gray-100 border-gray-200 opacity-75 cursor-not-allowed'
    : 'bg-white border-gray-200 shadow-sm hover:shadow-lg group';

  const contenido = (
    <>
      {children}
      <h3 className={`font-black text-xl mb-2 ${disabled ? 'text-gray-400' : 'text-gray-900'}`}>
        {titulo}
      </h3>
      <p className={`text-sm mb-6 leading-relaxed ${disabled ? 'text-gray-400' : 'text-gray-500'}`}>
        {descripcion}
      </p>
      {disabled ? (
        <span className="text-gray-400 font-bold text-sm flex items-center gap-2">
          {linkLabel}
        </span>
      ) : (
        <Link
          to={linkTo}
          className="text-blue-600 font-bold text-sm flex items-center gap-2 group-hover:translate-x-2 transition-transform"
        >
          {linkLabel}{' '}
          <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
            <path d="M5 12h14M12 5l7 7-7 7" />
          </svg>
        </Link>
      )}
    </>
  );

  return (
    <div className={`p-8 rounded-3xl border transition-all ${cardClass}`} aria-disabled={disabled}>
      {contenido}
    </div>
  );
};

const DashboardProfesional = () => {
  const navigate = useNavigate();
  const { presentacion, membresiaActual, cargandoPerfil } = useProfesionalSession();

  const pendienteVerificacionAdmin =
    membresiaActual?.toUpperCase() === MEMBRESIA_SIN_VERIFICAR;

  const handleLogout = () => {
    clearSession();
    navigate(PROFESIONAL_PATHS.login);
  };

  const especialidadLabel = presentacion?.especialidad
    ? formatEspecialidad(presentacion.especialidad)
    : '';

  const bienvenida = especialidadLabel
    ? `Bienvenido, ${especialidadLabel}. Gestioná tu agenda y pacientes desde aquí.`
    : 'Bienvenido. Gestioná tu agenda y pacientes desde aquí.';

  const nombreCompleto =
    presentacion?.apellido || presentacion?.nombre
      ? [presentacion.apellido, presentacion.nombre].filter(Boolean).join(', ')
      : null;

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      
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

      <main className="max-w-7xl mx-auto px-6 py-10">
        <header className="mb-10 flex flex-col sm:flex-row sm:items-center sm:justify-between gap-6">
          <div className="min-w-0 flex-1">
            <h1 className="text-4xl font-black text-gray-900 leading-tight">
              Panel de Control de Profesional de la Salud
            </h1>
            <p className="text-gray-500 font-medium mt-1">{bienvenida}</p>
          </div>

          <div className="shrink-0 flex flex-col items-center sm:items-end">
            {cargandoPerfil ? (
              <div className="flex flex-col items-center gap-2 animate-pulse">
                <div className="w-20 h-20 sm:w-24 sm:h-24 rounded-2xl bg-gray-200 shrink-0" />
                <div className="h-5 w-32 bg-gray-200 rounded-lg" />
              </div>
            ) : (
              <>
                <ProfesionalFoto
                  fotoPerfil={presentacion?.fotoPerfil}
                  alt={nombreCompleto ? `Foto de ${nombreCompleto}` : 'Foto de perfil'}
                  className="w-20 h-20 sm:w-24 sm:h-24 object-cover rounded-2xl border-2 border-blue-100 shadow-md shrink-0"
                  placeholderClassName="w-20 h-20 sm:w-24 sm:h-24 rounded-2xl border-2 border-gray-200 bg-gray-100 flex items-center justify-center text-xs font-bold text-gray-400 text-center px-2 shrink-0"
                />
                {nombreCompleto && (
                  <p className="text-center font-black text-gray-900 text-sm sm:text-base leading-tight max-w-[10rem] sm:max-w-[6.5rem] mt-2">
                    {nombreCompleto}
                  </p>
                )}
              </>
            )}
          </div>
        </header>

        {!cargandoPerfil && pendienteVerificacionAdmin && (
          <div
            role="alert"
            className="mb-8 rounded-2xl border border-amber-300 bg-amber-50 px-6 py-4 text-amber-900"
          >
            <p className="font-bold text-amber-950 mb-1">Cuenta pendiente de verificación</p>
            <p className="text-sm text-amber-800 leading-relaxed">
              Confirmaste tu correo, pero un administrador aún no verificó tu matrícula profesional.
              Hasta entonces, la agenda y la gestión de pacientes no están disponibles. Te avisaremos
              cuando tu cuenta quede habilitada.
            </p>
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-8">
          <ModuloDashboard
            disabled={pendienteVerificacionAdmin}
            titulo="Agenda de Hoy"
            descripcion="Consultá tus turnos programados y gestioná tus horarios de atención."
            linkTo={PROFESIONAL_PATHS.turnos}
            linkLabel="Ver agenda"
          >
            <div
              className={`w-12 h-12 rounded-2xl flex items-center justify-center mb-6 transition-colors ${
                pendienteVerificacionAdmin
                  ? 'bg-gray-200 text-gray-400'
                  : 'bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white'
              }`}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect><line x1="16" y1="2" x2="16" y2="6"></line><line x1="8" y1="2" x2="8" y2="6"></line><line x1="3" y1="10" x2="21" y2="10"></line></svg>
            </div>
          </ModuloDashboard>

          <ModuloDashboard
            disabled={pendienteVerificacionAdmin}
            titulo="Mis Pacientes"
            descripcion="Accedé a las historias clínicas, estudios y evoluciones de tus pacientes."
            linkTo={PROFESIONAL_PATHS.pacientes}
            linkLabel="Gestionar pacientes"
          >
            <div
              className={`w-12 h-12 rounded-2xl flex items-center justify-center mb-6 transition-colors ${
                pendienteVerificacionAdmin
                  ? 'bg-gray-200 text-gray-400'
                  : 'bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white'
              }`}
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17 21v-2a4 4 0 0 0-4-4H5a4 4 0 0 0-4 4v2"></path><circle cx="9" cy="7" r="4"></circle><path d="M23 21v-2a4 4 0 0 0-3-3.87"></path><path d="M16 3.13a4 4 0 0 1 0 7.75"></path></svg>
            </div>
          </ModuloDashboard>

          <ModuloDashboard
            disabled={false}
            titulo="Configuración"
            descripcion="Ajustá tus datos profesionales, especialidades y preferencias de cuenta."
            linkTo={PROFESIONAL_PATHS.perfil}
            linkLabel="Editar ajustes"
          >
            <div className="bg-blue-50 w-12 h-12 rounded-2xl flex items-center justify-center mb-6 text-blue-600 group-hover:bg-blue-600 group-hover:text-white transition-colors">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M12 20h9"></path><path d="M16.5 3.5a2.121 2.121 0 0 1 3 3L7 19l-4 1 1-4L16.5 3.5z"></path></svg>
            </div>
          </ModuloDashboard>
        </div>
      </main>
    </div>
  );
};

export default DashboardProfesional;
