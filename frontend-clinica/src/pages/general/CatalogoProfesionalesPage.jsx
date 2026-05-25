import React, { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { clienteAxiosPublic } from '../../api/axiosConfig';
import { formatEspecialidad } from '../../utils/formatEspecialidad';
import { HOME_PATH, PUBLIC_PATHS } from '../../utils/portalPaths';
import ProfesionalFotoPublic from '../../components/ProfesionalFotoPublic';
import { nombreProfesionalPresentacion } from '../../utils/profesionalPresentacion';

const CatalogoProfesionalesPage = () => {
  const [profesionales, setProfesionales] = useState([]);
  const [cargando, setCargando] = useState(true);
  const [error, setError] = useState('');
  const [busqueda, setBusqueda] = useState('');
  const [especialidadFiltro, setEspecialidadFiltro] = useState('');

  useEffect(() => {
    let cancelled = false;

    (async () => {
      setCargando(true);
      setError('');
      try {
        const { data } = await clienteAxiosPublic.get('/usuarios/api/profesionales/presentacion');
        if (!cancelled) setProfesionales(Array.isArray(data) ? data : []);
      } catch {
        if (!cancelled) {
          setProfesionales([]);
          setError('No se pudo cargar el catálogo de profesionales. Intentá más tarde.');
        }
      } finally {
        if (!cancelled) setCargando(false);
      }
    })();

    return () => {
      cancelled = true;
    };
  }, []);

  const especialidadesUnicas = useMemo(() => {
    const set = new Set(profesionales.map((p) => p.especialidad).filter(Boolean));
    return [...set].sort((a, b) =>
      formatEspecialidad(a).localeCompare(formatEspecialidad(b), 'es')
    );
  }, [profesionales]);

  const filtrados = useMemo(() => {
    const q = busqueda.trim().toLowerCase();
    return profesionales.filter((p) => {
      if (especialidadFiltro && p.especialidad !== especialidadFiltro) return false;
      if (!q) return true;
      const nombre = nombreProfesionalPresentacion(p).toLowerCase();
      const esp = formatEspecialidad(p.especialidad).toLowerCase();
      const dir = (p.direccion || '').toLowerCase();
      return nombre.includes(q) || esp.includes(q) || dir.includes(q);
    });
  }, [profesionales, busqueda, especialidadFiltro]);

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
      <nav className="bg-white border-b border-slate-200 px-6 py-4">
        <div className="max-w-7xl mx-auto flex flex-col sm:flex-row sm:items-center sm:justify-between gap-4">
          <Link to={HOME_PATH} className="text-clinica-dark font-bold text-sm hover:underline">
            ← Volver al inicio
          </Link>
          <p className="text-xs font-bold uppercase tracking-wider text-slate-400">Catálogo público</p>
        </div>
      </nav>

      <main className="max-w-7xl mx-auto px-6 py-10">
        <header className="mb-8">
          <h1 className="text-3xl md:text-4xl font-black text-slate-900 mb-2">
            Profesionales de la salud
          </h1>
          <p className="text-slate-600 max-w-2xl leading-relaxed">
            Conocé a nuestro staff. Solo se muestran nombre, especialidad y ubicación de atención.
            No incluimos datos sensibles como email, DNI ni teléfono.
          </p>
        </header>

        <div className="mb-8 flex flex-col md:flex-row gap-4">
          <input
            type="search"
            value={busqueda}
            onChange={(e) => setBusqueda(e.target.value)}
            placeholder="Buscar por nombre, especialidad o ubicación…"
            className="flex-1 px-4 py-3 rounded-xl border border-slate-200 bg-white focus:ring-2 focus:ring-clinica-dark outline-none"
          />
          <select
            value={especialidadFiltro}
            onChange={(e) => setEspecialidadFiltro(e.target.value)}
            className="md:w-64 px-4 py-3 rounded-xl border border-slate-200 bg-white focus:ring-2 focus:ring-clinica-dark outline-none"
          >
            <option value="">Todas las especialidades</option>
            {especialidadesUnicas.map((esp) => (
              <option key={esp} value={esp}>
                {formatEspecialidad(esp)}
              </option>
            ))}
          </select>
        </div>

        {error && (
          <div role="alert" className="mb-6 rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-red-700 text-sm">
            {error}
          </div>
        )}

        {cargando ? (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {[1, 2, 3].map((i) => (
              <div key={i} className="h-56 rounded-2xl bg-slate-200 animate-pulse" />
            ))}
          </div>
        ) : filtrados.length === 0 ? (
          <div className="rounded-2xl border border-slate-200 bg-white p-10 text-center">
            <p className="text-slate-600 font-medium">
              {profesionales.length === 0
                ? 'Aún no hay profesionales publicados en el catálogo.'
                : 'No hay resultados con los filtros aplicados.'}
            </p>
          </div>
        ) : (
          <div className="grid grid-cols-1 sm:grid-cols-2 lg:grid-cols-3 gap-6">
            {filtrados.map((p) => {
              const nombre = nombreProfesionalPresentacion(p);
              const espLabel = formatEspecialidad(p.especialidad);
              return (
                <Link
                  key={p.idUsuario}
                  to={PUBLIC_PATHS.profesionalPublico(p.idUsuario)}
                  className="group bg-white rounded-2xl border border-slate-200 p-6 shadow-sm hover:shadow-lg hover:border-clinica-dark/30 transition-all flex flex-col"
                >
                  <ProfesionalFotoPublic
                    fotoPerfil={p.fotoPerfil}
                    alt={nombre ? `Foto de ${nombre}` : 'Foto de perfil'}
                    className="w-20 h-20 rounded-2xl object-cover border-2 border-slate-100 mb-4"
                    placeholderClassName="w-20 h-20 rounded-2xl border-2 border-slate-200 bg-slate-100 flex items-center justify-center text-[10px] font-bold text-slate-400 mb-4"
                  />
                  <h2 className="font-black text-lg text-slate-900 group-hover:text-clinica-dark transition-colors">
                    {nombre || 'Profesional'}
                  </h2>
                  {espLabel && (
                    <p className="text-sm font-semibold text-blue-700 mt-1">{espLabel}</p>
                  )}
                  {p.direccion && (
                    <p className="text-sm text-slate-500 mt-2 line-clamp-2 leading-relaxed">{p.direccion}</p>
                  )}
                  <span className="mt-4 text-sm font-bold text-clinica-dark group-hover:underline">
                    Ver ficha →
                  </span>
                </Link>
              );
            })}
          </div>
        )}
      </main>
    </div>
  );
};

export default CatalogoProfesionalesPage;
