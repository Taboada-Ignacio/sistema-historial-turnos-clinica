import React, { useState, useEffect } from 'react';
import { useLocation, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';

const VerificarEmailProfesional = () => {
  const location = useLocation();
  const navigate = useNavigate();
  const email = location.state?.email || "";
  const [timeLeft, setTimeLeft] = useState(180); // 3 minutos = 180 segundos
  const [loading, setLoading] = useState(false);
  const [message, setMessage] = useState("");

  useEffect(() => {
    if (timeLeft > 0) {
      const timer = setTimeout(() => setTimeLeft(timeLeft - 1), 1000);
      return () => clearTimeout(timer);
    }
  }, [timeLeft]);

  const handleReenviar = async () => {
    try {
      setLoading(true);
      setMessage("");
      await clienteAxios.post(`/usuarios/api/profesionales/reenviar-confirmacion?email=${email}`);
      setTimeLeft(180); // Reinicia el contador a 3 minutos
      setMessage("✓ Enlace reenviado con éxito. Revisa tu bandeja de entrada.");
    } catch (error) { 
      setMessage("✗ Error al reenviar el email."); 
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#eef5ff] flex items-center justify-center p-4">
      <div className="bg-white p-10 rounded-3xl shadow-xl text-center max-w-md">
        <div className="mb-6">
          <div className="text-5xl mb-4">✉️</div>
          <h2 className="text-2xl font-black mb-2">Verificá tu correo</h2>
          <p className="text-gray-500">Enviamos un enlace a <b>{email}</b></p>
        </div>
        
        <div className="bg-blue-50 border-2 border-blue-200 p-6 rounded-2xl mb-6">
          <p className="text-sm text-gray-600 mb-3">Tiempo para reenviar:</p>
          <span className="text-4xl font-mono font-black text-blue-600">
            {Math.floor(timeLeft / 60)}:{String(timeLeft % 60).padStart(2, '0')}
          </span>
        </div>

        <div className="mb-6 text-sm text-gray-600">
          <p>Una vez confirmado, tu solicitud será revisada por nuestro equipo de Administración.</p>
          <p className="mt-2 text-xs text-gray-500">Te avisaremos por correo cuando esté aprobado.</p>
        </div>

        {message && (
          <div className={`mb-4 p-3 rounded-lg text-sm font-semibold ${
            message.startsWith('✓') ? 'bg-green-100 text-green-700' : 'bg-red-100 text-red-700'
          }`}>
            {message}
          </div>
        )}

        <button 
          onClick={handleReenviar} 
          disabled={timeLeft > 0 || loading}
          className="w-full py-3 rounded-xl font-bold border-2 border-blue-600 text-blue-600 hover:bg-blue-50 disabled:opacity-50 disabled:cursor-not-allowed transition-all"
        >
          {loading ? "Reenviando..." : timeLeft > 0 ? `Reenviar en ${Math.floor(timeLeft / 60)}:${String(timeLeft % 60).padStart(2, '0')}` : "Reenviar correo"}
        </button>
      </div>
    </div>
  );
};

export default VerificarEmailProfesional;