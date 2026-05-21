import React, { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import AdminPasswordConfirmModal from '../../components/AdminPasswordConfirmModal';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';
import { adminApiErrorMessage } from '../../utils/adminApiError';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const ESTADOS_CUENTA = ['PENDIENTE', 'ACTIVO', 'BLOQUEADO'];

const AdminPacienteEditPage = () => {
  const { idPaciente } = useParams();
  const navigate = useNavigate();
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [obrasSociales, setObrasSociales] = useState([]);
  const [form, setForm] = useState(null);
  const [initialForm, setInitialForm] = useState(null);
  const [showPwd, setShowPwd] = useState(false);
  const [pwd, setPwd] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const [resPaciente, resObras] = await Promise.all([
          clienteAxios.get(`/usuarios/api/pacientes/${idPaciente}`),
          clienteAxios.get('/usuarios/api/obras-sociales'),
        ]);
        if (cancelled) return;
        const p = resPaciente.data;
        const picked = {
          nombre: p.nombre ?? '',
          apellido: p.apellido ?? '',
          dni: p.dni != null ? String(p.dni) : '',
          email: p.email ?? '',
          telefono: p.telefono ?? '',
          fechaNacimiento: p.fechaNacimiento ? String(p.fechaNacimiento).slice(0, 10) : '',
          numeroAfiliado: p.numeroAfiliado ?? '',
          estadoActual: p.estadoActual ?? '',
          idObraSocial: p.idObraSocial != null ? String(p.idObraSocial) : '',
          idProvincia: p.idProvincia != null ? String(p.idProvincia) : '',
          idLocalidad: p.idLocalidad != null ? String(p.idLocalidad) : '',
          direccion: p.direccion ?? '',
        };
        setForm(picked);
        setInitialForm({ ...picked });
        setObrasSociales(Array.isArray(resObras.data) ? resObras.data : []);
      } catch (err) {
        if (!cancelled) setError(adminApiErrorMessage(err, 'No se pudo cargar el paciente.'));
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [idPaciente]);

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
    if (!form.idObraSocial) {
      setError('Seleccioná una obra social.');
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
        numeroAfiliado: form.numeroAfiliado.trim() || null,
        estadoActual: form.estadoActual,
        idObraSocial: parseInt(form.idObraSocial, 10),
        idLocalidad: parseInt(form.idLocalidad, 10),
        direccion: form.direccion.trim(),
      };
      await clienteAxios.put(`/usuarios/api/pacientes/${idPaciente}`, payload);
      setShowPwd(false);
      navigate(ADMIN_PATHS.pacienteDetalle(idPaciente), {
        state: { mensaje: 'Datos del paciente actualizados correctamente.' },
      });
    } catch (err) {
      setShowPwd(false);
      setPwd('');
      setError(adminApiErrorMessage(err, 'No se pudo guardar. Revisá la contraseña o los datos.'));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="bg-white rounded-xl border border-slate-200 p-6">Cargando…</div>;
  }

  if (!form) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <p className="text-red-600 mb-4">{error || 'Paciente no encontrado.'}</p>
        <button
          type="button"
          onClick={() => navigate(ADMIN_PATHS.pacientesBuscar)}
          className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
        >
          Volver a consultar pacientes
        </button>
      </div>
    );
  }

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6 max-w-2xl">
      <button
        type="button"
        onClick={() => navigate(ADMIN_PATHS.pacienteDetalle(idPaciente))}
        className="mb-4 px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 text-sm font-semibold hover:bg-slate-50"
      >
        ← Volver al detalle
      </button>

      <h2 className="text-xl font-black text-slate-900 mb-6">Modificar paciente</h2>

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <div className="space-y-4">
        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Apellido</label>
            <input
              type="text"
              required
              value={form.apellido}
              onChange={(e) => handleField('apellido', e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Nombre</label>
            <input
              type="text"
              required
              value={form.nombre}
              onChange={(e) => handleField('nombre', e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2"
            />
          </div>
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">DNI</label>
            <input
              type="number"
              required
              value={form.dni}
              onChange={(e) => handleField('dni', e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2"
            />
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Teléfono</label>
            <input
              type="tel"
              value={form.telefono}
              onChange={(e) => handleField('telefono', e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Email</label>
          <input
            type="email"
            required
            value={form.email}
            onChange={(e) => handleField('email', e.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2"
          />
        </div>

        <div>
          <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Fecha de nacimiento</label>
          <input
            type="date"
            value={form.fechaNacimiento}
            onChange={(e) => handleField('fechaNacimiento', e.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2"
          />
        </div>

        <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Obra social</label>
            <select
              required
              value={form.idObraSocial}
              onChange={(e) => handleField('idObraSocial', e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2"
            >
              <option value="">Seleccionar</option>
              {obrasSociales.map((o) => (
                <option key={o.idObraSocial} value={o.idObraSocial}>
                  {o.descripcion}
                </option>
              ))}
            </select>
          </div>
          <div>
            <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Nº afiliado</label>
            <input
              type="text"
              value={form.numeroAfiliado}
              onChange={(e) => handleField('numeroAfiliado', e.target.value)}
              className="w-full rounded-lg border border-slate-200 px-3 py-2"
            />
          </div>
        </div>

        <div>
          <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Estado de cuenta</label>
          <select
            required
            value={form.estadoActual}
            onChange={(e) => handleField('estadoActual', e.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2"
          >
            {ESTADOS_CUENTA.map((e) => (
              <option key={e} value={e}>
                {e}
              </option>
            ))}
          </select>
        </div>

        <ProvinciaLocalidadFields
          showLabels
          provinciaId={form.idProvincia}
          localidadId={form.idLocalidad}
          onProvinciaChange={(id) => {
            setForm((prev) => ({ ...prev, idProvincia: id, idLocalidad: '', direccion: '' }));
          }}
          onLocalidadChange={(id) => handleField('idLocalidad', id)}
          inputClassName="w-full rounded-lg border border-slate-200 px-3 py-2"
        />

        <div>
          <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Dirección</label>
          <input
            type="text"
            required
            maxLength={500}
            disabled={!form.idLocalidad}
            value={form.direccion}
            onChange={(e) => handleField('direccion', e.target.value)}
            className="w-full rounded-lg border border-slate-200 px-3 py-2 disabled:opacity-50"
          />
        </div>
      </div>

      <div className="flex flex-wrap gap-3 mt-8 pt-4 border-t border-slate-100">
        <button
          type="button"
          onClick={() => navigate(ADMIN_PATHS.pacienteDetalle(idPaciente))}
          className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
        >
          Cancelar
        </button>
        <button
          type="button"
          disabled={!isDirty}
          onClick={openSave}
          className={`px-5 py-2.5 rounded-lg font-semibold ${
            isDirty
              ? 'bg-emerald-700 text-white hover:bg-emerald-600'
              : 'bg-slate-200 text-slate-500 cursor-not-allowed'
          }`}
        >
          {isDirty ? 'Confirmar modificación' : 'Sin cambios'}
        </button>
      </div>

      <AdminPasswordConfirmModal
        open={showPwd}
        password={pwd}
        onPasswordChange={setPwd}
        onCancel={() => {
          setShowPwd(false);
          setPwd('');
        }}
        onConfirm={confirmSave}
        submitting={saving}
      />
    </section>
  );
};

export default AdminPacienteEditPage;
