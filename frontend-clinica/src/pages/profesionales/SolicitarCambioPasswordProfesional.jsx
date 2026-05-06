import React, { useState } from 'react';
import { Link } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';

const SolicitarCambioPasswordProfesional = () => {
  const [email, setEmail] = useState('');
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');
    setLoading(true);
    try {
      const response = await clienteAxios.post('/usuarios/api/auth/solicitar-cambio-password/profesional', { email });
      setSuccess(response.data?.message || 'Revisá tu correo para continuar.');
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.mensaje || 'No se pudo procesar la solicitud.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-blue-50 flex items-center justify-center p-6">
      <div className="w-full max-w-md bg-white border border-blue-200 rounded-2xl shadow-lg p-8">
        <h1 className="text-2xl font-black text-blue-800 mb-2">Recuperar acceso profesional</h1>
        <p className="text-sm text-blue-700 mb-6">Te enviaremos un correo de confirmación para continuar.</p>

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
            className="w-full bg-blue-700 text-white py-2.5 rounded-lg font-bold hover:bg-blue-600 disabled:opacity-70"
          >
            {loading ? 'Enviando...' : 'Enviar correo de confirmación'}
          </button>
        </form>

        <Link to="/login-profesional" className="inline-block mt-4 text-sm text-gray-600 hover:text-gray-900">
          Volver al login profesional
        </Link>
      </div>
    </div>
  );
};

export default SolicitarCambioPasswordProfesional;
