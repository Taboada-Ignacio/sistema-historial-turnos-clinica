import React from 'react';
import { Link } from 'react-router-dom';
import ProfesionalPortalNav from '../../components/ProfesionalPortalNav';
import { useProfesionalSession } from '../../context/ProfesionalSessionContext';
import { HOME_PATH, PROFESIONAL_PATHS } from '../../utils/portalPaths';

const MEMBRESIA_SIN_VERIFICAR = 'SIN_VERIFICAR';

const cardButtonClass =
  'inline-flex items-center justify-center px-4 py-2.5 rounded-lg border border-blue-600/35 bg-white text-blue-700 text-sm font-semibold hover:bg-blue-50 hover:border-blue-600/50 transition-colors';

const ProfesionalModuloPlaceholder = ({ titulo, descripcion, funcionalidadesPrevistas = [] }) => {
  const { membresiaActual, cargandoPerfil } = useProfesionalSession();
  const pendienteVerificacion =
    !cargandoPerfil && membresiaActual?.toUpperCase() === MEMBRESIA_SIN_VERIFICAR;

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      <ProfesionalPortalNav />
      <main className="max-w-3xl mx-auto px-6 py-10">
        {pendienteVerificacion && (
          <div
            role="alert"
            className="mb-6 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-amber-900 text-sm"
          >
            <p className="font-bold text-amber-950 mb-1">Cuenta pendiente de verificación</p>
            <p className="leading-relaxed">
              Este módulo estará disponible cuando un administrador habilite tu matrícula profesional.
            </p>
          </div>
        )}

        <section className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
          <p className="text-xs font-bold uppercase tracking-wider text-blue-600 mb-2">En desarrollo</p>
          <h1 className="text-2xl font-black text-gray-900 mb-2">{titulo}</h1>
          <p className="text-gray-600 text-sm mb-6 leading-relaxed">{descripcion}</p>

          {funcionalidadesPrevistas.length > 0 && (
            <div className="mb-6 rounded-xl border border-slate-200 bg-slate-50 px-4 py-4">
              <p className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">
                Próximas funcionalidades
              </p>
              <ul className="space-y-2 text-sm text-slate-700">
                {funcionalidadesPrevistas.map((item) => (
                  <li key={item} className="flex items-start gap-2">
                    <span className="mt-1.5 h-1.5 w-1.5 shrink-0 rounded-full bg-blue-500" aria-hidden />
                    {item}
                  </li>
                ))}
              </ul>
            </div>
          )}

          <div className="flex flex-wrap gap-3">
            <Link to={PROFESIONAL_PATHS.dashboard} className={cardButtonClass}>
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
};

export default ProfesionalModuloPlaceholder;
