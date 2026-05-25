import React, { useEffect, useMemo, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import PacientePortalNav from '../../components/PacientePortalNav';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';
import SexoSelectField from '../../components/SexoSelectField';
import { usePacienteSession } from '../../context/PacienteSessionContext';
import { apiErrorMessage } from '../../utils/adminApiError';
import { clearSession } from '../../utils/auth';
import {
  mensajePerfilSoloPortalProfesional,
  PERFIL_PACIENTE_NO_DISPONIBLE,
  apiErrorCode,
} from '../../utils/portalPaciente';
import { PACIENTE_PATHS, PROFESIONAL_PATHS } from '../../utils/portalPaths';

const PacientePerfilPage = () => {
  const navigate = useNavigate();
  const { sesion, cargandoPerfil, perfilEditable, error: sessionError, invalidate, refetch } =
    usePacienteSession();
  const [obrasSociales, setObrasSociales] = useState([]);
  const [form, setForm] = useState(null);
  const [initialForm, setInitialForm] = useState(null);
  const [error, setError] = useState('');
  const [mensaje, setMensaje] = useState('');
  const [saving, setSaving] = useState(false);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { data } = await clienteAxios.get('/usuarios/api/obras-sociales');
        if (!cancelled) setObrasSociales(Array.isArray(data) ? data : []);
      } catch {
        if (!cancelled) setError('No se pudieron cargar las obras sociales.');
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  useEffect(() => {
    if (!sesion || !perfilEditable) return;
    const picked = {
      nombre: sesion.nombre ?? '',
      apellido: sesion.apellido ?? '',
      dni: sesion.dni != null ? String(sesion.dni) : '',
      email: sesion.email ?? '',
      telefono: sesion.telefono ?? '',
      fechaNacimiento: sesion.fechaNacimiento ? String(sesion.fechaNacimiento).slice(0, 10) : '',
      sexo: sesion.sexo ?? '',
      numeroAfiliado: sesion.numeroAfiliado ?? '',
      idObraSocial: sesion.idObraSocial != null ? String(sesion.idObraSocial) : '',
      idProvincia: sesion.idProvincia != null ? String(sesion.idProvincia) : '',
      idLocalidad: sesion.idLocalidad != null ? String(sesion.idLocalidad) : '',
      direccion: sesion.direccion ?? '',
    };
    setForm(picked);
    setInitialForm({ ...picked });
  }, [sesion, perfilEditable]);

  const isDirty = useMemo(() => {
    if (!form || !initialForm) return false;
    return Object.keys(form).some((k) => (form[k] ?? '') !== (initialForm[k] ?? ''));
  }, [form, initialForm]);

  const handleField = (name, value) => {
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSave = async () => {
    if (!isDirty || !sesion?.idUsuario || !form) return;
    if (!form.idLocalidad || !form.direccion?.trim()) {
      setError('La localidad y la dirección son obligatorias.');
      return;
    }
    if (!form.idObraSocial) {
      setError('Seleccioná una obra social.');
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
        fechaNacimiento: form.fechaNacimiento || null,
        sexo: form.sexo,
        numeroAfiliado: form.numeroAfiliado.trim() || null,
        idObraSocial: parseInt(form.idObraSocial, 10),
        idLocalidad: parseInt(form.idLocalidad, 10),
        direccion: form.direccion.trim(),
      };
      await clienteAxios.put(`/usuarios/api/pacientes/${sesion.idUsuario}`, payload);
      await invalidate();
      setMensaje('Tus datos se actualizaron correctamente.');
    } catch (err) {
      if (apiErrorCode(err) === PERFIL_PACIENTE_NO_DISPONIBLE) {
        setError(mensajePerfilSoloPortalProfesional);
      } else {
        setError(apiErrorMessage(err, 'No se pudo guardar. Revisá los datos ingresados.'));
      }
    } finally {
      setSaving(false);
    }
  };

  const sessionErrorMsg = apiErrorMessage(sessionError, '');

  const handleCerrarSesion = () => {
    clearSession();
    navigate(PACIENTE_PATHS.login);
  };

  if (!cargandoPerfil && !perfilEditable && sesion) {
    return (
      <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
        <PacientePortalNav />
        <main className="max-w-3xl mx-auto px-6 py-10">
          <button
            type="button"
            onClick={() => navigate(PACIENTE_PATHS.dashboard)}
            className="mb-6 px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 text-sm font-semibold hover:bg-white"
          >
            ← Volver al panel
          </button>
          <section className="bg-white rounded-2xl border border-blue-200 p-6 shadow-sm">
            <div className="rounded-xl border border-blue-200 bg-blue-50 px-4 py-4 mb-6">
              <h1 className="text-xl font-black text-blue-900 mb-2">Cuenta profesional en modo paciente</h1>
              <p className="text-blue-900 text-sm">{mensajePerfilSoloPortalProfesional}</p>
            </div>
            <Link
              to={PROFESIONAL_PATHS.login}
              className="inline-flex items-center px-5 py-2.5 rounded-lg border border-clinica-dark/35 bg-white text-clinica-dark font-semibold hover:bg-clinica-light hover:border-clinica-dark/50 transition-colors"
            >
              Ir al portal profesional
            </Link>
          </section>
        </main>
      </div>
    );
  }

  if (!cargandoPerfil && !sesion && sessionError) {
    return (
      <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
        <PacientePortalNav />
        <main className="max-w-3xl mx-auto px-6 py-10">
          <section className="bg-white rounded-2xl border border-red-200 p-6 shadow-sm">
            <h1 className="text-xl font-black text-slate-900 mb-2">No se pudo cargar tu perfil</h1>
            <p className="text-red-700 text-sm mb-6">
              {sessionErrorMsg || 'La sesión no corresponde a una cuenta de paciente.'}
            </p>
            <p className="text-slate-600 text-sm mb-6">
              Si sos profesional de la salud, cerrá sesión e ingresá desde el portal profesional. Si sos paciente,
              contactá soporte.
            </p>
            <div className="flex flex-wrap gap-3">
              <button
                type="button"
                onClick={() => refetch()}
                className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
              >
                Reintentar
              </button>
              <button
                type="button"
                onClick={handleCerrarSesion}
                className="px-5 py-2.5 rounded-lg bg-clinica-dark text-white font-semibold hover:bg-clinica-hover"
              >
                Ir al login de paciente
              </button>
            </div>
          </section>
        </main>
      </div>
    );
  }

  return (
    <div className="min-h-screen bg-slate-50 font-sans text-slate-800">
      <PacientePortalNav />

      <main className="max-w-3xl mx-auto px-6 py-10">
        <button
          type="button"
          onClick={() => navigate(PACIENTE_PATHS.dashboard)}
          className="mb-6 px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 text-sm font-semibold hover:bg-white"
        >
          ← Volver al panel
        </button>

        <section className="bg-white rounded-2xl border border-slate-200 p-6 shadow-sm">
          <h1 className="text-2xl font-black text-slate-900 mb-2">Mi perfil</h1>
          <p className="text-slate-600 text-sm mb-6">
            Actualizá tus datos personales y de contacto. Para cambiar tu contraseña, usá{' '}
            <Link to={PACIENTE_PATHS.recuperarPassword} className="text-clinica-dark font-semibold hover:underline">
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

          {cargandoPerfil ? (
            <p className="text-slate-500">Cargando perfil…</p>
          ) : !form ? (
            <p className="text-slate-500">No hay datos de perfil disponibles.</p>
          ) : (
            <>
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

                <div className="grid grid-cols-1 sm:grid-cols-2 gap-4">
                  <div>
                    <label className="block text-xs font-bold text-slate-500 uppercase mb-1">Fecha de nacimiento</label>
                    <input
                      type="date"
                      value={form.fechaNacimiento}
                      onChange={(e) => handleField('fechaNacimiento', e.target.value)}
                      className="w-full rounded-lg border border-slate-200 px-3 py-2"
                    />
                  </div>
                  <SexoSelectField
                    value={form.sexo}
                    onChange={(e) => handleField('sexo', e.target.value)}
                    label="Sexo"
                    showLabels
                    inputClassName="w-full rounded-lg border border-slate-200 px-3 py-2"
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
                  onClick={() => navigate(PACIENTE_PATHS.dashboard)}
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
                      ? 'bg-emerald-700 text-white hover:bg-emerald-600'
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

export default PacientePerfilPage;
