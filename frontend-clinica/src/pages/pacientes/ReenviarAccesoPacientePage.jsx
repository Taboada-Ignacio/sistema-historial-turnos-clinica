import React, { useRef, useState } from 'react';
import { Link } from 'react-router-dom';
import { clienteAxiosPublic } from '../../api/axiosConfig';
import { PACIENTE_PATHS } from '../../utils/portalPaths';

const ReenviarAccesoPacientePage = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const submittingRef = useRef(false);

  const handleSubmit = async (e) => {
    e.preventDefault();
    if (loading || submittingRef.current) return;
    setError('');
    setSuccess('');
    submittingRef.current = true;
    setLoading(true);
    try {
      const { data } = await clienteAxiosPublic.post('/usuarios/api/auth/reenviar-acceso-paciente', { email });
      setSuccess(data?.message || 'Si el correo está registrado, te enviamos un nuevo correo.');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.mensaje || 'No se pudo procesar la solicitud.');
    } finally {
      setLoading(false);
      submittingRef.current = false;
    }
  };

  return (
    <div className="min-h-screen bg-emerald-50 flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white border border-emerald-200 rounded-2xl shadow-lg p-8">
        <h1 className="text-2xl font-black text-emerald-800 mb-2">Reenviar correo</h1>
        <p className="text-sm text-emerald-700 mb-6">
          Si tu cuenta está pendiente de confirmación o de activación (cargada por un profesional), te enviaremos
          el correo correspondiente. El enlace es válido por 72 horas.
        </p>

        {error && <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{error}</div>}
        {success && <div className="mb-4 p-3 rounded-lg bg-green-50 text-green-700 text-sm">{success}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-sm font-semibold text-gray-700">Email</label>
            <input
              type="email"
              required
              value={email}
              onChange={(e) => setEmail(e.target.value)}
              className="w-full mt-1 px-3 py-2 rounded-lg border border-gray-300"
            />
          </div>
          <button
            type="submit"
            disabled={loading}
            className="w-full bg-emerald-700 text-white py-2.5 rounded-lg font-bold hover:bg-emerald-600 disabled:opacity-70"
          >
            {loading ? 'Enviando…' : 'Enviar correo'}
          </button>
        </form>

        <Link to={PACIENTE_PATHS.login} className="inline-block mt-4 text-sm text-gray-600 hover:text-gray-900">
          Volver al login
        </Link>
      </div>
    </div>
  );
};

export default ReenviarAccesoPacientePage;
