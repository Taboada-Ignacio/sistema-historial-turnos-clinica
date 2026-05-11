import React, { useState, useEffect } from 'react';
import { useLocation } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';

const VerificarEmailAdmin = () => {
  const location = useLocation();
  const email = location.state?.email || '';
  const [timeLeft, setTimeLeft] = useState(180);
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState('');

  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft]);

  const handleReenviar = async () => {
    if (!email) {
      setMessage('✗ No hay correo en sesión. Volvé al registro e intentá de nuevo.');
      return;
    }
    try {
      setLoading(true);
      setMessage('');
      await clienteAxios.post(`/usuarios/api/administradores/reenviar-confirmacion?email=${encodeURIComponent(email)}`);
      setTimeLeft(180);
      setMessage('✓ Enlace reenviado. Revisá tu bandeja de entrada.');
    } catch {
      setMessage('✗ Error al reenviar el correo.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4">
      <div className="bg-white p-10 rounded-xl shadow-2xl text-center max-w-md border-t-4 border-red-600">
        <div className="mb-6">
          <div className="text-5xl mb-4">✉️</div>
          <h2 className="text-2xl font-black mb-2 uppercase tracking-tight">Verificá tu correo</h2>
          <p className="text-gray-500 text-sm">Enviamos un enlace a <b>{email || 'tu email'}</b></p>
        </div>

        <div className="bg-gray-50 border border-gray-200 p-6 rounded-2xl mb-6">
          <p className="text-sm text-gray-600 mb-3">Tiempo para reenviar:</p>
          <span className="text-4xl font-mono font-black text-red-600">
            {Math.floor(timeLeft / 60)}:{String(timeLeft % 60).padStart(2, '0')}
          </span>
        </div>

        <p className="mb-6 text-sm text-gray-600">
          Activá tu cuenta desde el enlace del correo. Luego podés iniciar sesión en el panel de administración.
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
          disabled={timeLeft > 0 || loading}
          className="w-full py-3 rounded-lg font-bold border-2 border-red-600 text-red-600 hover:bg-red-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all uppercase text-sm"
        >
          {loading ? 'Reenviando...' : timeLeft > 0
            ? `Reenviar en ${Math.floor(timeLeft / 60)}:${String(timeLeft % 60).padStart(2, '0')}`
            : 'Reenviar correo'}
        </button>
      </div>
    </div>
  );
};

export default VerificarEmailAdmin;
