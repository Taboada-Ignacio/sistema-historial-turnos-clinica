import React from 'react';
import { Link } from 'react-router-dom';
import PacientePortalNav from '../../components/PacientePortalNav';
import { HOME_PATH, PACIENTE_PATHS } from '../../utils/portalPaths';

const cardButtonClass =
  'inline-flex items-center justify-center px-4 py-2.5 rounded-lg border border-clinica-dark/35 bg-white text-clinica-dark text-sm font-semibold hover:bg-clinica-light hover:border-clinica-dark/50 transition-colors';

const PacienteModuloPlaceholder = ({ titulo, descripcion }) => (
  <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
    <PacientePortalNav />
    <main className="max-w-3xl mx-auto px-6 py-10">
      <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
        <h1 className="text-2xl font-black text-slate-900 mb-2">{titulo}</h1>
        <p className="text-slate-600 text-sm mb-6">{descripcion}</p>
        <div className="flex flex-wrap gap-3">
          <Link to={PACIENTE_PATHS.dashboard} className={cardButtonClass}>
            Volver al panel
          </Link>
          <Link to={HOME_PATH} className={cardButtonClass}>
            Volver al inicio
          </Link>
        </div>
      </section>
    </main>
  </div>
);

export default PacienteModuloPlaceholder;
