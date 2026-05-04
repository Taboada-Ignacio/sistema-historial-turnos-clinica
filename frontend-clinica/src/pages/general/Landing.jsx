import React from 'react';
import { Link } from 'react-router-dom';

const Landing = () => {
  return (
    <div className="min-h-screen w-full bg-clinica-light font-sans text-gray-800 flex flex-col">
      
      {/* --- 1. BARRA DE NAVEGACIÓN --- */}
      <nav className="w-full bg-white shadow-sm border-b border-gray-100 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
          
          {/* Logo (Izquierda) */}
          <div className="flex items-center gap-2 text-clinica-dark shrink-0">
            <svg xmlns="http://www.w3.org/2000/svg" width="32" height="32" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.5" strokeLinecap="round" strokeLinejoin="round">
              <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
            </svg>
            <span className="text-2xl font-black tracking-tighter">Salud</span>
          </div>

          {/* Botones de Navegación (Centro) */}
          {/* Al quitar los botones de la derecha, estos links se mantendrán centrados si el logo tiene un ancho similar al espacio vacío derecho */}
          <div className="hidden md:flex items-center gap-8 text-sm font-bold text-gray-500 uppercase tracking-widest">
            <a href="#inicio" className="hover:text-clinica-dark transition-colors">Inicio</a>
            <a href="#especialidades" className="hover:text-clinica-dark transition-colors">Especialidades</a>
            <a href="#nosotros" className="hover:text-clinica-dark transition-colors">Nosotros</a>
            <a href="#contacto" className="hover:text-clinica-dark transition-colors">Contacto</a>
          </div>

          {/* 
              BOTONES BORRADOS: 
              He quitado el div que contenía los Links de 'Iniciar Sesión' y 'Registrarse'.
          */}
          <div className="md:w-[48px]"></div> {/* Espaciador invisible para mantener el equilibrio visual del centro */}

        </div>
      </nav>

      {/* --- 2. CONTENIDO PRINCIPAL --- */}
      <main className="w-full flex-grow flex flex-col items-center px-6 py-12">
        
        <div className="w-full text-center max-w-3xl mb-12">
          <h2 className="text-4xl md:text-6xl font-black text-gray-900 mb-6 leading-tight">
            Bienvenido al Portal <span className="text-clinica-dark">Salud</span>
          </h2>
          <p className="text-gray-500 text-lg font-medium">
            Seleccioná tu tipo de acceso para continuar con la gestión de tu salud.
          </p>
        </div>

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full max-w-5xl">
          
          {/* BOTÓN PROFESIONAL */}
          <Link 
            to="/login-profesional" 
            className="group bg-white p-10 md:p-16 rounded-[2.5rem] shadow-xl hover:shadow-2xl hover:ring-4 hover:ring-blue-500/20 transition-all flex flex-col items-center text-center border border-gray-100"
          >
            <div className="bg-blue-50 w-24 h-24 rounded-3xl flex items-center justify-center mb-8 group-hover:scale-110 transition-transform duration-300">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#2563eb" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                <circle cx="9" cy="7" r="4"></circle>
                <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
              </svg>
            </div>
            <h3 className="text-3xl font-black text-gray-900 mb-4 uppercase tracking-tight">Soy Profesional</h3>
            <p className="text-gray-500 text-lg leading-relaxed mb-8">
              Ingresá al panel médico, gestioná tu agenda y consultá historias clínicas.
            </p>
            <div className="bg-blue-600 text-white px-10 py-3 rounded-xl font-bold">
              Acceso Médico
            </div>
          </Link>

          {/* BOTÓN PACIENTE */}
          <Link 
            to="/login" 
            className="group bg-white p-10 md:p-16 rounded-[2.5rem] shadow-xl hover:shadow-2xl hover:ring-4 hover:ring-clinica-dark/20 transition-all flex flex-col items-center text-center border border-gray-100"
          >
            <div className="bg-clinica-light w-24 h-24 rounded-3xl flex items-center justify-center mb-8 group-hover:scale-110 transition-transform duration-300">
              <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="#15803d" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"></path>
              </svg>
            </div>
            <h3 className="text-3xl font-black text-gray-900 mb-4 uppercase tracking-tight">Soy Paciente</h3>
            <p className="text-gray-500 text-lg leading-relaxed mb-8">
              Sacá turnos, revisá tus resultados y mantené contacto con tus médicos.
            </p>
            <div className="bg-clinica-dark text-white px-10 py-3 rounded-xl font-bold">
              Acceso Paciente
            </div>
          </Link>

        </div>
      </main>

      {/* --- 3. FOOTER --- */}
      <footer className="w-full py-10 bg-white border-t border-gray-100 text-center text-gray-400 text-sm font-medium mt-auto">
        © 2026 Sistema de Gestión Clínica Salud. Desarrollado por Ignacio Taboada.
      </footer>
      
    </div>
  );
};

export default Landing;