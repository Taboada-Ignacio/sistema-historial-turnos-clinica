import React from 'react';
import { Link } from 'react-router-dom';
import PacientePortalNav from '../../components/PacientePortalNav';
import { usePacienteSession } from '../../context/PacienteSessionContext';
import { PACIENTE_PATHS } from '../../utils/portalPaths';

const cardButtonClass =
  'inline-flex items-center justify-center px-4 py-2.5 rounded-lg border border-clinica-dark/35 bg-white text-clinica-dark text-sm font-semibold group-hover:bg-clinica-light group-hover:border-clinica-dark/50 transition-colors';

const cardLinkClass =
  'block bg-white p-6 rounded-2xl border border-slate-200 shadow-sm hover:shadow-md transition-shadow group focus:outline-none focus-visible:ring-2 focus-visible:ring-clinica-dark/40';

const iconBoxClass =
  'w-12 h-12 rounded-xl bg-clinica-light text-clinica-dark flex items-center justify-center mb-4 group-hover:bg-clinica-light transition-colors';

const DashboardCard = ({ to, title, description, buttonLabel, icon }) => (
  <Link to={to} className={cardLinkClass}>
    <div className={iconBoxClass} aria-hidden="true">
      {icon}
    </div>
    <h3 className="font-bold text-lg text-slate-900 mb-2 group-hover:text-clinica-dark transition-colors">{title}</h3>
    <p className="text-slate-500 text-sm mb-4">{description}</p>
    <span className={cardButtonClass}>{buttonLabel}</span>
  </Link>
);

const DashboardPaciente = () => {
  const { sesion, cargandoPerfil } = usePacienteSession();

  const nombreCompleto =
    sesion?.apellido || sesion?.nombre
      ? [sesion.apellido, sesion.nombre].filter(Boolean).join(', ')
      : null;

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
      <PacientePortalNav />

      <main className="max-w-7xl mx-auto px-6 py-10">
        <header className="mb-8">
          {cargandoPerfil ? (
            <>
              <div className="h-9 w-72 bg-slate-200 rounded-lg animate-pulse mb-2" />
              <div className="h-5 w-96 bg-slate-100 rounded-lg animate-pulse" />
            </>
          ) : (
            <>
              <h1 className="text-3xl font-black text-slate-900 mb-2">
                {nombreCompleto ? `¡Hola, ${nombreCompleto}!` : '¡Hola! Bienvenido a tu panel'}
              </h1>
              <p className="text-slate-600">Desde acá podés gestionar tus turnos, estudios y perfil.</p>
            </>
          )}
        </header>

        <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
          <DashboardCard
            to={PACIENTE_PATHS.turnos}
            title="Mis Turnos"
            description="Agendá nuevos turnos médicos o revisá los que ya tenés programados."
            buttonLabel="Ir a turnos"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                <line x1="16" y1="2" x2="16" y2="6" />
                <line x1="8" y1="2" x2="8" y2="6" />
                <line x1="3" y1="10" x2="21" y2="10" />
              </svg>
            }
          />
          <DashboardCard
            to={PACIENTE_PATHS.historial}
            title="Historial Clínico"
            description="Accedé a tus estudios, recetas y diagnósticos médicos previos."
            buttonLabel="Ver historial"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
              </svg>
            }
          />
          <DashboardCard
            to={PACIENTE_PATHS.perfil}
            title="Mi Perfil"
            description="Mantené tus datos personales y de contacto siempre actualizados."
            buttonLabel="Editar perfil"
            icon={
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
                <circle cx="12" cy="7" r="4" />
              </svg>
            }
          />
        </div>
      </main>
    </div>
  );
};

export default DashboardPaciente;
