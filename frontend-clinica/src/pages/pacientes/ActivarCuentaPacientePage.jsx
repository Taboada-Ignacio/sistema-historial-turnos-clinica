import React, { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { clienteAxiosPublic } from '../../api/axiosConfig';
import PasswordVisibilityToggle from '../../components/PasswordVisibilityToggle';
import { PACIENTE_PATHS } from '../../utils/portalPaths';
import { formatSexoLabel } from '../../components/SexoSelectField';

const Campo = ({ etiqueta, valor }) => (
  <div>
    <dt className="text-xs font-bold text-gray-500 uppercase">{etiqueta}</dt>
    <dd className="text-sm font-semibold text-gray-900 mt-0.5">{valor ?? '—'}</dd>
  </div>
);

const ActivarCuentaPacientePage = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = useMemo(() => searchParams.get('token') || '', [searchParams]);
  const [datos, setDatos] = useState(null);
  const [passwordNueva, setPasswordNueva] = useState('');
  const [confirmPassword, setConfirmPassword] = useState('');
  const [showNueva, setShowNueva] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const [loadingDatos, setLoadingDatos] = useState(true);
  const [loadingSubmit, setLoadingSubmit] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const lockRef = useRef(false);

  useEffect(() => {
    let cancelado = false;
    const cargar = async () => {
      if (!token) {
        setError('Enlace inválido o sin token.');
        setLoadingDatos(false);
        return;
      }
      try {
        const { data } = await clienteAxiosPublic.get('/usuarios/api/auth/datos-activacion-paciente', {
          params: { token },
        });
        if (!cancelado) setDatos(data);
      } catch (err) {
        if (!cancelado) {
          setError(err.response?.data?.message || err.response?.data?.mensaje || 'No se pudo cargar la activación.');
        }
      } finally {
        if (!cancelado) setLoadingDatos(false);
      }
    };
    cargar();
    return () => {
      cancelado = true;
    };
  }, [token]);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (lockRef.current || loadingSubmit) return;
    setError('');
    if (passwordNueva !== confirmPassword) {
      setError('La confirmación de contraseña no coincide.');
      return;
    }
    lockRef.current = true;
    setLoadingSubmit(true);
    try {
      const { data } = await clienteAxiosPublic.post('/usuarios/api/auth/establecer-password-inicial/paciente', {
        token,
        passwordNueva,
      });
      setSuccess(data?.message || 'Cuenta activada correctamente.');
      setTimeout(() => navigate(PACIENTE_PATHS.login), 2000);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.mensaje || 'No se pudo activar la cuenta.');
      lockRef.current = false;
    } finally {
      setLoadingSubmit(false);
    }
  };

  const ubicacion = [datos?.nombreLocalidad, datos?.nombreProvincia].filter(Boolean).join(', ');

  return (
    <div className="min-h-screen bg-emerald-50 flex items-center justify-center p-6">
      <div className="w-full max-w-lg bg-white border border-emerald-200 rounded-2xl shadow-lg p-8">
        <h1 className="text-2xl font-black text-emerald-800 mb-2">Activá tu cuenta</h1>
        <p className="text-sm text-emerald-700 mb-6">
          Un profesional registró tus datos. Revisalos y creá tu contraseña para ingresar al portal paciente.
        </p>

        {loadingDatos && <p className="text-sm text-gray-500">Cargando datos…</p>}
        {error && !loadingDatos && (
          <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{error}</div>
        )}
        {success && <div className="mb-4 p-3 rounded-lg bg-green-50 text-green-700 text-sm">{success}</div>}

        {datos && !loadingDatos && (
          <>
            <section className="mb-6 rounded-xl border border-gray-200 bg-gray-50 p-4">
              <h2 className="text-sm font-bold text-gray-800 mb-3">Tus datos registrados</h2>
              <dl className="grid gap-3 sm:grid-cols-2">
                <Campo etiqueta="Apellido" valor={datos.apellido} />
                <Campo etiqueta="Nombre" valor={datos.nombre} />
                <Campo etiqueta="DNI" valor={datos.dni} />
                <Campo etiqueta="Email" valor={datos.email} />
                <Campo etiqueta="Teléfono" valor={datos.telefono} />
                <Campo etiqueta="Fecha de nacimiento" valor={datos.fechaNacimiento} />
                <Campo etiqueta="Sexo" valor={formatSexoLabel(datos.sexo)} />
                <Campo etiqueta="Obra social" valor={datos.nombreObraSocial} />
                <Campo etiqueta="Nº afiliado" valor={datos.numeroAfiliado} />
                <div className="sm:col-span-2">
                  <Campo etiqueta="Dirección" valor={[datos.direccion, ubicacion].filter(Boolean).join(' — ')} />
                </div>
              </dl>
            </section>

            <form onSubmit={handleSubmit} className="space-y-4" noValidate>
              <div>
                <label className="text-sm font-semibold text-gray-700">Contraseña</label>
                <div className="relative mt-1">
                  <input
                    type={showNueva ? 'text' : 'password'}
                    required
                    minLength={8}
                    value={passwordNueva}
                    onChange={(e) => setPasswordNueva(e.target.value)}
                    className="w-full pl-3 pr-12 py-2 rounded-lg border border-gray-300"
                  />
                  <PasswordVisibilityToggle visible={showNueva} onToggle={() => setShowNueva((v) => !v)} />
                </div>
              </div>
              <div>
                <label className="text-sm font-semibold text-gray-700">Confirmar contraseña</label>
                <div className="relative mt-1">
                  <input
                    type={showConfirm ? 'text' : 'password'}
                    required
                    minLength={8}
                    value={confirmPassword}
                    onChange={(e) => setConfirmPassword(e.target.value)}
                    className="w-full pl-3 pr-12 py-2 rounded-lg border border-gray-300"
                  />
                  <PasswordVisibilityToggle visible={showConfirm} onToggle={() => setShowConfirm((v) => !v)} />
                </div>
              </div>
              <button
                type="submit"
                disabled={loadingSubmit || Boolean(success)}
                className="w-full bg-emerald-700 text-white py-2.5 rounded-lg font-bold hover:bg-emerald-600 disabled:opacity-70"
              >
                {loadingSubmit ? 'Activando…' : 'Confirmar y activar cuenta'}
              </button>
            </form>
          </>
        )}

        <Link to={PACIENTE_PATHS.login} className="inline-block mt-4 text-sm text-gray-600 hover:text-gray-900">
          Volver al login
        </Link>
      </div>
    </div>
  );
};

export default ActivarCuentaPacientePage;
