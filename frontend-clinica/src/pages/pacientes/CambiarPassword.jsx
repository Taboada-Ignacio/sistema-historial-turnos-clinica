import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';

const CambiarPassword = () => {
  const navigate = useNavigate();
  const [formData, setFormData] = useState({
    email: '',
    passwordActual: '',
    passwordNueva: '',
    confirmPasswordNueva: '',
  });
  const [showPasswords, setShowPasswords] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const [loading, setLoading] = useState(false);

  const handleChange = (e) => {
    setFormData((prev) => ({ ...prev, [e.target.name]: e.target.value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    setSuccess('');

    if (formData.passwordNueva !== formData.confirmPasswordNueva) {
      setError('La confirmación de la nueva contraseña no coincide.');
      return;
    }

    setLoading(true);
    try {
      const response = await clienteAxios.post('/usuarios/api/auth/cambiar-password', {
        email: formData.email,
        passwordActual: formData.passwordActual,
        passwordNueva: formData.passwordNueva,
      });

      setSuccess(response.data?.message || 'Contraseña actualizada correctamente.');
      setTimeout(() => navigate('/login'), 1800);
    } catch (err) {
      setError(err.response?.data?.message || err.response?.data?.mensaje || 'No se pudo cambiar la contraseña.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-100 flex items-center justify-center p-6">
      <div className="bg-white w-full max-w-lg rounded-2xl border border-slate-200 shadow-lg p-8">
        <h1 className="text-2xl font-black text-slate-900 mb-2">Cambiar contraseña</h1>
        <p className="text-slate-600 text-sm mb-6">
          Ingresá tu email, contraseña actual y la nueva contraseña para actualizar el acceso.
        </p>

        {error && <div className="mb-4 p-3 rounded-lg bg-red-50 text-red-700 text-sm">{error}</div>}
        {success && <div className="mb-4 p-3 rounded-lg bg-green-50 text-green-700 text-sm">{success}</div>}

        <form onSubmit={handleSubmit} className="space-y-4">
          <div>
            <label className="text-sm font-semibold text-slate-700">Email</label>
            <input
              type="email"
              name="email"
              required
              value={formData.email}
              onChange={handleChange}
              className="w-full mt-1 border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>

          <div>
            <label className="text-sm font-semibold text-slate-700">Contraseña actual</label>
            <input
              type={showPasswords ? 'text' : 'password'}
              name="passwordActual"
              required
              value={formData.passwordActual}
              onChange={handleChange}
              className="w-full mt-1 border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>

          <div>
            <label className="text-sm font-semibold text-slate-700">Nueva contraseña</label>
            <input
              type={showPasswords ? 'text' : 'password'}
              name="passwordNueva"
              required
              value={formData.passwordNueva}
              onChange={handleChange}
              className="w-full mt-1 border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>

          <div>
            <label className="text-sm font-semibold text-slate-700">Confirmar nueva contraseña</label>
            <input
              type={showPasswords ? 'text' : 'password'}
              name="confirmPasswordNueva"
              required
              value={formData.confirmPasswordNueva}
              onChange={handleChange}
              className="w-full mt-1 border border-slate-300 rounded-lg px-3 py-2"
            />
          </div>

          <label className="flex items-center text-sm text-slate-600">
            <input
              type="checkbox"
              className="mr-2"
              checked={showPasswords}
              onChange={() => setShowPasswords((prev) => !prev)}
            />
            Mostrar contraseñas
          </label>

          <button
            type="submit"
            disabled={loading}
            className="w-full bg-clinica-dark text-white py-3 rounded-lg font-bold hover:bg-clinica-hover disabled:opacity-70"
          >
            {loading ? 'Actualizando...' : 'Actualizar contraseña'}
          </button>
        </form>

        <Link to="/login" className="inline-block mt-5 text-sm text-slate-600 hover:text-slate-900">
          Volver al login
        </Link>
      </div>
    </div>
  );
};

export default CambiarPassword;
