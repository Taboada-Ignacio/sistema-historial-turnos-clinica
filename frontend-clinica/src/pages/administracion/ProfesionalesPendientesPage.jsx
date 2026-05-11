import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { profesionalFotoAbsoluteUrl } from '../../utils/profesionalFotoUrl';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const apiErrorMessage = (err) =>
  err.response?.data?.mensaje ||
  err.response?.data?.message ||
  err.response?.data?.error ||
  'No se pudo obtener la lista de profesionales pendientes.';

function sortProfesionalesPendientes(list) {
  return [...list].sort((a, b) => {
    const byApellido = (a.apellido || '').localeCompare(b.apellido || '', 'es', { sensitivity: 'base' });
    if (byApellido !== 0) return byApellido;
    return (a.nombre || '').localeCompare(b.nombre || '', 'es', { sensitivity: 'base' });
  });
}

const ProfesionalesPendientesPage = () => {
  const navigate = useNavigate();
  const [pendientes, setPendientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchPendientes = async () => {
      try {
        const response = await clienteAxios.get('/usuarios/api/profesionales', {
          params: { membresia: 'SIN_VERIFICAR' },
        });
        const data = response.data || [];
        setPendientes(sortProfesionalesPendientes(data));
      } catch (err) {
        setError(apiErrorMessage(err));
      } finally {
        setLoading(false);
      }
    };

    fetchPendientes();
  }, []);

  if (loading) {
    return <div className="bg-white rounded-xl border border-slate-200 p-6">Cargando profesionales...</div>;
  }

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <h2 className="text-xl font-black text-slate-900 mb-2">Profesionales pendientes de revisión</h2>
      <p className="text-slate-600 mb-6">
        Listado ordenado por apellido y nombre. Membresía <span className="font-mono text-sm">SIN_VERIFICAR</span>.
      </p>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {!error && pendientes.length === 0 && (
        <p className="text-slate-600">
          No hay profesionales pendientes de revisión en este momento. Cuando un profesional se registre y quede por
          verificar matrícula, aparecerá aquí.
        </p>
      )}

      <ul className="space-y-3">
        {pendientes.map((profesional) => {
          const fotoUrl = profesionalFotoAbsoluteUrl(profesional.fotoPerfil);
          return (
            <li key={profesional.idUsuario}>
              <button
                type="button"
                onClick={() => navigate(`${ADMIN_PATHS.profesionalesPendientes}/${profesional.idUsuario}`)}
                className="w-full text-left border border-slate-200 rounded-xl p-4 flex flex-col sm:flex-row sm:items-center gap-4 hover:border-slate-400 hover:bg-slate-50/80 transition-colors"
              >
                <div className="shrink-0 flex justify-center sm:justify-start">
                  {fotoUrl ? (
                    <img
                      src={fotoUrl}
                      alt=""
                      className="w-20 h-20 sm:w-16 sm:h-16 object-cover rounded-lg border border-slate-200"
                    />
                  ) : (
                    <div className="w-20 h-20 sm:w-16 sm:h-16 rounded-lg border border-slate-200 bg-slate-100 flex items-center justify-center text-xs text-slate-500">
                      Sin foto
                    </div>
                  )}
                </div>
                <div className="flex-1 min-w-0 grid grid-cols-1 sm:grid-cols-3 gap-1 sm:gap-4 sm:items-center">
                  <p className="font-bold text-slate-900 truncate order-1 sm:order-1">{profesional.apellido}</p>
                  <p className="text-slate-800 truncate order-2 sm:order-2">{profesional.nombre}</p>
                  <p className="text-sm text-slate-600 truncate order-3 sm:order-3">{profesional.especialidad || '—'}</p>
                </div>
                <span className="text-sm font-semibold text-slate-500 shrink-0 hidden sm:inline">Ver detalle →</span>
              </button>
            </li>
          );
        })}
      </ul>
    </section>
  );
};

export default ProfesionalesPendientesPage;
