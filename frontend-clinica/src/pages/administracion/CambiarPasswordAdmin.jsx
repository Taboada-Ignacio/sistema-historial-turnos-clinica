import React, { useMemo, useRef, useState } from 'react';
import { Link, useNavigate, useSearchParams } from 'react-router-dom';
import { clienteAxiosPublic } from '../../api/axiosConfig';
import PasswordVisibilityToggle from '../../components/PasswordVisibilityToggle';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const CambiarPasswordAdmin = () => {
  const navigate = useNavigate();
  const [searchParams] = useSearchParams();
  const token = useMemo(() => searchParams.get('token') || '', [searchParams]);

  const [passwordNueva, setPasswordNueva] = useState('');
  const [confirmPasswordNueva, setConfirmPasswordNueva] = useState('');
  const [loading, setLoading] = useState(false);

  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const [showNueva, setShowNueva] = useState(false);
  const [showConfirm, setShowConfirm] = useState(false);
  const submittingRef = useRef(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading || submittingRef.current) return;

    setError('');
    setSuccess('');

    if (!token) {
      setError('Token inválido o ausente.');
      return;
    }

    if (passwordNueva !== confirmPasswordNueva) {
      setError('La confirmación de contraseña no coincide.');
      return;
    }

    submittingRef.current = true;
    setLoading(true);
    try {
      const response = await clienteAxiosPublic.post('/usuarios/api/auth/cambiar-password-con-token/admin', {
        token,
        passwordNueva,
      });
      setSuccess(response.data?.message || 'Contraseña actualizada correctamente.');
      setTimeout(() => navigate(ADMIN_PATHS.login), 1500);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.mensaje || 'No se pudo cambiar la contraseña.');
    } finally {
      setLoading(false);
      submittingRef.current = false;
    }
  };

  return (
    <div className="min-h-screen bg-red-50 flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white border border-red-200 rounded-2xl shadow-lg p-8">
        <h1 className="text-2xl font-black text-red-900 mb-2">Nueva contraseña administrador</h1>
        <p className="text-sm text-red-700 mb-6">Definí una nueva contraseña para el panel de administración.</p>

        {error && <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{error}</div>}
        {success && <div className="mb-4 p-3 rounded-lg bg-green-50 text-green-700 text-sm">{success}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-sm font-semibold text-gray-700">Nueva contraseña</label>
            <div className="relative mt-1">
              <input
                type={showNueva ? 'text' : 'password'}
                required
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
                value={confirmPasswordNueva}
                onChange={(e) => setConfirmPasswordNueva(e.target.value)}
                className="w-full pl-3 pr-12 py-2 rounded-lg border border-gray-300"
              />
              <PasswordVisibilityToggle visible={showConfirm} onToggle={() => setShowConfirm((v) => !v)} />
            </div>
          </div>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-red-700 text-white py-2.5 rounded-lg font-bold hover:bg-red-600 disabled:opacity-70"
          >
            {loading ? 'Guardando...' : 'Guardar nueva contraseña'}
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

