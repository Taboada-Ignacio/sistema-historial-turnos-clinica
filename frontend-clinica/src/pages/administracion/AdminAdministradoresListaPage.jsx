import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import useAdminSesion from '../../hooks/useAdminSesion';
import { adminApiErrorMessage } from '../../utils/adminApiError';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const AdminAdministradoresListaPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const flashMensaje = location.state?.mensaje;
  const { idUsuario: idSesion, loading: sesionLoading, esPropio } = useAdminSesion();
  const [lista, setLista] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const { data } = await clienteAxios.get('/usuarios/api/administradores');
        if (!cancelled) setLista(Array.isArray(data) ? data : []);
      } catch (err) {
        if (!cancelled) {
          setLista([]);
          setError(adminApiErrorMessage(err, 'No se pudo cargar el listado de administradores.'));
        }
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const verAdministrador = (id) => {
    navigate(ADMIN_PATHS.administradorDetalle(id));
  };

  if (loading || sesionLoading) {
    return <div className="bg-white rounded-xl border border-slate-200 p-6">Cargando…</div>;
  }

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <button
        type="button"
        onClick={() => navigate(ADMIN_PATHS.entidades)}
        className="mb-4 px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 text-sm font-semibold hover:bg-slate-50"
      >
        ← Volver a entidades
      </button>

      <h2 className="text-2xl font-black text-slate-900 mb-1">Consultar administradores</h2>
      <p className="text-slate-600 text-sm mb-6">
        Se listan todas las cuentas de administrador. Solo podés modificar o eliminar la tuya; el resto se
        consulta en solo lectura.
      </p>

      {flashMensaje && (
        <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900">
          {flashMensaje}
        </div>
      )}

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      {lista.length === 0 ? (
        <p className="text-slate-600 text-sm">No hay administradores registrados.</p>
      ) : (
        <div className="overflow-x-auto border border-slate-200 rounded-xl">
          <table className="w-full text-sm text-left">
            <thead className="bg-slate-50 text-slate-600 uppercase text-xs font-bold">
              <tr>
                <th className="px-4 py-3">Apellido y nombre</th>
                <th className="px-4 py-3">Email</th>
                <th className="px-4 py-3">DNI</th>
                <th className="px-4 py-3">Estado</th>
                <th className="px-4 py-3 text-right">Acción</th>
              </tr>
            </thead>
            <tbody>
              {lista.map((a) => {
                const propio = esPropio(a.idUsuario);
                const esSesion = idSesion != null && Number(a.idUsuario) === Number(idSesion);
                return (
                  <tr
                    key={a.idUsuario}
                    className={`border-t border-slate-100 ${
                      propio ? 'bg-emerald-50/40' : 'bg-slate-50/80 text-slate-500'
                    }`}
                  >
                    <td className="px-4 py-3 font-medium">
                      {a.apellido}, {a.nombre}
                      {esSesion && (
                        <span className="ml-2 text-xs font-bold text-emerald-800 bg-emerald-100 px-2 py-0.5 rounded">
                          Tu cuenta
                        </span>
                      )}
                    </td>
                    <td className="px-4 py-3 break-all">{a.email}</td>
                    <td className="px-4 py-3">{a.dni ?? '—'}</td>
                    <td className="px-4 py-3">{a.estadoActual ?? '—'}</td>
                    <td className="px-4 py-3 text-right">
                      <button
                        type="button"
                        onClick={() => verAdministrador(a.idUsuario)}
                        className={`px-4 py-2 rounded-lg font-semibold text-sm ${
                          propio
                            ? 'bg-emerald-700 text-white hover:bg-emerald-600'
                            : 'border border-slate-300 text-slate-600 hover:bg-slate-100'
                        }`}
                      >
                        Ver administrador
                      </button>
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      )}
    </section>
  );
};

export default AdminAdministradoresListaPage;
