import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import AdminPasswordConfirmModal from '../../components/AdminPasswordConfirmModal';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';
import useAdminSesion from '../../hooks/useAdminSesion';
import { adminApiErrorMessage } from '../../utils/adminApiError';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const AdminAdministradorEditPage = () => {
  const { idAdministrador } = useParams();
  const navigate = useNavigate();
  const { esPropio, loading: sesionLoading } = useAdminSesion();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [form, setForm] = useState(null);
  const [initialForm, setInitialForm] = useState(null);
  const [showPwd, setShowPwd] = useState(false);
  const [pwd, setPwd] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    if (sesionLoading) return;
    if (!esPropio(idAdministrador)) {
      navigate(ADMIN_PATHS.administradorDetalle(idAdministrador), { replace: true });
    }
  }, [sesionLoading, esPropio, idAdministrador, navigate]);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const { data } = await clienteAxios.get(`/usuarios/api/administradores/${idAdministrador}`);
        if (cancelled) return;
        const picked = {
          nombre: data.nombre ?? '',
          apellido: data.apellido ?? '',
          dni: data.dni != null ? String(data.dni) : '',
          email: data.email ?? '',
          telefono: data.telefono ?? '',
          fechaNacimiento: data.fechaNacimiento ? String(data.fechaNacimiento).slice(0, 10) : '',
          idProvincia: data.idProvincia != null ? String(data.idProvincia) : '',
          idLocalidad: data.idLocalidad != null ? String(data.idLocalidad) : '',
          direccion: data.direccion ?? '',
        };
        setForm(picked);
        setInitialForm({ ...picked });
      } catch (err) {
        if (!cancelled) setError(adminApiErrorMessage(err, 'No se pudo cargar el administrador.'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [idAdministrador]);

  const isDirty = useMemo(() => {
    if (!form || !initialForm) return false;
    return Object.keys(form).some((k) => (form[k] ?? '') !== (initialForm[k] ?? ''));
  }, [form, initialForm]);

  const handleField = (name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const openSave = () => {
    if (!isDirty) return;
    if (!form.idLocalidad || !form.direccion?.trim()) {
      setError('La localidad y la dirección son obligatorias.');
      return;
    }
    setPwd('');
    setShowPwd(true);
  };

  const confirmSave = async () => {
    if (!pwd.trim()) {
      setError('Ingresá tu contraseña para confirmar los cambios.');
      return;
    }
    setSaving(true);
    setError('');
    try {
      await clienteAxios.post('/usuarios/api/seguridad/verificar-password-actual', { password: pwd });
      const payload = {
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        dni: parseInt(form.dni, 10),
        email: form.email.trim(),
        telefono: form.telefono.trim(),
        fechaNacimiento: form.fechaNacimiento || null,
        idLocalidad: parseInt(form.idLocalidad, 10),
        direccion: form.direccion.trim(),
      };
      await clienteAxios.put(`/usuarios/api/administradores/${idAdministrador}`, payload);
      setShowPwd(false);
      navigate(ADMIN_PATHS.administradorDetalle(idAdministrador), {
        state: { mensaje: 'Tus datos fueron actualizados correctamente.' },
      });
    } catch (err) {
      setShowPwd(false);
      setPwd('');
      setError(adminApiErrorMessage(err, 'No se pudo guardar. Revisá la contraseña o los datos.'));
    } finally {
      setSaving(false);
    }
  };

  if (loading || sesionLoading) {
    return <div className="bg-white rounded-xl border border-slate-200 p-6">Cargando…</div>;
  }

  if (!form || !esPropio(idAdministrador)) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <p className="text-red-600 mb-4">{error || 'No podés editar este administrador.'}</p>
        <button
          type="button"
          onClick={() => navigate(ADMIN_PATHS.administradoresLista)}
          className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
        >
          Volver al listado
        </button>
      </div>
    );
  }

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6 max-w-3xl">
      <button
        type="button"
        onClick={() => navigate(ADMIN_PATHS.administradorDetalle(idAdministrador))}
        className="mb-4 px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 text-sm font-semibold hover:bg-slate-50"
      >
        ← Volver al detalle
      </button>

      <h2 className="text-2xl font-black text-slate-900 mb-6">Modificar mis datos</h2>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <form
        className="space-y-5"
        onSubmit={(e) => {
          e.preventDefault();
          openSave();
        }}
      >
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <label className="block">
            <span className="text-sm font-medium text-slate-700">Nombre</span>
            <input
              type="text"
              required
              value={form.nombre}
              onChange={(e) => handleField('nombre', e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>
          <label className="block">
            <span className="text-sm font-medium text-slate-700">Apellido</span>
            <input
              type="text"
              required
              value={form.apellido}
              onChange={(e) => handleField('apellido', e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>
          <label className="block">
            <span className="text-sm font-medium text-slate-700">DNI</span>
            <input
              type="number"
              required
              value={form.dni}
              onChange={(e) => handleField('dni', e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>
          <label className="block">
            <span className="text-sm font-medium text-slate-700">Fecha de nacimiento</span>
            <input
              type="date"
              value={form.fechaNacimiento}
              onChange={(e) => handleField('fechaNacimiento', e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>
          <label className="block sm:col-span-2">
            <span className="text-sm font-medium text-slate-700">Email</span>
            <input
              type="email"
              required
              value={form.email}
              onChange={(e) => handleField('email', e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>
          <label className="block sm:col-span-2">
            <span className="text-sm font-medium text-slate-700">Teléfono</span>
            <input
              type="text"
              value={form.telefono}
              onChange={(e) => handleField('telefono', e.target.value)}
              className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
            />
          </label>
        </div>

        <ProvinciaLocalidadFields
          idProvincia={form.idProvincia}
          idLocalidad={form.idLocalidad}
          onProvinciaChange={(v) => handleField('idProvincia', v)}
          onLocalidadChange={(v) => handleField('idLocalidad', v)}
        />

        <label className="block">
          <span className="text-sm font-medium text-slate-700">Dirección</span>
          <input
            type="text"
            required
            value={form.direccion}
            onChange={(e) => handleField('direccion', e.target.value)}
            className="mt-1 w-full rounded-lg border border-slate-300 px-3 py-2"
          />
        </label>

        <div className="flex flex-wrap gap-3 pt-4">
          <button
            type="submit"
            disabled={!isDirty}
            className="px-5 py-2.5 rounded-lg bg-emerald-700 text-white font-semibold hover:bg-emerald-600 disabled:opacity-50"
          >
            Guardar cambios
          </button>
          <button
            type="button"
            onClick={() => navigate(ADMIN_PATHS.administradorDetalle(idAdministrador))}
            className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
          >
            Cancelar
          </button>
        </div>
      </form>

      <AdminPasswordConfirmModal
        open={showPwd}
        title="Confirmar cambios"
        description="Ingresá tu contraseña de administrador para guardar tus datos."
        password={pwd}
        onPasswordChange={setPwd}
        onCancel={() => {
          setShowPwd(false);
          setPwd('');
        }}
        onConfirm={confirmSave}
        submitting={saving}
        confirmLabel="Confirmar y guardar"
        submittingLabel="Guardando…"
      />
    </section>
  );
};

export default AdminAdministradorEditPage;
