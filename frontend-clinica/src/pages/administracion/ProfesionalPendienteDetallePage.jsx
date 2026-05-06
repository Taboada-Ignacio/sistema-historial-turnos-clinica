import React, { useEffect, useState } from 'react';
import { Link, useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { ADMIN_PATHS } from '../../utils/adminPaths';

const ProfesionalPendienteDetallePage = () => {
  const { idProfesional } = useParams();
  const navigate = useNavigate();
  const [profesional, setProfesional] = useState(null);
  const [loading, setLoading] = useState(true);
  const [saving, setSaving] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');

  useEffect(() => {
    const fetchProfesional = async () => {
      try {
        const response = await clienteAxios.get(`/usuarios/api/profesionales/${idProfesional}`);
        setProfesional(response.data);
      } catch (err) {
        setError(err.response?.data?.message || 'No se pudo cargar el detalle del profesional.');
      } finally {
        setLoading(false);
      }
    };

    fetchProfesional();
  }, [idProfesional]);

  const handleVerificar = async () => {
    setSaving(true);
    setError('');
    setSuccess('');

    try {
      await clienteAxios.put(`/usuarios/api/profesionales/${idProfesional}/verificar-matricula`);
      setSuccess('Membresía actualizada correctamente a INACTIVA.');
      setProfesional((prev) => ({ ...prev, membresiaActual: 'INACTIVA' }));
    } catch (err) {
      setError(err.response?.data?.message || 'No se pudo verificar la matrícula.');
    } finally {
      setSaving(false);
    }
  };

  if (loading) {
    return <div className="bg-white rounded-xl border border-slate-200 p-6">Cargando detalle...</div>;
  }

  if (!profesional) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <p className="text-red-600">{error || 'Profesional no encontrado.'}</p>
        <Link to={ADMIN_PATHS.profesionalesPendientes} className="text-slate-700 underline">
          Volver al listado
        </Link>
      </div>
    );
  }

  const fotoUrl = profesional.fotoPerfil ? `http://localhost:8080${profesional.fotoPerfil}` : null;
  const isSinVerificar = profesional.membresiaActual === 'SIN_VERIFICAR';

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <button
        type="button"
        onClick={() => navigate(ADMIN_PATHS.profesionalesPendientes)}
        className="text-sm text-slate-600 hover:text-slate-900 mb-4"
      >
        ← Volver al listado
      </button>

      <div className="grid grid-cols-1 md:grid-cols-[220px_1fr] gap-6">
        <div>
          {fotoUrl ? (
            <img
              src={fotoUrl}
              alt={`Perfil de ${profesional.nombre} ${profesional.apellido}`}
              className="w-[220px] h-[220px] object-cover rounded-xl border border-slate-200"
            />
          ) : (
            <div className="w-[220px] h-[220px] rounded-xl border border-slate-200 bg-slate-100 flex items-center justify-center text-slate-500 text-sm">
              Sin foto de perfil
            </div>
          )}
        </div>

        <div>
          <h2 className="text-2xl font-black text-slate-900 mb-2">
            {profesional.nombre} {profesional.apellido}
          </h2>
          <div className="grid grid-cols-1 md:grid-cols-2 gap-3 text-sm mb-6">
            <p><span className="font-semibold">Email:</span> {profesional.email}</p>
            <p><span className="font-semibold">DNI:</span> {profesional.dni}</p>
            <p><span className="font-semibold">Teléfono:</span> {profesional.telefono}</p>
            <p><span className="font-semibold">Matrícula:</span> {profesional.matricula}</p>
            <p><span className="font-semibold">Especialidad:</span> {profesional.especialidad}</p>
            <p><span className="font-semibold">Membresía actual:</span> {profesional.membresiaActual}</p>
          </div>

          {error && <p className="text-red-600 mb-4">{error}</p>}
          {success && <p className="text-green-700 mb-4">{success}</p>}

          <button
            type="button"
            disabled={!isSinVerificar || saving}
            onClick={handleVerificar}
            className="bg-emerald-700 text-white px-5 py-2.5 rounded-lg font-semibold hover:bg-emerald-600 disabled:opacity-60"
          >
            {saving ? 'Verificando...' : 'Verificar y cambiar a INACTIVA'}
          </button>
        </div>
      </div>
    </section>
  );
};

export default ProfesionalPendienteDetallePage;
