import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import clienteAxios from '../api/axiosConfig';

const RESEND_SECONDS = 180;

/**
 * @param {object} props
 * @param {string} props.email
 * @param {'pacientes'|'profesionales'|'administradores'} props.apiRole
 * @param {string} props.successPath - ruta SPA tras confirmar por código
 * @param {'paciente'|'profesional'|'admin'} props.variant
 */
const VerificarEmailForm = ({ email, apiRole, successPath, variant }) => {
  const navigate = useNavigate();
  const [codigo, setCodigo] = useState('');
  const [timeLeft, setTimeLeft] = useState(RESEND_SECONDS);
  const [loading, setLoading] = useState(false);
  const [verifying, setVerifying] = useState(false);
  const [message, setMessage] = useState('');
  const [cuentaConfirmada, setCuentaConfirmada] = useState(false);

  const styles = {
    paciente: {
      page: 'min-h-screen bg-[#eef5ff] flex items-center justify-center p-4',
      card: 'bg-white p-10 rounded-3xl shadow-xl text-center max-w-md w-full',
      timerBox: 'bg-blue-50 border-2 border-blue-200 p-6 rounded-2xl mb-6',
      timerText: 'text-4xl font-mono font-black text-blue-600',
      input: 'border-blue-300 focus:ring-blue-500 text-blue-900',
      btnPrimary: 'bg-blue-600 hover:bg-blue-700 text-white',
      btnOutline: 'border-2 border-blue-600 text-blue-600 hover:bg-blue-50',
    },
    profesional: {
      page: 'min-h-screen bg-slate-100 flex items-center justify-center p-4',
      card: 'bg-white p-10 rounded-3xl shadow-xl text-center max-w-md w-full',
      timerBox: 'bg-blue-50 border-2 border-blue-200 p-6 rounded-2xl mb-6',
      timerText: 'text-4xl font-mono font-black text-blue-600',
      input: 'border-blue-300 focus:ring-blue-500 text-blue-900',
      btnPrimary: 'bg-blue-600 hover:bg-blue-700 text-white',
      btnOutline: 'border-2 border-blue-600 text-blue-600 hover:bg-blue-50',
    },
    admin: {
      page: 'min-h-screen bg-gray-900 flex items-center justify-center p-4',
      card: 'bg-white p-10 rounded-xl shadow-2xl text-center max-w-md w-full border-t-4 border-red-600',
      timerBox: 'bg-gray-50 border border-gray-200 p-6 rounded-2xl mb-6',
      timerText: 'text-4xl font-mono font-black text-red-600',
      input: 'border-red-300 focus:ring-red-500 text-gray-900',
      btnPrimary: 'bg-red-600 hover:bg-red-700 text-white',
      btnOutline: 'border-2 border-red-600 text-red-600 hover:bg-red-50',
    },
  }[variant] || styles.paciente;

  useEffect(() => {
    if (timeLeft > 0 && !cuentaConfirmada) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft, cuentaConfirmada]);

  const handleCodigoChange = (e) => {
    const value = e.target.value.replace(/\D/g, '').slice(0, 6);
    setCodigo(value);
  };

  const handleVerificar = async (e) => {
    e.preventDefault();
    if (cuentaConfirmada) return;

    if (!email) {
      setMessage('✗ No hay correo en sesión. Volvé al registro e intentá de nuevo.');
      return;
    }
    if (codigo.length !== 6) {
      setMessage('✗ Ingresá el código de 6 dígitos del correo.');
      return;
    }

    try {
      setVerifying(true);
      setMessage('');
      await clienteAxios.post(`/usuarios/api/${apiRole}/confirmar-codigo`, { email, codigo });
      navigate(successPath, { replace: true });
    } catch (error) {
      const backendMessage = error?.response?.data?.mensaje || '';
      if (backendMessage.toLowerCase().includes('ya se encuentra activa')) {
        setCuentaConfirmada(true);
        setTimeLeft(0);
        setMessage('✓ Este registro ya fue confirmado.');
      } else if (backendMessage.toLowerCase().includes('expir')) {
        setMessage('✗ El código expiró (3 minutos). Pedí un nuevo correo.');
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
      setMessage('✗ No hay correo en sesión. Volvé al registro e intentá de nuevo.');
      return;
    }
    try {
      setLoading(true);
      setMessage('');
      await clienteAxios.post(
        `/usuarios/api/${apiRole}/reenviar-confirmacion?email=${encodeURIComponent(email)}`
      );
      setTimeLeft(RESEND_SECONDS);
      setCodigo('');
      setMessage('✓ Código y enlace reenviados. Revisá tu bandeja de entrada.');
    } catch (error) {
      const backendMessage = error?.response?.data?.mensaje || '';
      if (backendMessage.toLowerCase().includes('ya se encuentra activa')) {
        setCuentaConfirmada(true);
        setTimeLeft(0);
        setMessage('✓ Este registro ya fue confirmado. Ya no es necesario verificarlo nuevamente.');
      } else {
        setMessage('✗ Error al reenviar el correo.');
      }
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className={styles.page}>
      <div className={styles.card}>
        <div className="mb-6">
          <div className="text-5xl mb-4">✉️</div>
          <h2 className="text-2xl font-black mb-2">Verificá tu correo</h2>
          <p className="text-gray-500 text-sm">
            Enviamos un código de 6 dígitos y un enlace a <b>{email || 'tu email'}</b>
          </p>
        </div>

        {!cuentaConfirmada && (
          <div className={styles.timerBox}>
            <p className="text-sm text-gray-600 mb-3">Válido por 3 min · reenviar en:</p>
            <span className={styles.timerText}>
              {Math.floor(timeLeft / 60)}:{String(timeLeft % 60).padStart(2, '0')}
            </span>
          </div>
        )}

        <form onSubmit={handleVerificar} className="mb-4">
          <label htmlFor="codigo-verificacion" className="block text-sm font-semibold text-gray-700 mb-2">
            Código de verificación
          </label>
          <input
            id="codigo-verificacion"
            type="text"
            inputMode="numeric"
            autoComplete="one-time-code"
            maxLength={6}
            value={codigo}
            onChange={handleCodigoChange}
            disabled={cuentaConfirmada || verifying}
            placeholder="000000"
            className={`w-full text-center text-2xl font-mono tracking-[0.4em] py-3 rounded-xl border-2 outline-none focus:ring-2 ${styles.input} disabled:opacity-50`}
          />
          <button
            type="submit"
            disabled={cuentaConfirmada || verifying || codigo.length !== 6}
            className={`w-full mt-4 py-3 rounded-xl font-bold transition-all disabled:opacity-50 disabled:cursor-not-allowed ${styles.btnPrimary}`}
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
          disabled={cuentaConfirmada || timeLeft > 0 || loading}
          className={`w-full py-3 rounded-xl font-bold disabled:opacity-50 disabled:cursor-not-allowed transition-all ${styles.btnOutline}`}
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

export default VerificarEmailForm;
