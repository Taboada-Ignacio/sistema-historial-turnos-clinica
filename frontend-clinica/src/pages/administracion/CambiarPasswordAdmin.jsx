import React, { useMemo, useRef, useState } from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import PasswordVisibilityToggle from '../../components/PasswordVisibilityToggle';
import { ADMIN_PATHS } from '../../utils/portalPaths';
import { useSubmitCambioPasswordConToken } from '../../hooks/useSubmitCambioPasswordConToken';

const CambiarPasswordAdmin = () => {
  const [searchParams] = useSearchParams();
  const token = useMemo(() => searchParams.get('token') || '', [searchParams]);
  const [passwordNueva, setPasswordNueva] = useState('');
  const [confirmPasswordNueva, setConfirmPasswordNueva] = useState('');
  const [showNueva, setShowNueva] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);

  const lockRef = useRef(false);

  const { isSubmitting, completado, formDisabled, error, success, submitPasswordChange } =
    useSubmitCambioPasswordConToken('admin', ADMIN_PATHS.login, 'admin');

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (lockRef.current) return;
    lockRef.current = true;

    let result = { completed: false };
    try {
      result = await submitPasswordChange(lockRef, {
        token,
        passwordNueva,
        confirmPasswordNueva,
      });
    } finally {
      if (!result?.completed) {
        lockRef.current = false;
      }
    }
  };

  return (
    <div className="min-h-screen bg-red-50 flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white border border-red-200 rounded-2xl shadow-lg p-8">
        <h1 className="text-2xl font-black text-red-900 mb-2">Nueva contraseña administrador</h1>
        <p className="text-sm text-red-700 mb-6">Definí una nueva contraseña para el panel de administración.</p>

        {error && <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{error}</div>}
        {success && <div className="mb-4 p-3 rounded-lg bg-green-50 text-green-700 text-sm">{success}</div>}

        <form onSubmit={handleSubmit} className="space-y-4" noValidate>
          <div>
            <label className="text-sm font-semibold text-gray-700">Nueva contraseña</label>
            <div className="relative mt-1">
              <input
                type={showNueva ? 'text' : 'password'}
                autoComplete="new-password"
                required
                disabled={formDisabled}
                value={passwordNueva}
                onChange={(e) => setPasswordNueva(e.target.value)}
                className="w-full pl-3 pr-12 py-2 rounded-lg border border-gray-300 disabled:opacity-60"
              />
              <PasswordVisibilityToggle visible={showNueva} onToggle={() => setShowNueva((v) => !v)} />
            </div>
          </div>

          <div>
            <label className="text-sm font-semibold text-gray-700">Confirmar contraseña</label>
            <div className="relative mt-1">
              <input
                type={showConfirm ? 'text' : 'password'}
                autoComplete="new-password"
                required
                disabled={formDisabled}
                value={confirmPasswordNueva}
                onChange={(e) => setConfirmPasswordNueva(e.target.value)}
                className="w-full pl-3 pr-12 py-2 rounded-lg border border-gray-300 disabled:opacity-60"
              />
              <PasswordVisibilityToggle visible={showConfirm} onToggle={() => setShowConfirm((v) => !v)} />
            </div>
          </div>

          <button
            type="submit"
            disabled={isSubmitting || completado}
            className="w-full bg-red-700 text-white py-2.5 rounded-lg font-bold hover:bg-red-600 disabled:opacity-70"
          >
            {isSubmitting ? 'Guardando...' : 'Guardar nueva contraseña'}
          </button>
        </form>

        <Link to={ADMIN_PATHS.login} className="inline-block mt-4 text-sm text-gray-600 hover:text-gray-900">
          Volver al login de administración
        </Link>
      </div>
    </div>
  );
};

export default CambiarPasswordAdmin;
