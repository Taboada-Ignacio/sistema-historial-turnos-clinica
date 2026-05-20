import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { ADMIN_PATHS } from '../../utils/portalPaths';
import { filaEsReservada, getCatalogConfig } from './adminCatalogConfig';
import { buildPutPayload } from './catalogPayloadHelpers';
import CatalogProvinciaPicker from '../../components/CatalogProvinciaPicker';

function pickForm(row, editableKeys) {
  const o = {};
  editableKeys.forEach((k) => {
    o[k] = row[k] != null ? String(row[k]) : '';
  });
  return o;
}

const AdminCatalogoEditPage = () => {
  const { tipo, id } = useParams();
  const navigate = useNavigate();
  const config = getCatalogConfig(tipo);

  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [row, setRow] = useState(null);
  const [form, setForm] = useState({});
  const [initialForm, setInitialForm] = useState({});
  const [saving, setSaving] = useState(false);
  const [showPwd, setShowPwd] = useState(false);
  const [pwd, setPwd] = useState('');
  const numericId = useMemo(() => {
    const n = Number(id);
    return Number.isFinite(n) ? n : NaN;
  }, [id]);

  const isDirty = useMemo(() => {
    if (!config?.editableKeys) return false;
    return config.editableKeys.some((k) => (form[k] ?? '') !== (initialForm[k] ?? ''));
  }, [config, form, initialForm]);

  const reservada = row && config && !config.readOnly ? filaEsReservada(row, config) : false;

  useEffect(() => {
    if (!config || config.readOnly || Number.isNaN(numericId)) {
      navigate(ADMIN_PATHS.entidades, { replace: true });
      return;
    }
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const { data } = await clienteAxios.get(`/usuarios/api/${config.apiSegment}/${numericId}`);
        if (cancelled) return;
        setRow(data);
        const picked = pickForm(data, config.editableKeys);
        setForm(picked);
        setInitialForm({ ...picked });
      } catch {
        if (!cancelled) setError('No se pudo cargar el registro.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [config, navigate, numericId]);

  const handleField = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const openSave = () => {
    if (!isDirty || reservada) return;
    setPwd('');
    setShowPwd(true);
  };

  const confirmSave = async () => {
    if (!pwd.trim()) {
      setError('Ingresá tu contraseña para confirmar.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      await clienteAxios.post('/usuarios/api/seguridad/verificar-password-actual', { password: pwd });
      const body = buildPutPayload(tipo, form);
      await clienteAxios.put(`/usuarios/api/${config.apiSegment}/${numericId}`, body);
      setShowPwd(false);
      navigate(ADMIN_PATHS.entidades);
    } catch (err) {
      const msg = err.response?.data?.message || err.response?.data?.error;
      setError(msg || 'No se pudo guardar. Revisá la contraseña o los datos.');
    } finally {
      setSaving(false);
    }
  };

  if (!config || config.readOnly) return null;

  const listaUrl = `${ADMIN_PATHS.dashboard}/catalogo/${tipo}`;

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6 max-w-lg">
      <div className="flex justify-between items-start gap-4 mb-6">
        <h2 className="text-xl font-black text-slate-900">Editar {config.label}</h2>
        <Link to={listaUrl} className="text-sm font-semibold text-slate-600 hover:text-slate-900 underline shrink-0">
          ← Volver al listado
        </Link>
      </div>

      {reservada && (
        <div className="mb-4 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-sm text-amber-900">
          Este registro está reservado para el sistema y no se puede modificar.
        </div>
      )}

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      )}

      {loading ? (
        <p className="text-slate-500">Cargando…</p>
      ) : row ? (
        <div className="space-y-4">
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">ID</label>
            <input
              type="text"
              disabled
              value={String(row[config.idKey] ?? '')}
              className="w-full rounded-lg border border-slate-200 bg-slate-100 px-3 py-2 text-slate-600"
            />
          </div>
          {config.editableKeys.map((key) => (
            <div key={key}>
              <label className="block text-xs font-bold text-slate-500 uppercase mb-1">
                {config.fieldLabels[key] || key}
              </label>
              {key === 'idProvincia' && tipo === 'localidades' ? (
                <CatalogProvinciaPicker
                  value={form.idProvincia ?? ''}
                  onChange={(val) => handleField('idProvincia', val)}
                  excludeSentinel={false}
                  required
                  disabled={reservada}
                />
              ) : (
                <input
                  type="text"
                  disabled={reservada}
                  value={form[key] ?? ''}
                  onChange={(e) => handleField(key, e.target.value)}
                  className="w-full rounded-lg border border-slate-200 px-3 py-2 outline-none focus:ring-2 focus:ring-emerald-600/30 disabled:bg-slate-100"
                />
              )}
            </div>
          ))}

          <div className="flex flex-wrap gap-3 pt-4 border-t border-slate-100">
            <Link
              to={listaUrl}
              className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
            >
              Volver atrás
            </Link>
            <button
              type="button"
              disabled={!isDirty || reservada}
              onClick={openSave}
              className={`rounded-xl px-4 py-2.5 text-sm font-bold ${
                isDirty && !reservada
                  ? 'bg-emerald-600 text-white hover:bg-emerald-700 shadow-sm'
                  : 'bg-slate-200 text-slate-500 cursor-not-allowed'
              }`}
            >
              {isDirty && !reservada ? 'Confirmar modificación' : 'Sin cambios'}
            </button>
          </div>
        </div>
      ) : (
        <p className="text-slate-500">Registro no encontrado.</p>
      )}

      {showPwd && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl border border-slate-200">
            <h3 className="font-bold text-slate-900 mb-2">Confirmar con contraseña</h3>
            <p className="text-sm text-slate-600 mb-4">Ingresá tu contraseña actual para aplicar los cambios.</p>
            <input
              type="password"
              autoFocus
              value={pwd}
              onChange={(e) => setPwd(e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2 mb-4"
              placeholder="Contraseña"
            />
            <div className="flex justify-end gap-2">
              <button
                type="button"
                disabled={saving}
                onClick={() => setShowPwd(false)}
                className="rounded-lg px-3 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100"
              >
                Cancelar
              </button>
              <button
                type="button"
                disabled={saving}
                onClick={confirmSave}
                className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-bold text-white hover:bg-emerald-700 disabled:opacity-50"
              >
                {saving ? 'Guardando…' : 'Confirmar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

export default AdminCatalogoEditPage;
