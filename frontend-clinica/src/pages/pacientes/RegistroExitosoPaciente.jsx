import React, { useEffect } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import { PACIENTE_PATHS } from '../../utils/portalPaths';

/**
 * Pantalla tras confirmar el email (redirect del backend). Redirige al dashboard;
 * si no hay sesión, PacienteRoute envía al login.
 */
const RegistroExitosoPaciente = () => {
  const navigate = useNavigate();

  useEffect(() => {
    const t = setTimeout(() => navigate(PACIENTE_PATHS.dashboard, { replace: true }), 4500);
    return () => clearTimeout(t);
  }, [navigate]);

  return (
    <div className="min-h-screen bg-clinica-light flex items-center justify-center p-4 font-sans">
      <div className="bg-white p-10 rounded-3xl shadow-2xl max-w-md w-full text-center border border-gray-100">
        <div className="text-5xl mb-4">✓</div>
        <h1 className="text-2xl font-bold text-clinica-dark mb-2">¡Cuenta activada!</h1>
        <p className="text-gray-600 mb-6">
          Tu correo fue verificado. Te llevamos al panel del paciente; si aún no iniciaste sesión, te pediremos que ingreses.
        </p>
        <Link
          to={PACIENTE_PATHS.dashboard}
          className="inline-block w-full py-3 rounded-xl bg-clinica-dark text-white font-semibold hover:bg-clinica-hover transition-all"
        >
          Ir al panel
        </Link>
        <p className="text-xs text-gray-400 mt-4">Redirección automática en unos segundos…</p>
      </div>
    </div>
  );
};

export default RegistroExitosoPaciente;
