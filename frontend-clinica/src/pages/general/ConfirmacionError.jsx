import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ADMIN_PATHS, HOME_PATH, PACIENTE_PATHS, PROFESIONAL_PATHS } from '../../utils/portalPaths';

const textos = {
  invalido: 'El enlace no es válido o ya fue utilizado.',
  expirado: 'El enlace expiró. Podés pedir un nuevo correo desde la pantalla de verificación.',
};

const ConfirmacionError = () => {
  const [params] = useSearchParams();
  const tipo = params.get('tipo') || 'paciente';
  const motivo = params.get('motivo') || 'invalido';
  const mensaje = textos[motivo] || textos.invalido;

  let inicio = HOME_PATH;
  let login = PACIENTE_PATHS.login;
  if (tipo === 'profesional') {
    login = PROFESIONAL_PATHS.login;
  } else if (tipo === 'admin') {
    login = ADMIN_PATHS.login;
    inicio = ADMIN_PATHS.setup;
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <div className="bg-white p-8 rounded-2xl shadow-lg max-w-md w-full text-center">
        <div className="text-4xl mb-3">⚠️</div>
        <h1 className="text-xl font-bold text-gray-900 mb-2">No pudimos confirmar la cuenta</h1>
        <p className="text-gray-600 text-sm mb-6">{mensaje}</p>
        <div className="flex flex-col gap-2">
          <Link to={login} className="py-2 rounded-lg bg-gray-900 text-white font-semibold text-sm">
            Ir al inicio de sesión
          </Link>
          <Link to={inicio} className="py-2 rounded-lg border border-gray-300 text-gray-700 text-sm">
            Volver al inicio
          </Link>
        </div>
      </div>
    </div>
  );
};

export default ConfirmacionError;
