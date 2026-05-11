import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { profesionalFotoAbsoluteUrl } from '../../utils/profesionalFotoUrl';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const apiErrorMessage = (err) =>
  err.response?.data?.mensaje ||
  err.response?.data?.message ||
  err.response?.data?.error ||
  'Ocurrió un error.';

function formatFechaNacimiento(value) {
  if (value == null || value === '') return '—';
  if (typeof value === 'string') {
    const d = value.slice(0, 10);
    if (/^\d{4}-\d{2}-\d{2}$/.test(d)) {
      const [y, m, day] = d.split('-');
      return `${day}/${m}/${y}`;
    }
    return value;
  }
  if (Array.isArray(value) && value.length >= 3) {
    const [y, m, day] = value;
    return `${String(day).padStart(2, '0')}/${String(m).padStart(2, '0')}/${y}`;
  }
  return String(value);
}

const ProfesionalPendienteDetallePage = () => {
  const { idProfesional } = useParams();
  const navigate = useNavigate();
  const [profesional, setProfesional] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [notPendiente, setNotPendiente] = useState(false);

  useEffect(() => {
    const fetchProfesional = async () => {
      setLoading(true);
      setError('');
      setNotPendiente(false);
      try {
        const response = await clienteAxios.get(`/usuarios/api/profesionales/${idProfesional}`);
        const data = response.data;
        setProfesional(data);
        if (data.membresiaActual !== 'SIN_VERIFICAR') {
          setNotPendiente(true);
        }
      } catch (err) {
        setError(apiErrorMessage(err));
        setProfesional(null);
      } finally {
        setLoading(false);
      }
    };

    fetchProfesional();
  }, [idProfesional]);

  const handleVerificar = async () => {
    if (!profesional || profesional.membresiaActual !== 'SIN_VERIFICAR') return;
    setSaving(true);
    setError('');
    setSuccess('');

    try {
      await clienteAxios.put(`/usuarios/api/profesionales/${idProfesional}/verificar-matricula`);
      setSuccess('Profesional verificado: membresía actualizada a INACTIVA.');
      setProfesional((prev) => ({ ...prev, membresiaActual: 'INACTIVA' }));
      setNotPendiente(true);
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="bg-white rounded-xl border border-slate-200 p-6">Cargando detalle...</div>;
  }

  if (!profesional) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <p className="text-red-600 mb-4">{error || 'Profesional no encontrado.'}</p>
        <Link to={ADMIN_PATHS.profesionalesPendientes} className="text-slate-700 underline font-medium">
          Volver a profesionales pendientes
        </Link>
      </div>
    );
  }

  const fotoUrl = profesionalFotoAbsoluteUrl(profesional.fotoPerfil);
  const rolesList = profesional.roles ? Array.from(profesional.roles).sort() : [];

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <button
        type="button"
        onClick={() => navigate(ADMIN_PATHS.profesionalesPendientes)}
        className="text-sm text-slate-600 hover:text-slate-900 mb-4 font-medium"
      >
        ← Volver a profesionales pendientes
      </button>

      {notPendiente && (
        <div
          className="mb-6 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-amber-900 text-sm"
          role="status"
        >
          {profesional.membresiaActual === 'INACTIVA' && success ? (
            <p>{success}</p>
          ) : (
            <p>
              Este profesional ya no está pendiente de revisión por matrícula (membresía actual:{' '}
              <strong className="font-mono">{profesional.membresiaActual}</strong>). Podés volver al listado; la
              verificación no aplica en este estado.
            </p>
          )}
        </div>
      )}

      <div className="grid grid-cols-1 lg:grid-cols-[220px_1fr] gap-8">
        <div>
          {fotoUrl ? (
            <img
              src={fotoUrl}
              alt={`Perfil de ${profesional.nombre} ${profesional.apellido}`}
              className="w-full max-w-[220px] aspect-square object-cover rounded-xl border border-slate-200 mx-auto lg:mx-0"
            />
          ) : (
            <div className="w-full max-w-[220px] aspect-square rounded-xl border border-slate-200 bg-slate-100 flex items-center justify-center text-slate-500 text-sm mx-auto lg:mx-0">
              Sin foto de perfil
            </div>
          )}
        </div>

        <div className="min-w-0">
          <h2 className="text-2xl font-black text-slate-900 mb-1">
            {profesional.apellido}, {profesional.nombre}
          </h2>
          <p className="text-slate-500 text-sm mb-6">ID usuario: {profesional.idUsuario}</p>

          <div className="space-y-8">
            <div>
              <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">Contacto y documento</h3>
              <dl className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 text-sm">
                <div>
                  <dt className="text-slate-500">Email</dt>
                  <dd className="font-medium text-slate-900 break-all">{profesional.email}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Teléfono</dt>
                  <dd className="font-medium text-slate-900">{profesional.telefono ?? '—'}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">DNI</dt>
                  <dd className="font-medium text-slate-900">{profesional.dni ?? '—'}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Fecha de nacimiento</dt>
                  <dd className="font-medium text-slate-900">{formatFechaNacimiento(profesional.fechaNacimiento)}</dd>
                </div>
              </dl>
            </div>

            <div>
              <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">Datos profesionales</h3>
              <dl className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 text-sm">
                <div>
                  <dt className="text-slate-500">Matrícula</dt>
                  <dd className="font-medium text-slate-900">{profesional.matricula}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Especialidad</dt>
                  <dd className="font-medium text-slate-900">{profesional.especialidad ?? '—'}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Membresía actual</dt>
                  <dd className="font-mono font-medium text-slate-900">{profesional.membresiaActual}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Estado de cuenta</dt>
                  <dd className="font-medium text-slate-900">{profesional.estadoActual ?? '—'}</dd>
                </div>
              </dl>
            </div>

            <div>
              <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">Ubicación</h3>
              <dl className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 text-sm">
                <div className="sm:col-span-2">
                  <dt className="text-slate-500">Dirección</dt>
                  <dd className="font-medium text-slate-900">{profesional.direccion?.trim() || '—'}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Localidad</dt>
                  <dd className="font-medium text-slate-900">{profesional.nombreLocalidad ?? '—'}</dd>
                </div>
                <div>
                  <dt className="text-slate-500">Provincia</dt>
                  <dd className="font-medium text-slate-900">{profesional.nombreProvincia ?? '—'}</dd>
                </div>
              </dl>
            </div>

            <div>
              <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">Roles</h3>
              {rolesList.length > 0 ? (
                <ul className="flex flex-wrap gap-2">
                  {rolesList.map((r) => (
                    <li key={r} className="px-2.5 py-1 rounded-md bg-slate-100 text-slate-800 text-xs font-mono">
                      {r}
                    </li>
                  ))}
                </ul>
              ) : (
                <p className="text-sm text-slate-600">—</p>
              )}
            </div>
          </div>

          {error && <p className="text-red-600 mt-6 text-sm">{error}</p>}

          <div className="mt-8 flex flex-wrap gap-3">
            <button
              type="button"
              onClick={() => navigate(ADMIN_PATHS.profesionalesPendientes)}
              className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
            >
              Volver atrás
            </button>
            <button
              type="button"
              disabled={notPendiente || saving}
              onClick={handleVerificar}
              className="bg-emerald-700 text-white px-5 py-2.5 rounded-lg font-semibold hover:bg-emerald-600 disabled:opacity-50 disabled:cursor-not-allowed"
            >
              {saving ? 'Verificando…' : 'Verificar profesional'}
            </button>
          </div>
        </div>
      </div>
    </section>
  );
};

export default ProfesionalPendienteDetallePage;
