import React from 'react';
import { useNavigate } from 'react-router-dom';

const Dashboard = () => {
  const navigate = useNavigate();

  // Función para cerrar sesión
  const handleLogout = () => {
    localStorage.removeItem('token'); // Borramos el JWT
    navigate('/'); // Volvemos al login
  };

  return (
    <div className="min-h-screen bg-gray-50 font-sans">
      
      {/* Barra de Navegación Superior */}
      <nav className="bg-clinica-dark text-white shadow-md">
        <div className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8">
          <div className="flex justify-between h-16 items-center">
            {/* Logo y Nombre */}
            <div className="flex items-center gap-2">
              <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
              </svg>
              <span className="text-xl font-bold tracking-wider">Salud</span>
            </div>
            
            {/* Botón de Cerrar Sesión */}
            <button 
              onClick={handleLogout}
              className="flex items-center gap-2 hover:bg-clinica-hover px-4 py-2 rounded-lg transition-colors text-sm font-medium cursor-pointer"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M9 21H5a2 2 0 0 1-2-2V5a2 2 0 0 1 2-2h4"></path>
                <polyline points="16 17 21 12 16 7"></polyline>
                <line x1="21" y1="12" x2="9" y2="12"></line>
              </svg>
              Cerrar Sesión
            </button>
          </div>
        </div>
      </nav>

      {/* Contenido Principal */}
      <main className="max-w-7xl mx-auto px-4 sm:px-6 lg:px-8 py-10">
        
        {/* Mensaje de Bienvenida */}
        <div className="mb-10">
          <h1 className="text-3xl font-bold text-gray-800">Bienvenido al Portal Clínico</h1>
          <p className="text-gray-500 mt-2">Selecciona un módulo para comenzar a trabajar.</p>
        </div>

        {/* Grilla de Tarjetas (Accesos a tus Microservicios) */}
        <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
          
          {/* Tarjeta: Gestión de Usuarios / Pacientes */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 hover:shadow-lg transition-shadow cursor-pointer group">
            <div className="bg-clinica-light w-14 h-14 rounded-xl flex items-center justify-center text-clinica-dark mb-4 group-hover:scale-110 transition-transform">
              <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">Pacientes y Staff</h2>
            <p className="text-gray-500 text-sm">Gestionar altas, bajas y modificaciones de usuarios en el sistema.</p>
          </div>

          {/* Tarjeta: Gestión de Turnos */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 hover:shadow-lg transition-shadow cursor-pointer group">
            <div className="bg-clinica-light w-14 h-14 rounded-xl flex items-center justify-center text-clinica-dark mb-4 group-hover:scale-110 transition-transform">
              <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2"></rect>
                <line x1="16" y1="2" x2="16" y2="6"></line>
                <line x1="8" y1="2" x2="8" y2="6"></line>
                <line x1="3" y1="10" x2="21" y2="10"></line>
                <path d="M8 14h.01"></path>
                <path d="M12 14h.01"></path>
                <path d="M16 14h.01"></path>
                <path d="M8 18h.01"></path>
                <path d="M12 18h.01"></path>
                <path d="M16 18h.01"></path>
              </svg>
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">Agenda de Turnos</h2>
            <p className="text-gray-500 text-sm">Administrar la agenda médica, programar y cancelar citas.</p>
          </div>

          {/* Tarjeta: Historial Clínico */}
          <div className="bg-white rounded-2xl shadow-sm border border-gray-100 p-6 hover:shadow-lg transition-shadow cursor-pointer group">
            <div className="bg-clinica-light w-14 h-14 rounded-xl flex items-center justify-center text-clinica-dark mb-4 group-hover:scale-110 transition-transform">
              <svg xmlns="http://www.w3.org/2000/svg" width="28" height="28" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z"></path>
                <polyline points="14 2 14 8 20 8"></polyline>
                <line x1="16" y1="13" x2="8" y2="13"></line>
                <line x1="16" y1="17" x2="8" y2="17"></line>
                <polyline points="10 9 9 9 8 9"></polyline>
              </svg>
            </div>
            <h2 className="text-xl font-bold text-gray-800 mb-2">Historiales Clínicos</h2>
            <p className="text-gray-500 text-sm">Consultar y actualizar las evoluciones médicas de los pacientes.</p>
          </div>

        </div>
      </main>
    </div>
  );
};

export default Dashboard;