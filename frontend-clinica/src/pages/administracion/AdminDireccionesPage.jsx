import React, { useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { ADMIN_PATHS } from '../../utils/portalPaths';
import { SIN_ESPECIFICAR } from './adminCatalogConfig';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';

const AdminDireccionesPage = () => {
  const [idProvincia, setIdProvincia] = useState('');
  const [idLocalidad, setIdLocalidad] = useState('');
  const [direcciones, setDirecciones] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const [nuevoNombre, setNuevoNombre] = useState('');
  const [editId, setEditId] = useState(null);
  const [editNombre, setEditNombre] = useState('');
  const [showPwd, setShowPwd] = useState(false);
  const [pwd, setPwd] = useState('');
  const [pendingAction, setPendingAction] = useState(null);

  useEffect(() => {
    setDirecciones([]);
    if (!idLocalidad) return;
    setLoading(true);
    clienteAxios
      .get(`/usuarios/api/direcciones/localidad/${idLocalidad}`)
      .then(({ data }) => setDirecciones(Array.isArray(data) ? data : []))
      .catch(() => setError('No se pudieron cargar las direcciones.'))
      .finally(() => setLoading(false));
  }, [idLocalidad]);

  const esReservada = (d) => (d.nombre || '').trim().toUpperCase() === SIN_ESPECIFICAR;

  const pedirPassword = (action) => {
    setPendingAction(action);
    setPwd('');
    setShowPwd(true);
  };

  const ejecutarConPassword = async () => {
    if (!pwd.trim()) {
      setError('Ingresá tu contraseña para confirmar.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await clienteAxios.post('/usuarios/api/seguridad/verificar-password-actual', { password: pwd });
      if (pendingAction?.type === 'create') {
        await clienteAxios.post('/usuarios/api/direcciones/registro', {
          nombre: pendingAction.nombre,
          idLocalidad: parseInt(idLocalidad, 10),
        });
        setNuevoNombre('');
      } else if (pendingAction?.type === 'update') {
        await clienteAxios.put(`/usuarios/api/direcciones/${pendingAction.id}`, {
          nombre: pendingAction.nombre,
        });
        setEditId(null);
        setEditNombre('');
      } else if (pendingAction?.type === 'delete') {
        await clienteAxios.delete(`/usuarios/api/direcciones/${pendingAction.id}`);
      }
      setShowPwd(false);
      setPendingAction(null);
      const { data } = await clienteAxios.get(`/usuarios/api/direcciones/localidad/${idLocalidad}`);
      setDirecciones(Array.isArray(data) ? data : []);
    } catch (err) {
      const msg = err?.response?.data?.mensaje || err?.response?.data?.message;
      setError(msg || 'No se pudo completar la operación.');
    } finally {
      setLoading(false);
    }
  };

  const handleCreate = () => {
    const nombre = nuevoNombre.trim().toUpperCase();
    if (!nombre || !idLocalidad) return;
    if (nombre === SIN_ESPECIFICAR) {
      setError(`No podés crear una dirección con el nombre reservado ${SIN_ESPECIFICAR}.`);
      return;
    }
    pedirPassword({ type: 'create', nombre });
  };

  const handleUpdate = (id) => {
    const nombre = editNombre.trim().toUpperCase();
    if (!nombre) return;
    if (nombre === SIN_ESPECIFICAR) {
      setError(`No podés usar el nombre reservado ${SIN_ESPECIFICAR}.`);
      return;
    }
    pedirPassword({ type: 'update', id, nombre });
  };

  const handleDelete = (d) => {
    if (esReservada(d)) {
      window.alert('No se puede eliminar la dirección reservada del sistema.');
      return;
    }
    if (!window.confirm('¿Eliminar esta dirección? Los usuarios que la usaban pasarán a SIN ESPECIFICAR en esta localidad.')) return;
    pedirPassword({ type: 'delete', id: d.idDireccion });
  };

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-black text-slate-900">Direcciones</h2>
          <p className="text-sm text-slate-600 mt-1">
            Elegí provincia y localidad. Alta, edición (solo nombre) y baja requieren contraseña de administrador.
          </p>
        </div>
        <Link
          to={ADMIN_PATHS.entidades}
          className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-100 px-4 py-2.5 text-sm font-bold text-slate-800 shadow-sm hover:bg-slate-200"
        >
          ← Volver a entidades
        </Link>
      </div>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      )}

      <div className="mb-6">
        <ProvinciaLocalidadFields
          excludeSentinel={false}
          showLabels
          labelClassName="block text-xs font-bold text-slate-500 uppercase mb-1"
          provinciaId={idProvincia}
          localidadId={idLocalidad}
          onProvinciaChange={(id) => {
            setIdProvincia(id);
            setIdLocalidad('');
            setDirecciones([]);
          }}
          onLocalidadChange={(id) => setIdLocalidad(id)}
          inputClassName="w-full rounded-lg border border-slate-200 px-3 py-2"
        />
      </div>

      {idLocalidad && (
        <>
          <div className="flex flex-wrap gap-2 mb-4 p-4 bg-slate-50 rounded-xl border border-slate-100">
            <input
              type="text"
              placeholder="Nombre de la nueva dirección"
              value={nuevoNombre}
              onChange={(e) => setNuevoNombre(e.target.value)}
              className="flex-1 min-w-[200px] rounded-lg border border-slate-200 px-3 py-2"
            />
            <button
              type="button"
              disabled={!nuevoNombre.trim() || loading}
              onClick={handleCreate}
              className="rounded-xl bg-emerald-600 px-4 py-2 text-sm font-bold text-white hover:bg-emerald-700 disabled:opacity-50"
            >
              Agregar dirección
            </button>
          </div>

          {loading && direcciones.length === 0 ? (
            <p className="text-slate-500">Cargando…</p>
          ) : direcciones.length === 0 ? (
            <p className="text-slate-500 border border-dashed border-slate-200 rounded-xl p-8 text-center">
              No hay direcciones en esta localidad.
            </p>
          ) : (
            <ul className="divide-y divide-slate-100 border border-slate-100 rounded-xl overflow-hidden">
              {direcciones.map((d) => {
                const reservada = esReservada(d);
                const editing = editId === d.idDireccion;
                return (
                  <li key={d.idDireccion} className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 bg-slate-50/50">
                    <div className="min-w-0 flex-1">
                      {editing ? (
                        <input
                          type="text"
                          value={editNombre}
                          onChange={(e) => setEditNombre(e.target.value)}
                          className="w-full max-w-md rounded-lg border border-slate-200 px-3 py-1.5"
                        />
                      ) : (
                        <p className="font-medium text-slate-900">{d.nombre}</p>
                      )}
                      <p className="text-xs text-slate-500">ID: {d.idDireccion}</p>
                      {reservada && (
                        <span className="mt-1 inline-block text-[10px] font-bold uppercase text-amber-700 bg-amber-50 border border-amber-200 rounded px-2 py-0.5">
                          Reservado
                        </span>
                      )}
                    </div>
                    <div className="flex gap-2 shrink-0">
                      {editing ? (
                        <>
                          <button
                            type="button"
                            onClick={() => {
                              setEditId(null);
                              setEditNombre('');
                            }}
                            className="rounded-lg border border-slate-200 px-3 py-1.5 text-sm font-semibold"
                          >
                            Cancelar
                          </button>
                          <button
                            type="button"
                            disabled={loading}
                            onClick={() => handleUpdate(d.idDireccion)}
                            className="rounded-lg bg-emerald-600 px-3 py-1.5 text-sm font-bold text-white"
                          >
                            Guardar
                          </button>
                        </>
                      ) : (
                        <>
                          <button
                            type="button"
                            disabled={reservada}
                            onClick={() => {
                              setEditId(d.idDireccion);
                              setEditNombre(d.nombre || '');
                            }}
                            className="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm font-semibold disabled:opacity-40"
                          >
                            Modificar
                          </button>
                          <button
                            type="button"
                            disabled={reservada || loading}
                            onClick={() => handleDelete(d)}
                            className="rounded-lg border border-red-200 bg-white p-2 text-red-600 hover:bg-red-50 disabled:opacity-40"
                            title="Eliminar"
                          >
                            ✕
                          </button>
                        </>
                      )}
                    </div>
                  </li>
                );
              })}
            </ul>
          )}
        </>
      )}

      {showPwd && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl border border-slate-200">
            <h3 className="font-bold text-slate-900 mb-2">Confirmar con contraseña</h3>
            <input
              type="password"
              autoFocus
              value={pwd}
              onChange={(e) => setPwd(e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 mb-4"
              placeholder="Contraseña"
            />
            <div className="flex justify-end gap-2">
              <button type="button" onClick={() => setShowPwd(false)} className="rounded-lg px-3 py-2 text-sm font-semibold">
                Cancelar
              </button>
              <button
                type="button"
                disabled={loading}
                onClick={ejecutarConPassword}
                className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-bold text-white disabled:opacity-50"
              >
                {loading ? 'Procesando…' : 'Confirmar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

export default AdminDireccionesPage;
