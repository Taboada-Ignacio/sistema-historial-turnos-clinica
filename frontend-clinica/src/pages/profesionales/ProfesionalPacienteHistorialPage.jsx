import React from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import ProfesionalPortalNav from '../../components/ProfesionalPortalNav';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';

const outlineButtonClass =
  'inline-flex items-center justify-center px-4 py-2.5 rounded-lg border border-blue-600/35 bg-white text-blue-700 text-sm font-semibold hover:bg-blue-50 hover:border-blue-600/50 transition-colors';

const ProfesionalPacienteHistorialPage = () => {
  const { id } = useParams();
  const location = useLocation();
  const mensaje = location.state?.mensaje;

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      <ProfesionalPortalNav />
      <main className="max-w-3xl mx-auto px-6 py-10">
        <Link to={PROFESIONAL_PATHS.pacientes} className={`${outlineButtonClass} mb-6`}>
          ← Volver a Mis pacientes
        </Link>

        {mensaje && (
          <div className="mb-6 rounded-xl border border-green-200 bg-green-50 px-4 py-3 text-sm text-green-800">
            {mensaje}
          </div>
        )}

        <section className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
          <p className="text-xs font-bold uppercase tracking-wider text-blue-600 mb-2">En desarrollo</p>
          <h1 className="text-2xl font-black text-gray-900 mb-2">Historial clínico</h1>
          <p className="text-sm text-gray-600 mb-4">
            Paciente ID: <span className="font-mono font-semibold">{id}</span>
          </p>
          <p className="text-gray-600 text-sm leading-relaxed">
            El microservicio de historial clínico se integrará próximamente. Desde aquí podrás registrar evoluciones,
            estudios y recetas para este paciente.
          </p>
          <div className="mt-6 flex flex-wrap gap-3">
            <Link to={PROFESIONAL_PATHS.pacienteDetalle(id)} className={outlineButtonClass}>
              Ver ficha de la persona
            </Link>
            <Link to={PROFESIONAL_PATHS.pacientesBuscar} className={outlineButtonClass}>
              Buscar otro paciente
            </Link>
          </div>
        </section>
      </main>
    </div>
  );
};

export default ProfesionalPacienteHistorialPage;
