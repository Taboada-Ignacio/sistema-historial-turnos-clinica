import React from 'react';
import { Link } from 'react-router-dom';

const AprobacionPendiente = () => (
  <div className="min-h-screen bg-blue-600 flex items-center justify-center text-white text-center p-6">
    <div className="max-w-md">
      <div className="text-6xl mb-6">🛡️</div>
      <h1 className="text-4xl font-black mb-4">Email Confirmado</h1>
      <p className="text-lg mb-8 opacity-90">
        Tu cuenta está activa, pero tus credenciales médicas están siendo revisadas por Administración. 
        Te avisaremos por correo cuando puedas acceder al sistema.
      </p>
      <Link to="/" className="bg-white text-blue-600 px-8 py-4 rounded-2xl font-black shadow-lg">
        Volver al Inicio
      </Link>
    </div>
  </div>
);

export default AprobacionPendiente;