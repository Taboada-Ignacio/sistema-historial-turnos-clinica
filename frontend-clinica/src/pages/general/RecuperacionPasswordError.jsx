import React from 'react';
import { Link, useSearchParams } from 'react-router-dom';
import { ADMIN_PATHS, HOME_PATH, PACIENTE_PATHS, PROFESIONAL_PATHS } from '../../utils/portalPaths';

const textos = {
  invalido: 'Este enlace ya fue utilizado o no es válido. Si necesitás cambiar la contraseña, solicitá un nuevo correo.',
  expirado: 'El enlace expiró (válido por 30 minutos). Solicitá un nuevo correo desde la pantalla de recuperación.',
};

const RecuperacionPasswordError = () => {
  const [params] = useSearchParams();
  const tipo = params.get('tipo') || 'paciente';
  const motivo = params.get('motivo') || 'invalido';
  const mensaje = textos[motivo] || textos.invalido;

  let login = PACIENTE_PATHS.login;
  let recuperar = PACIENTE_PATHS.recuperarPassword;
  let inicio = HOME_PATH;
  let tituloPortal = 'paciente';

  if (tipo === 'profesional') {
    login = PROFESIONAL_PATHS.login;
    recuperar = PROFESIONAL_PATHS.recuperarPassword;
    tituloPortal = 'profesional';
  } else if (tipo === 'admin') {
    login = ADMIN_PATHS.login;
    recuperar = ADMIN_PATHS.recuperarPassword;
    inicio = ADMIN_PATHS.setup;
    tituloPortal = 'administrador';
  }

  return (
    <div className="min-h-screen bg-gray-100 flex items-center justify-center p-4">
      <div className="bg-white p-8 rounded-2xl shadow-lg max-w-md w-full text-center">
        <div className="text-4xl mb-3">⚠️</div>
        <h1 className="text-xl font-bold text-gray-900 mb-2">No pudimos continuar con el cambio</h1>
        <p className="text-gray-500 text-xs mb-2 uppercase tracking-wide">Portal {tituloPortal}</p>
        <p className="text-gray-600 text-sm mb-6">{mensaje}</p>
        <div className="flex flex-col gap-2">
          <Link to={recuperar} className="py-2 rounded-lg bg-gray-900 text-white font-semibold text-sm">
            Solicitar nuevo correo
          </Link>
          <Link to={login} className="py-2 rounded-lg border border-gray-300 text-gray-700 text-sm">
            Ir al inicio de sesión
          </Link>
          <Link to={inicio} className="py-2 text-gray-500 text-sm hover:text-gray-800">
            Volver al inicio
          </Link>
        </div>
      </div>
    </div>
  );
};

export default RecuperacionPasswordError;
