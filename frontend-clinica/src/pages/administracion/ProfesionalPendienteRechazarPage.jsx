import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import PasswordVisibilityToggle from '../../components/PasswordVisibilityToggle';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const apiErrorMessage = (err) => {
  const data = err.response?.data;
  if (!data) return 'Ocurrió un error al procesar el rechazo.';
  if (data.mensaje) return data.mensaje;
  if (data.message) return data.message;
  if (typeof data === 'object') {
    const first = Object.values(data).find((v) => typeof v === 'string');
    if (first) return first;
  }
  return 'Ocurrió un error al procesar el rechazo.';
};

const MIN_MOTIVO = 10;

const ProfesionalPendienteRechazarPage = () => {
  const { idProfesional } = useParams();
  const navigate = useNavigate();
  const [profesional, setProfesional] = useState(null);
  const [loading, setLoading] = useState(true);
  const [submitting, setSubmitting] = useState(false);
  const [error, setError] = useState('');
  const [motivo, setMotivo] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);

  const detalleUrl = `${ADMIN_PATHS.profesionalesPendientes}/${idProfesional}`;

  useEffect(() => {
    const fetchProfesional = async () => {
      setLoading(true);
      setError('');
      try {
        const response = await clienteAxios.get(`/usuarios/api/profesionales/${idProfesional}`);
        const data = response.data;
        setProfesional(data);
        if (data.membresiaActual !== 'SIN_VERIFICAR') {
          setError('Este profesional ya no está pendiente de verificación y no puede rechazarse desde aquí.');
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

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');

    const motivoTrim = motivo.trim();
    if (motivoTrim.length < MIN_MOTIVO) {
      setError(`El motivo debe tener al menos ${MIN_MOTIVO} caracteres.`);
      return;
    }
    if (!password.trim()) {
      setError('Ingresá tu contraseña de administrador para confirmar el borrado.');
      return;
    }

    setSubmitting(true);
    try {
      await clienteAxios.post(`/usuarios/api/profesionales/${idProfesional}/rechazar-pendiente`, {
        motivo: motivoTrim,
        password,
      });
      navigate(ADMIN_PATHS.profesionalesPendientes, {
        state: {
          mensaje: `Se notificó a ${profesional?.email ?? 'el profesional'} y se eliminó la solicitud pendiente.`,
        },
      });
    } catch (err) {
      setError(apiErrorMessage(err));
    } finally {
      setSubmitting(false);
    }
  };

  const cardClass =
    'w-full max-w-2xl mx-auto bg-white rounded-xl border border-slate-200 p-6 md:p-8';

  if (loading) {
    return (
      <div className="flex justify-center w-full">
        <div className={`${cardClass} text-center text-slate-600`}>Cargando…</div>
      </div>
    );
  }

  if (!profesional) {
    return (
      <div className="flex justify-center w-full">
        <div className={`${cardClass} text-center`}>
          <p className="text-red-600 mb-4">{error || 'Profesional no encontrado.'}</p>
          <Link to={ADMIN_PATHS.profesionalesPendientes} className="text-slate-700 underline font-medium">
            Volver al listado
          </Link>
        </div>
      </div>
    );
  }

  const puedeRechazar = profesional.membresiaActual === 'SIN_VERIFICAR';

  return (
    <div className="flex justify-center w-full">
      <section className={cardClass}>
      <div className="text-center mb-6">
        <Link
          to={detalleUrl}
          className="text-sm text-slate-600 hover:text-slate-900 font-medium inline-block mb-4"
        >
          ← Volver al detalle
        </Link>
        <h2 className="text-2xl font-black text-slate-900 mb-1">Rechazar solicitud profesional</h2>
        <p className="text-slate-600 text-sm">
          {profesional.apellido}, {profesional.nombre}
          <br />
          <span className="font-mono text-xs">{profesional.email}</span>
        </p>
      </div>

      {!puedeRechazar && (
        <div className="mb-6 rounded-lg border border-amber-200 bg-amber-50 px-4 py-3 text-amber-900 text-sm">
          {error ||
            `Membresía actual: ${profesional.membresiaActual}. Solo se pueden rechazar solicitudes con SIN_VERIFICAR.`}
        </div>
      )}

      {puedeRechazar && (
        <>
          <p className="text-sm text-slate-600 mb-4 text-center">
            Indicá el motivo del rechazo. Se enviará un correo al profesional y, tras confirmar con tu contraseña, se
            eliminará su registro pendiente.
          </p>

          {error && (
            <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700" role="alert">
              {error}
            </div>
          )}

          <form onSubmit={handleSubmit} className="space-y-5">
            <div>
              <label htmlFor="motivo" className="block text-xs font-bold text-slate-500 uppercase mb-2">
                Motivo del rechazo
              </label>
              <textarea
                id="motivo"
                name="motivo"
                required
                minLength={MIN_MOTIVO}
                maxLength={1000}
                rows={5}
                value={motivo}
                onChange={(e) => setMotivo(e.target.value)}
                placeholder="Ej.: La matrícula no pudo verificarse o la documentación enviada es incompleta."
                className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-red-600/30 resize-y"
              />
              <p className="text-xs text-slate-500 mt-1">Mínimo {MIN_MOTIVO} caracteres.</p>
            </div>

            <div>
              <label htmlFor="password-admin" className="block text-xs font-bold text-slate-500 uppercase mb-2">
                Tu contraseña de administrador
              </label>
              <div className="relative">
                <input
                  id="password-admin"
                  type={showPassword ? 'text' : 'password'}
                  autoComplete="current-password"
                  value={password}
                  onChange={(e) => setPassword(e.target.value)}
                  className="w-full rounded-xl border border-slate-200 pl-4 pr-12 py-3 text-sm outline-none focus:ring-2 focus:ring-red-600/30"
                  placeholder="Contraseña"
                />
                <PasswordVisibilityToggle visible={showPassword} onToggle={() => setShowPassword((v) => !v)} />
              </div>
            </div>

            <div className="flex flex-wrap justify-center gap-3 pt-4 border-t border-slate-100">
              <Link
                to={detalleUrl}
                className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
              >
                Cancelar
              </Link>
              <button
                type="submit"
                disabled={submitting}
                className="px-5 py-2.5 rounded-lg bg-red-700 text-white font-semibold hover:bg-red-600 disabled:opacity-50 disabled:cursor-not-allowed"
              >
                {submitting ? 'Procesando…' : 'Confirmar borrado de profesional pendiente'}
              </button>
            </div>
          </form>
        </>
      )}

      {!puedeRechazar && (
        <div className="text-center">
          <Link
            to={ADMIN_PATHS.profesionalesPendientes}
            className="inline-block mt-4 text-slate-700 underline font-medium"
          >
            Volver al listado
          </Link>
        </div>
      )}
    </section>
    </div>
  );
};

export default ProfesionalPendienteRechazarPage;
