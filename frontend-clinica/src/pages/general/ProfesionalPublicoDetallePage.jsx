import React, { useEffect, useState } from 'react';
import { Link, useParams } from 'react-router-dom';
import { clienteAxiosPublic } from '../../api/axiosConfig';
import { formatEspecialidad } from '../../utils/formatEspecialidad';
import { HOME_PATH, PUBLIC_PATHS, PACIENTE_PATHS } from '../../utils/portalPaths';
import ProfesionalFotoPublic from '../../components/ProfesionalFotoPublic';
import { nombreProfesionalPresentacion } from '../../utils/profesionalPresentacion';

const ProfesionalPublicoDetallePage = () => {
  const { idProfesional } = useParams();
  const [profesional, setProfesional] = useState(null);
  const [cargando, setCargando] = useState(true);
  const [noEncontrado, setNoEncontrado] = useState(false);

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setCargando(true);
      setNoEncontrado(false);
      try {
        const { data } = await clienteAxiosPublic.get(
          `/usuarios/api/profesionales/${idProfesional}/presentacion`
        );
        if (!cancelled) setProfesional(data);
      } catch (err) {
        if (!cancelled) {
          setProfesional(null);
          if (err.response?.status === 404) setNoEncontrado(true);
        }
      } finally {
        if (!cancelled) setCargando(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, [idProfesional]);

  const nombre = nombreProfesionalPresentacion(profesional);
  const espLabel = formatEspecialidad(profesional?.especialidad);

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
      <nav className="bg-white border-b border-slate-200 px-6 py-4">
        <div className="max-w-3xl mx-auto flex flex-wrap items-center gap-4 text-sm">
          <Link to={HOME_PATH} className="text-slate-500 hover:text-clinica-dark font-semibold">
            Inicio
          </Link>
          <span className="text-slate-300">/</span>
          <Link
            to={PUBLIC_PATHS.catalogoProfesionales}
            className="text-slate-500 hover:text-clinica-dark font-semibold"
          >
            Profesionales
          </Link>
        </div>
      </nav>

      <main className="max-w-3xl mx-auto px-6 py-10">
        {cargando ? (
          <div className="animate-pulse space-y-4">
            <div className="w-32 h-32 rounded-2xl bg-slate-200" />
            <div className="h-8 w-64 bg-slate-200 rounded-lg" />
            <div className="h-4 w-40 bg-slate-200 rounded-lg" />
          </div>
        ) : noEncontrado || !profesional ? (
          <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center">
            <h1 className="text-xl font-black text-slate-900 mb-2">Profesional no encontrado</h1>
            <p className="text-slate-600 mb-6">
              La ficha no está disponible en el catálogo público o el enlace no es válido.
            </p>
            <Link
              to={PUBLIC_PATHS.catalogoProfesionales}
              className="inline-block px-6 py-3 rounded-xl bg-clinica-dark text-white font-bold hover:bg-clinica-hover transition-colors"
            >
              Volver al catálogo
            </Link>
          </div>
        ) : (
          <article className="bg-white rounded-3xl border border-slate-200 shadow-sm overflow-hidden">
            <div className="p-8 md:p-10 flex flex-col sm:flex-row gap-8 items-start">
              <ProfesionalFotoPublic
                fotoPerfil={profesional.fotoPerfil}
                alt={nombre ? `Foto de ${nombre}` : 'Foto de perfil'}
                className="w-32 h-32 rounded-2xl object-cover border-2 border-slate-100 shrink-0"
                placeholderClassName="w-32 h-32 rounded-2xl border-2 border-slate-200 bg-slate-100 flex items-center justify-center text-xs font-bold text-slate-400 shrink-0"
              />
              <div className="min-w-0 flex-1">
                <h1 className="text-2xl md:text-3xl font-black text-slate-900 leading-tight">
                  {nombre || 'Profesional'}
                </h1>
                {espLabel && (
                  <p className="text-lg font-semibold text-blue-700 mt-2">{espLabel}</p>
                )}
                {profesional.direccion && (
                  <div className="mt-6">
                    <h2 className="text-xs font-bold uppercase tracking-wider text-slate-400 mb-1">
                      Ubicación de atención
                    </h2>
                    <p className="text-slate-700 leading-relaxed">{profesional.direccion}</p>
                  </div>
                )}
                <p className="mt-8 text-sm text-slate-500 leading-relaxed">
                  Esta ficha es informativa. Para solicitar un turno, ingresá al{' '}
                  <Link to={PACIENTE_PATHS.login} className="text-clinica-dark font-semibold hover:underline">
                    portal paciente
                  </Link>
                  .
                </p>
              </div>
            </div>
          </article>
        )}
      </main>
    </div>
  );
};

export default ProfesionalPublicoDetallePage;
