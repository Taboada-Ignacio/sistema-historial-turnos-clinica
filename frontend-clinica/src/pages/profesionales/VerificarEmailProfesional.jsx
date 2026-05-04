import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';

const VerificarEmailProfesional = () => {
  const location = useLocation();
  const email = location.state?.email || "";
  const [timeLeft, setTimeLeft] = useState(180);

  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft]);

  const handleReenviar = async () => {
    try {
      await clienteAxios.post(`/usuarios/api/profesionales/reenviar-confirmacion?email=${email}`);
      setTimeLeft(180);
      alert("Enlace reenviado con éxito.");
    } catch (error) { alert("Error al reenviar."); }
  };

  return (
    <div className="min-h-screen bg-[#eef5ff] flex items-center justify-center">
      <div className="bg-white p-10 rounded-3xl shadow-xl text-center max-w-md">
        <h2 className="text-2xl font-black mb-4">Verificá tu correo</h2>
        <p className="mb-6 text-gray-500">Enviamos un enlace a <b>{email}</b></p>
        <div className="bg-gray-50 p-6 rounded-2xl mb-6">
          <span className="text-4xl font-mono font-black text-blue-600">
            {Math.floor(timeLeft / 60)}:{String(timeLeft % 60).padStart(2, '0')}
          </span>
        </div>
        <button 
          onClick={handleReenviar} 
          disabled={timeLeft > 0}
          className="w-full py-4 rounded-xl font-bold border-2 disabled:opacity-50"
        >
          Reenviar correo
        </button>
      </div>
    </div>
  );
};

export default VerificarEmailProfesional;