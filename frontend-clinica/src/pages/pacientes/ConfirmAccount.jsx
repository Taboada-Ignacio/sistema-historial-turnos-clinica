import React, { useEffect, useState } from 'react';
import { useSearchParams, Link } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';

const ConfirmAccount = () => {
  const [searchParams] = useSearchParams();
  const [status, setStatus] = useState('procesando'); // procesando, exito, error
  const [serverMessage, setServerMessage] = useState('');
  const token = searchParams.get('token');

  useEffect(() => {
    const confirmar = async () => {
      // 1. Verificación básica del token en la URL
      if (!token) {
        setStatus('error');
        setServerMessage('No se proporcionó un token de seguridad válido.');
        return;
      }

      try {
        // 2. Llamada al endpoint de confirmación (vía Gateway)
        // Nota: Asegurate que este endpoint coincida con tu AuthController o AdministradorController
        const response = await clienteAxios.get(`/usuarios/api/auth/confirmar`, {
          params: { token: token }
        });

        console.log('Confirmación exitosa:', response.data);
        setStatus('exito');
      } catch (err) {
        console.error('Error al confirmar cuenta:', err);
        setStatus('error');
        // Capturamos el mensaje específico del backend (ej: "Token expirado")
        setServerMessage(err.response?.data?.message || 'El token es inválido o ha expirado.');
      }
    };

    confirmar();
  }, [token]);

  return (
    <div className="min-h-screen bg-clinica-light flex items-center justify-center p-4 font-sans">
      <div className="bg-white p-10 rounded-3xl shadow-2xl max-w-md w-full text-center border border-gray-100">
        
        {/* --- ESTADO: PROCESANDO --- */}
        {status === 'procesando' && (
          <div className="space-y-4">
            <div className="flex justify-center">
              <div className="animate-spin rounded-full h-12 w-12 border-b-2 border-clinica-dark"></div>
            </div>
            <p className="text-clinica-dark font-bold text-lg">Verificando tu cuenta...</p>
            <p className="text-gray-500 text-sm">Esto tomará solo un momento.</p>
          </div>
        )}
        
        {/* --- ESTADO: ÉXITO --- */}
        {status === 'exito' && (
          <div className="animate-fade-in">
            <div className="bg-green-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-10 h-10 text-green-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M5 13l4 4L19 7" />
              </svg>
            </div>
            <h2 className="text-3xl font-bold text-gray-800 mb-4">¡Cuenta Activada!</h2>
            <p className="text-gray-600 mb-8 leading-relaxed">
              Tu correo ha sido validado correctamente. Ya puedes acceder a todas las funciones del portal médico.
            </p>
            <Link 
              to="/" 
              className="block w-full bg-clinica-dark text-white font-bold px-6 py-3 rounded-xl hover:bg-clinica-hover transition-all shadow-lg shadow-clinica-dark/30"
            >
              Iniciar Sesión
            </Link>
          </div>
        )}

        {/* --- ESTADO: ERROR --- */}
        {status === 'error' && (
          <div className="animate-fade-in">
            <div className="bg-red-100 w-20 h-20 rounded-full flex items-center justify-center mx-auto mb-6">
              <svg className="w-10 h-10 text-red-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="3" d="M6 18L18 6M6 6l12 12" />
              </svg>
            </div>
            <h2 className="text-2xl font-bold text-gray-800 mb-3">Error de Activación</h2>
            <p className="text-red-500 font-medium mb-4">{serverMessage}</p>
            <p className="text-gray-500 mb-8 text-sm">
              Si el problema persiste, solicita un nuevo enlace de registro o contacta a soporte técnico.
            </p>
            <Link 
              to="/" 
              className="text-clinica-dark font-bold hover:underline transition-all"
            >
              Volver al inicio
            </Link>
          </div>
        )}
        
      </div>
    </div>
  );
};

export default ConfirmAccount;