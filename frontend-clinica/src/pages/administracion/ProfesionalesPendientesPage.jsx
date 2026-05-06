import React, { useEffect, useState } from 'react';
import { useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const ProfesionalesPendientesPage = () => {
  const navigate = useNavigate();
  const [pendientes, setPendientes] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    const fetchPendientes = async () => {
      try {
        const response = await clienteAxios.get('/usuarios/api/profesionales');
        const soloSinVerificar = (response.data || []).filter(
          (profesional) => profesional.membresiaActual === 'SIN_VERIFICAR'
        );
        setPendientes(soloSinVerificar);
      } catch (err) {
        setError(err.response?.data?.message || 'No se pudo obtener la lista de profesionales pendientes.');
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
      <p className="text-slate-600 mb-6">Se muestran los usuarios con membresía `SIN_VERIFICAR`.</p>

      {error && <p className="text-red-600 mb-4">{error}</p>}

      {!error && pendientes.length === 0 && (
        <p className="text-slate-600">No hay profesionales pendientes en este momento.</p>
      )}

      <div className="space-y-3">
        {pendientes.map((profesional) => (
          <article
            key={profesional.idUsuario}
            className="border border-slate-200 rounded-xl p-4 flex flex-col md:flex-row md:items-center md:justify-between gap-4"
          >
            <div>
              <p className="font-bold text-slate-900">
                {profesional.nombre} {profesional.apellido}
              </p>
              <p className="text-sm text-slate-600">{profesional.email}</p>
              <p className="text-sm text-slate-600">Matrícula: {profesional.matricula}</p>
            </div>
            <button
              type="button"
              onClick={() => navigate(`${ADMIN_PATHS.profesionalesPendientes}/${profesional.idUsuario}`)}
              className="bg-slate-900 text-white px-4 py-2 rounded-lg text-sm font-semibold hover:bg-slate-800"
            >
              Ver detalle y verificar
            </button>
          </article>
        ))}
      </div>
    </section>
  );
};

export default ProfesionalesPendientesPage;
