import React, { useEffect, useState } from 'react';
import { Link, useLocation, useNavigate, useSearchParams } from 'react-router-dom';
import { clienteAxiosPublic } from '../../../api/axiosConfig';
import { ADMIN_PATHS } from '../../../utils/portalPaths';
import {
  ADMIN_ONBOARDING_API,
  persistAdminOnboardingEmail,
  resolveAdminOnboardingEmail,
} from '../../../utils/adminOnboarding';

const RESEND_SECONDS = 180;

const AdminConfirmarEmailPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const [searchParams] = useSearchParams();
  const email = resolveAdminOnboardingEmail(location.state, searchParams);

  const [codigo, setCodigo] = useState('');
  const [timeLeft, setTimeLeft] = useState(RESEND_SECONDS);
  const [loading, setLoading] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [message, setMessage] = useState('');
  const [cuentaConfirmada, setCuentaConfirmada] = useState(false);

  useEffect(() => {
    if (email) {
      persistAdminOnboardingEmail(email);
    }
  }, [email]);

  useEffect(() => {
    if (timeLeft > 0 && !cuentaConfirmada) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft, cuentaConfirmada]);

  const handleCodigoChange = (e) => {
    setCodigo(e.target.value.replace(/\D/g, '').slice(0, 6));
  };

  const handleVerificar = async (e) => {
    e.preventDefault();
    if (cuentaConfirmada) return;

    if (!email) {
      setMessage('✗ No hay correo en sesión. Volvé al setup e intentá de nuevo.');
      return;
    }
    if (codigo.length !== 6) {
      setMessage('✗ Ingresá el código de 6 dígitos del correo.');
      return;
    }

    try {
      setVerifying(true);
      setMessage('');
      await clienteAxiosPublic.post(ADMIN_ONBOARDING_API.confirmarCodigo, { email, codigo });
      navigate(ADMIN_PATHS.registroExitoso, { replace: true });
    } catch (error) {
      const backendMessage = error?.response?.data?.mensaje || '';
      if (backendMessage.toLowerCase().includes('ya se encuentra activa')) {
        setCuentaConfirmada(true);
        setTimeLeft(0);
        setMessage('✓ Este registro ya fue confirmado.');
      } else if (backendMessage.toLowerCase().includes('expir')) {
        setMessage('✗ El código expiró (72 horas). Pedí un nuevo correo.');
      } else {
        setMessage('✗ Código inválido. Revisá el correo o solicitá uno nuevo.');
      }
    } finally {
      setVerifying(false);
    }
  };

  const handleReenviar = async () => {
    if (cuentaConfirmada) return;
    if (!email) {
      setMessage('✗ No hay correo en sesión. Volvé al setup e intentá de nuevo.');
      return;
    }
    try {
      setLoading(true);
      setMessage('');
      await clienteAxiosPublic.post(ADMIN_ONBOARDING_API.reenviarConfirmacion(email));
      setTimeLeft(RESEND_SECONDS);
      setCodigo('');
      setMessage('✓ Código y enlace reenviados. Revisá tu bandeja de entrada.');
    } catch (error) {
      const backendMessage = error?.response?.data?.mensaje || '';
      if (backendMessage.toLowerCase().includes('ya se encuentra activa')) {
        setCuentaConfirmada(true);
        setTimeLeft(0);
        setMessage('✓ Este registro ya fue confirmado.');
      } else {
        setMessage('✗ Error al reenviar el correo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="bg-white p-10 rounded-xl shadow-2xl text-center max-w-md w-full border-t-4 border-red-600">
        <div className="mb-6">
          <div className="text-5xl mb-4">✉️</div>
          <h2 className="text-2xl font-black mb-2 text-gray-900">Verificá tu correo (admin)</h2>
          <p className="text-gray-500 text-sm">
            Enviamos un código de 6 dígitos y un enlace a <b>{email || 'tu email'}</b>
          </p>
        </div>

        {!email && (
          <p className="mb-4 text-sm text-amber-700 bg-amber-50 p-3 rounded-lg">
            No detectamos el email del alta.{' '}
            <Link to={ADMIN_PATHS.setup} className="font-bold underline">
              Volver al setup
            </Link>
          </p>
        )}

        {!cuentaConfirmada && (
          <div className="bg-gray-50 border border-gray-200 p-6 rounded-2xl mb-6">
            <p className="text-sm text-gray-600 mb-3">Válido por 72 h · reenviar en:</p>
            <span className="text-4xl font-mono font-black text-red-600">
              {Math.floor(timeLeft / 60)}:{String(timeLeft % 60).padStart(2, '0')}
            </span>
          </div>
        )}

        <form onSubmit={handleVerificar} className="mb-4">
          <label htmlFor="admin-codigo-verificacion" className="block text-sm font-semibold text-gray-700 mb-2">
            Código de verificación
          </label>
          <input
            id="admin-codigo-verificacion"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={6}
            value={codigo}
            onChange={handleCodigoChange}
            disabled={cuentaConfirmada || verifying || !email}
            placeholder="000000"
            className="w-full text-center text-2xl font-mono tracking-[0.4em] py-3 rounded-xl border-2 border-red-300 outline-none focus:ring-2 focus:ring-red-500 text-gray-900 disabled:opacity-50"
          />
          <button
            type="submit"
            disabled={cuentaConfirmada || verifying || codigo.length !== 6 || !email}
            className="w-full mt-4 py-3 rounded-xl font-bold bg-red-600 hover:bg-red-700 text-white transition-all disabled:opacity-50 disabled:cursor-not-allowed"
          >
            {verifying ? 'Verificando...' : 'Verificar código'}
          </button>
        </form>

        <p className="mb-4 text-sm text-gray-600">
          También podés hacer clic en el enlace del correo para activar tu cuenta al instante.
        </p>

        {message && (
          <div
            className={`mb-4 p-3 rounded-lg text-sm font-semibold ${
              message.startsWith('✓') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
            }`}
          >
            {message}
          </div>
        )}

        <button
          type="button"
          onClick={handleReenviar}
          disabled={cuentaConfirmada || timeLeft > 0 || loading || !email}
          className="w-full py-3 rounded-xl font-bold border-2 border-red-600 text-red-600 hover:bg-red-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
        >
          {loading
            ? 'Reenviando...'
            : cuentaConfirmada
              ? 'Registro ya confirmado'
              : timeLeft > 0
                ? `Reenviar en ${Math.floor(timeLeft / 60)}:${String(timeLeft % 60).padStart(2, '0')}`
                : 'Reenviar correo'}
        </button>
      </div>
    </div>
  );
};

export default AdminConfirmarEmailPage;
