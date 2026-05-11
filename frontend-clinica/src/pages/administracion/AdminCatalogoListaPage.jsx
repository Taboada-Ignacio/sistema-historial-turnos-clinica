import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { ADMIN_PATHS } from '../../utils/portalPaths';
import { ADMIN_CATALOG_CONFIG, filaEsReservada, getCatalogConfig } from './adminCatalogConfig';

const AdminCatalogoListaPage = () => {
  const { tipo } = useParams();
  const navigate = useNavigate();
  const config = getCatalogConfig(tipo);
  const [items, setItems] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [deletingId, setDeletingId] = useState(null);

  useEffect(() => {
    if (!config) {
      navigate(ADMIN_PATHS.entidades, { replace: true });
      return;
    }
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const { data } = await clienteAxios.get(`/usuarios/api/${config.apiSegment}`);
        if (!cancelled) setItems(Array.isArray(data) ? data : []);
      } catch {
        if (!cancelled) setError('No se pudo cargar el listado.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [config, navigate, tipo]);

  const baseCatalog = `${ADMIN_PATHS.dashboard}/catalogo/${tipo}`;

  const textoItem = (row) => {
    const main = row?.[config.listTitle] ?? '—';
    if (config.subtitleKey && row?.[config.subtitleKey]) {
      return `${main} (${row[config.subtitleKey]})`;
    }
    return String(main);
  };

  const handleDelete = async (row) => {
    if (config.readOnly) return;
    if (filaEsReservada(row, config)) {
      window.alert('No se puede eliminar el registro reservado del sistema.');
      return;
    }
    const id = row[config.idKey];
    if (!window.confirm('¿Eliminar este registro? Los datos que lo referenciaban pasarán al valor reservado SIN ESPECIFICAR.')) return;
    setDeletingId(id);
    setError('');
    try {
      await clienteAxios.delete(`/usuarios/api/${config.apiSegment}/${id}`);
      setItems((prev) => prev.filter((r) => r[config.idKey] !== id));
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo eliminar.');
    } finally {
      setDeletingId(null);
    }
  };

  if (!config) return null;

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-black text-slate-900">{config.label}</h2>
          <p className="text-sm text-slate-600 mt-1">
            {config.readOnly ? 'Solo consulta.' : 'Modificá o eliminá ítems del catálogo.'}
          </p>
        </div>
        <Link
          to={ADMIN_PATHS.entidades}
          className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-100 px-4 py-2.5 text-sm font-bold text-slate-800 shadow-sm transition-colors hover:bg-slate-200 hover:border-slate-300 active:bg-slate-300"
        >
          <span aria-hidden>←</span>
          Volver a entidades
        </Link>
      </div>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      )}

      {loading ? (
        <p className="text-slate-500">Cargando…</p>
      ) : items.length === 0 ? (
        <p className="text-slate-500 border border-dashed border-slate-200 rounded-xl p-8 text-center">No hay registros.</p>
      ) : (
        <ul className="divide-y divide-slate-100 border border-slate-100 rounded-xl overflow-hidden">
          {items.map((row) => {
            const id = row[config.idKey];
            const reservada = filaEsReservada(row, config);
            return (
              <li
                key={id}
                className="flex flex-wrap items-center justify-between gap-3 px-4 py-3 bg-slate-50/50 hover:bg-slate-50"
              >
                <div className="min-w-0 flex-1">
                  <p className="font-medium text-slate-900 truncate">{textoItem(row)}</p>
                  <p className="text-xs text-slate-500">ID: {id}</p>
                  {reservada && (
                    <span className="mt-1 inline-block text-[10px] font-bold uppercase tracking-wide text-amber-700 bg-amber-50 border border-amber-200 rounded px-2 py-0.5">
                      Reservado
                    </span>
                  )}
                </div>
                <div className="flex items-center gap-2 shrink-0">
                  {!config.readOnly && (
                    <>
                      <Link
                        to={`${baseCatalog}/${id}`}
                        className="rounded-lg border border-slate-200 bg-white px-3 py-1.5 text-sm font-semibold text-slate-800 hover:bg-slate-100"
                      >
                        Modificar
                      </Link>
                      <button
                        type="button"
                        disabled={reservada || deletingId === id}
                        onClick={() => handleDelete(row)}
                        className="rounded-lg border border-red-200 bg-white p-2 text-red-600 hover:bg-red-50 disabled:opacity-40 disabled:cursor-not-allowed"
                        title="Eliminar"
                        aria-label="Eliminar"
                      >
                        <svg xmlns="http://www.w3.org/2000/svg" className="h-5 w-5" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2">
                          <path d="M3 6h18M8 6V4h8v2M19 6v14a2 2 0 0 1-2 2H7a2 2 0 0 1-2-2V6M10 11v6M14 11v6" strokeLinecap="round" strokeLinejoin="round" />
                        </svg>
                      </button>
                    </>
                  )}
                </div>
              </li>
            );
          })}
        </ul>
      )}
    </section>
  );
};

export default AdminCatalogoListaPage;
