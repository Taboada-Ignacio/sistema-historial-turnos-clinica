import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import ProfesionalFoto from '../../components/ProfesionalFoto';
import ProfesionalPortalNav from '../../components/ProfesionalPortalNav';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';
import SexoSelectField from '../../components/SexoSelectField';
import { useProfesionalSession } from '../../context/ProfesionalSessionContext';
import { apiErrorMessage } from '../../utils/adminApiError';
import { clearSession } from '../../utils/auth';
import { formatEspecialidad } from '../../utils/formatEspecialidad';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';

const ProfesionalPerfilPage = () => {
  const navigate = useNavigate();
  const { cargandoPerfil, error: sessionError, invalidate, refetch } = useProfesionalSession();
  const [profesional, setProfesional] = useState(null);
  const [loadingMe, setLoadingMe] = useState(true);
  const [especialidades, setEspecialidades] = useState([]);
  const [form, setForm] = useState(null);
  const [initialForm, setInitialForm] = useState(null);
  const [estadoActual, setEstadoActual] = useState('');
  const [fotoFile, setFotoFile] = useState(null);
  const [fotoPreview, setFotoPreview] = useState(null);
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoadingMe(true);
      setError('');
      try {
        const [resMe, resEsp] = await Promise.all([
          clienteAxios.get('/usuarios/api/profesionales/me'),
          clienteAxios.get('/usuarios/api/especialidades'),
        ]);
        if (cancelled) return;
        const p = resMe.data;
        setProfesional(p);
        setEstadoActual(p.estadoActual ?? 'ACTIVO');
        const picked = {
          nombre: p.nombre ?? '',
          apellido: p.apellido ?? '',
          dni: p.dni != null ? String(p.dni) : '',
          email: p.email ?? '',
          telefono: p.telefono ?? '',
          sexo: p.sexo ?? '',
          matricula: p.matricula ?? '',
          idEspecialidad: p.idEspecialidad != null ? String(p.idEspecialidad) : '',
          idProvincia: p.idProvincia != null ? String(p.idProvincia) : '',
          idLocalidad: p.idLocalidad != null ? String(p.idLocalidad) : '',
          direccion: p.direccion ?? '',
        };
        setForm(picked);
        setInitialForm({ ...picked });
        setEspecialidades(Array.isArray(resEsp.data) ? resEsp.data : []);
      } catch (err) {
        if (!cancelled) setError(apiErrorMessage(err, 'No se pudo cargar tu perfil profesional.'));
      } finally {
        if (!cancelled) setLoadingMe(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!fotoFile) {
      setFotoPreview(null);
      return undefined;
    }
    const url = URL.createObjectURL(fotoFile);
    setFotoPreview(url);
    return () => URL.revokeObjectURL(url);
  }, [fotoFile]);

  const isDirty = useMemo(() => {
    if (!form || !initialForm) return false;
    const formChanged = Object.keys(form).some((k) => (form[k] ?? '') !== (initialForm[k] ?? ''));
    return formChanged || Boolean(fotoFile);
  }, [form, initialForm, fotoFile]);

  const handleField = (name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    if (!isDirty || !profesional?.idUsuario || !form) return;
    if (!form.idLocalidad || !form.direccion?.trim()) {
      setError('La localidad y la dirección son obligatorias.');
      return;
    }
    if (!form.idEspecialidad) {
      setError('Seleccioná una especialidad.');
      return;
    }
    if (!form.matricula?.trim()) {
      setError('La matrícula es obligatoria.');
      return;
    }

    setSaving(true);
    setError('');
    setMensaje('');
    try {
      const payload = {
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        dni: parseInt(form.dni, 10),
        email: form.email.trim(),
        telefono: form.telefono.trim(),
        sexo: form.sexo,
        matricula: form.matricula.trim(),
        estadoActual,
        idEspecialidad: parseInt(form.idEspecialidad, 10),
        idLocalidad: parseInt(form.idLocalidad, 10),
        direccion: form.direccion.trim(),
      };

      if (fotoFile) {
        const formData = new FormData();
        formData.append('datos', new Blob([JSON.stringify(payload)], { type: 'application/json' }));
        formData.append('foto', fotoFile);
        await clienteAxios.put(`/usuarios/api/profesionales/${profesional.idUsuario}`, formData, {
          headers: { 'Content-Type': 'multipart/form-data' },
        });
      } else {
        await clienteAxios.put(`/usuarios/api/profesionales/${profesional.idUsuario}`, payload);
      }

      await invalidate();
      setFotoFile(null);
      setMensaje('Tus datos se actualizaron correctamente.');
      const { data } = await clienteAxios.get('/usuarios/api/profesionales/me');
      setProfesional(data);
      setInitialForm({ ...form });
    } catch (err) {
      setError(apiErrorMessage(err, 'No se pudo guardar. Revisá los datos ingresados.'));
    } finally {
      setSaving(false);
    }
  };

  const sessionErrorMsg = apiErrorMessage(sessionError, '');
  const loading = cargandoPerfil || loadingMe;

  const handleCerrarSesion = () => {
    clearSession();
    navigate(PROFESIONAL_PATHS.login);
  };

  if (!loading && !profesional && (error || sessionError)) {
    return (
      <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
        <ProfesionalPortalNav />
        <main className="max-w-3xl mx-auto px-6 py-10">
          <section className="bg-white rounded-2xl border border-red-200 p-6 shadow-sm">
            <h1 className="text-xl font-black text-slate-900 mb-2">No se pudo cargar tu perfil</h1>
            <p className="text-red-700 text-sm mb-6">
              {error || sessionErrorMsg || 'La sesión no corresponde a una cuenta profesional.'}
            </p>
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={() => {
                  refetch();
                  window.location.reload();
                }}
                className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
              >
                Reintentar
              </button>
              <button
                type="button"
                onClick={handleCerrarSesion}
                className="px-5 py-2.5 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700"
              >
                Ir al login profesional
              </button>
            </div>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      <ProfesionalPortalNav />

      <main className="max-w-3xl mx-auto px-6 py-10">
        <button
          type="button"
          onClick={() => navigate(PROFESIONAL_PATHS.dashboard)}
          className="mb-6 px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 text-sm font-semibold hover:bg-white"
        >
          ← Volver al panel
        </button>

        <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h1 className="text-2xl font-black text-slate-900 mb-2">Configuración de perfil</h1>
          <p className="text-slate-600 text-sm mb-6">
            Actualizá tus datos profesionales. Para cambiar la contraseña, usá{' '}
            <Link to={PROFESIONAL_PATHS.recuperarPassword} className="text-blue-600 font-semibold hover:underline">
              recuperar contraseña
            </Link>
            .
          </p>

          {error && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
              {error}
            </div>
          )}

          {mensaje && (
            <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-800">
              {mensaje}
            </div>
          )}

          {loading || !form ? (
            <p className="text-slate-500">Cargando perfil…</p>
          ) : (
            <>
              <div className="mb-6 flex flex-col sm:flex-row items-start gap-4">
                {fotoPreview ? (
                  <img
                    src={fotoPreview}
                    alt="Vista previa de foto"
                    className="w-24 h-24 object-cover rounded-2xl border-2 border-blue-300"
                  />
                ) : (
                  <ProfesionalFoto
                    fotoPerfil={profesional?.fotoPerfil}
                    alt="Foto de perfil"
                    className="w-24 h-24 object-cover rounded-2xl border-2 border-blue-100"
                    placeholderClassName="w-24 h-24 rounded-2xl border-2 border-gray-200 bg-gray-100 flex items-center justify-center text-xs font-bold text-gray-400"
                  />
                )}
                <div>
                  <label className="block text-xs font-bold text-slate-500 uppercase mb-1">
                    Foto de perfil (opcional)
                  </label>
                  <input
                    type="file"
                    accept="image/*"
                    onChange={(e) => setFotoFile(e.target.files?.[0] ?? null)}
                    className="text-sm text-slate-600"
                  />
                </div>
              </div>

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
                  <SexoSelectField
                    value={form.sexo}
                    onChange={(e) => handleField('sexo', e.target.value)}
                    inputClassName="w-full rounded-lg border border-slate-200 px-3 py-2"
                  />
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

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Matrícula</label>
                    <input
                      type="text"
                      required
                      value={form.matricula}
                      onChange={(e) => handleField('matricula', e.target.value)}
                      className="w-full rounded-lg border border-slate-200 px-3 py-2"
                    />
                  </div>
                  <div>
                    <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Especialidad</label>
                    <select
                      required
                      value={form.idEspecialidad}
                      onChange={(e) => handleField('idEspecialidad', e.target.value)}
                      className="w-full rounded-lg border border-slate-200 px-3 py-2"
                    >
                      <option value="">Seleccionar</option>
                      {especialidades.map((e) => (
                        <option key={e.idEspecialidad} value={e.idEspecialidad}>
                          {formatEspecialidad(e.descripcion)}
                        </option>
                      ))}
                    </select>
                  </div>
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
                  onClick={() => navigate(PROFESIONAL_PATHS.dashboard)}
                  className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
                >
                  Cancelar
                </button>
                <button
                  type="button"
                  disabled={!isDirty || saving}
                  onClick={handleSave}
                  className={`px-5 py-2.5 rounded-lg font-semibold ${
                    isDirty && !saving
                      ? 'bg-blue-600 text-white hover:bg-blue-700'
                      : 'bg-slate-200 text-slate-500 cursor-not-allowed'
                  }`}
                >
                  {saving ? 'Guardando…' : isDirty ? 'Guardar cambios' : 'Sin cambios'}
                </button>
              </div>
            </>
          )}
        </section>
      </main>
    </div>
  );
};

export default ProfesionalPerfilPage;
