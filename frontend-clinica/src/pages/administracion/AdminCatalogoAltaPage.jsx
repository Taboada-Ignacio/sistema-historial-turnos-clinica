import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { ADMIN_PATHS } from '../../utils/portalPaths';
import { getCatalogConfig } from './adminCatalogConfig';
import { buildPostPayload } from './catalogPayloadHelpers';
import CatalogProvinciaPicker from '../../components/CatalogProvinciaPicker';

const AdminCatalogoAltaPage = () => {
  const { tipo } = useParams();
  const navigate = useNavigate();
  const config = getCatalogConfig(tipo);

  const [form, setForm] = useState({});
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPwd, setShowPwd] = useState(false);
  const [pwd, setPwd] = useState('');

  const listaUrl = `${ADMIN_PATHS.dashboard}/catalogo/${tipo}`;

  useEffect(() => {
    if (!config || config.readOnly) {
      navigate(ADMIN_PATHS.entidades, { replace: true });
      return;
    }
    const initial = {};
    config.editableKeys.forEach((k) => {
      initial[k] = '';
    });
    setForm(initial);
  }, [config, navigate, tipo]);

  const canSubmit = useMemo(() => {
    if (!config) return false;
    return config.editableKeys.every((k) => String(form[k] ?? '').trim() !== '');
  }, [config, form]);

  const handleField = (key, value) => {
    setForm((prev) => ({ ...prev, [key]: value }));
  };

  const openSave = () => {
    if (!canSubmit) return;
    setPwd('');
    setShowPwd(true);
  };

  const confirmSave = async () => {
    if (!pwd.trim()) {
      setError('Ingresá tu contraseña para confirmar.');
      return;
    }
    setLoading(true);
    setError('');
    try {
      await clienteAxios.post('/usuarios/api/seguridad/verificar-password-actual', { password: pwd });
      const body = buildPostPayload(tipo, form);
      await clienteAxios.post(`/usuarios/api/${config.apiSegment}/registro`, body);
      setShowPwd(false);
      navigate(listaUrl);
    } catch (err) {
      const msg = err?.response?.data?.mensaje || err?.response?.data?.message;
      setError(msg || 'No se pudo crear el registro. Revisá la contraseña o los datos.');
    } finally {
      setLoading(false);
    }
  };

  if (!config || config.readOnly) return null;

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6 max-w-lg">
      <div className="flex justify-between items-start gap-4 mb-6">
        <h2 className="text-xl font-black text-slate-900">Nuevo — {config.label}</h2>
        <Link to={listaUrl} className="text-sm font-semibold text-slate-600 hover:text-slate-900 underline shrink-0">
          ← Volver al listado
        </Link>
      </div>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
      )}

      <div className="space-y-4">
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
              />
            ) : (
              <input
                type="text"
                value={form[key] ?? ''}
                onChange={(e) => handleField(key, e.target.value)}
                className="w-full rounded-lg border border-slate-200 px-3 py-2 outline-none focus:ring-2 focus:ring-emerald-600/30"
              />
            )}
          </div>
        ))}

        <div className="flex flex-wrap gap-3 pt-4 border-t border-slate-100">
          <Link
            to={listaUrl}
            className="rounded-xl border border-slate-200 px-4 py-2.5 text-sm font-semibold text-slate-700 hover:bg-slate-50"
          >
            Cancelar
          </Link>
          <button
            type="button"
            disabled={!canSubmit || loading}
            onClick={openSave}
            className="rounded-xl bg-emerald-600 px-4 py-2.5 text-sm font-bold text-white hover:bg-emerald-700 disabled:opacity-50"
          >
            Crear registro
          </button>
        </div>
      </div>

      {showPwd && (
        <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
          <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl border border-slate-200">
            <h3 className="font-bold text-slate-900 mb-2">Confirmar con contraseña</h3>
            <p className="text-sm text-slate-600 mb-4">Ingresá tu contraseña actual para crear el registro.</p>
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
                disabled={loading}
                onClick={() => setShowPwd(false)}
                className="rounded-lg px-3 py-2 text-sm font-semibold text-slate-600 hover:bg-slate-100"
              >
                Cancelar
              </button>
              <button
                type="button"
                disabled={loading}
                onClick={confirmSave}
                className="rounded-lg bg-emerald-600 px-4 py-2 text-sm font-bold text-white hover:bg-emerald-700 disabled:opacity-50"
              >
                {loading ? 'Guardando…' : 'Confirmar'}
              </button>
            </div>
          </div>
        </div>
      )}
    </section>
  );
};

export default AdminCatalogoAltaPage;