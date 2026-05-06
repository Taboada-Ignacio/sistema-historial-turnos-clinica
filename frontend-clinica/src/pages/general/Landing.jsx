import React, { useEffect } from 'react';
import { Link } from 'react-router-dom'; // Solo necesitamos Link
import { 
  hasActiveSession, 
  getSessionPortal, 
  clearSession 
} from '../../utils/auth';
import { PACIENTE_PATHS, PROFESIONAL_PATHS } from '../../utils/portalPaths';

const VALID_PORTALS = ['paciente', 'profesional', 'admin'];

const Landing = () => {
  useEffect(() => {
    // Solo mantenemos la lógica para limpiar sesiones corruptas o "huérfanas"
    if (hasActiveSession()) {
      const portal = getSessionPortal();
      if (!VALID_PORTALS.includes(portal)) {
        clearSession();
      }
    }
  }, []);

  return (
    <div className="min-h-screen w-full bg-slate-50 font-sans text-slate-800 flex flex-col">
      
      {/* Navbar */}
      <nav className="w-full bg-white/90 backdrop-blur border-b border-slate-200 sticky top-0 z-50">
        <div className="max-w-7xl mx-auto px-6 h-20 flex items-center justify-between">
          <div className="flex items-center gap-3 text-clinica-dark">
            <div className="w-10 h-10 rounded-xl bg-clinica-light flex items-center justify-center">
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2.3" strokeLinecap="round" strokeLinejoin="round">
                <path d="M22 12h-4l-3 9L9 3l-3 9H2"></path>
              </svg>
            </div>
            <div>
              <p className="text-lg font-black leading-none tracking-tight">Portal Salud</p>
              <p className="text-xs text-slate-500">Gestión Clínica Integral</p>
            </div>
          </div>
          <div className="hidden md:flex items-center gap-7 text-sm font-semibold text-slate-500">
            <a href="#inicio" className="hover:text-clinica-dark transition-colors">Inicio</a>
            <a href="#accesos" className="hover:text-clinica-dark transition-colors">Accesos</a>
            <a href="#beneficios" className="hover:text-clinica-dark transition-colors">Beneficios</a>
            <a href="#nosotros" className="hover:text-clinica-dark transition-colors">Nosotros</a>
          </div>
        </div>
      </nav>

      <main id="inicio" className="w-full flex-grow">
        
        {/* Hero Section */}
        <section className="max-w-7xl mx-auto px-6 pt-14 pb-10 grid grid-cols-1 lg:grid-cols-2 gap-10 items-center">
          <div>
            <p className="inline-flex items-center gap-2 px-3 py-1 rounded-full bg-emerald-50 text-emerald-700 text-xs font-bold uppercase tracking-wider mb-5">
              Plataforma clínica segura
            </p>
            <h1 className="text-4xl md:text-5xl font-black text-slate-900 leading-tight mb-5">
              Gestión de salud profesional, simple y confiable
            </h1>
            <p className="text-slate-600 text-lg leading-relaxed mb-8 max-w-xl">
              Unificá acceso de pacientes y profesionales en una sola plataforma para turnos, seguimiento y administración del historial clínico.
            </p>
            <div className="flex flex-wrap gap-3">
              <a href="#accesos" className="px-6 py-3 rounded-xl bg-clinica-dark text-white font-bold hover:bg-clinica-hover transition-colors shadow-lg shadow-slate-900/20">
                Ir a accesos
              </a>
              <a href="#beneficios" className="px-6 py-3 rounded-xl border border-slate-300 text-slate-700 font-semibold hover:border-slate-400 transition-colors">
                Ver beneficios
              </a>
            </div>
          </div>

          {/* Platform Status Widget */}
          <div className="bg-white border border-slate-200 rounded-3xl p-7 shadow-sm">
            <h2 className="text-lg font-black text-slate-900 mb-5">Estado de la plataforma</h2>
            <div className="grid grid-cols-1 sm:grid-cols-3 gap-4">
              <article className="rounded-2xl bg-slate-50 border border-slate-200 p-4">
                <p className="text-2xl font-black text-slate-900">24/7</p>
                <p className="text-sm text-slate-500">Disponibilidad</p>
              </article>
              <article className="rounded-2xl bg-slate-50 border border-slate-200 p-4">
                <p className="text-2xl font-black text-slate-900">100%</p>
                <p className="text-sm text-slate-500">Trazabilidad</p>
              </article>
              <article className="rounded-2xl bg-slate-50 border border-slate-200 p-4">
                <p className="text-2xl font-black text-slate-900 text-emerald-600">Seguro</p>
                <p className="text-sm text-slate-500">Control de acceso</p>
              </article>
            </div>
            <div className="mt-5 rounded-2xl border border-blue-100 bg-blue-50 p-4">
              <p className="text-sm text-blue-900 font-semibold">
                Entorno diseñado para simplificar la operación clínica y mejorar la experiencia de atención.
              </p>
            </div>
          </div>
        </section>

        {/* Portal Access Grid */}
        <section id="accesos" className="max-w-7xl mx-auto px-6 py-10">
          <div className="mb-8 text-center lg:text-left">
            <h2 className="text-3xl font-black text-slate-900">Accesos al sistema</h2>
            <p className="text-slate-600 mt-2">Seleccioná el perfil correspondiente para ingresar.</p>
          </div>
          
          <div className="grid grid-cols-1 md:grid-cols-2 gap-8 w-full">
            <Link 
              to={PROFESIONAL_PATHS.login} 
              className="group bg-white p-10 rounded-3xl shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 flex flex-col border border-slate-200"
            >
              <div className="bg-blue-50 w-20 h-20 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 group-hover:bg-blue-600 group-hover:text-white transition-all duration-300">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2"></path>
                  <circle cx="9" cy="7" r="4"></circle>
                  <path d="M22 21v-2a4 4 0 0 0-3-3.87"></path>
                  <path d="M16 3.13a4 4 0 0 1 0 7.75"></path>
                </svg>
              </div>
              <h3 className="text-2xl font-black text-slate-900 mb-3 uppercase tracking-tight">Portal Profesional</h3>
              <p className="text-slate-600 text-base leading-relaxed mb-8">
                Ingresá al panel médico, gestioná tu agenda y consultá historias clínicas.
              </p>
              <div className="mt-auto bg-blue-600 text-white px-8 py-3 rounded-xl font-bold text-center group-hover:bg-blue-700 transition-colors shadow-lg shadow-blue-600/20">
                Acceso Médico
              </div>
            </Link>

            <Link 
              to={PACIENTE_PATHS.login} 
              className="group bg-white p-10 rounded-3xl shadow-sm hover:shadow-xl hover:-translate-y-1 transition-all duration-300 flex flex-col border border-slate-200"
            >
              <div className="bg-emerald-50 w-20 h-20 rounded-2xl flex items-center justify-center mb-6 group-hover:scale-110 group-hover:bg-emerald-600 group-hover:text-white transition-all duration-300">
                <svg xmlns="http://www.w3.org/2000/svg" width="48" height="48" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round">
                  <path d="M19 14c1.49-1.46 3-3.21 3-5.5A5.5 5.5 0 0 0 16.5 3c-1.76 0-3 .5-4.5 2-1.5-1.5-2.74-2-4.5-2A5.5 5.5 0 0 0 2 8.5c0 2.3 1.5 4.05 3 5.5l7 7Z"></path>
                </svg>
              </div>
              <h3 className="text-2xl font-black text-slate-900 mb-3 uppercase tracking-tight">Portal Paciente</h3>
              <p className="text-slate-600 text-base leading-relaxed mb-8">
                Sacá turnos, revisá tus resultados y mantené contacto con tus médicos.
              </p>
              <div className="mt-auto bg-emerald-700 text-white px-8 py-3 rounded-xl font-bold text-center group-hover:bg-emerald-800 transition-colors shadow-lg shadow-emerald-700/20">
                Acceso Paciente
              </div>
            </Link>
          </div>
        </section>

        {/* Benefits Section */}
        <section id="beneficios" className="max-w-7xl mx-auto px-6 py-10">
          <div className="bg-white rounded-3xl border border-slate-200 p-8 md:p-10 shadow-sm">
            <h2 className="text-2xl md:text-3xl font-black text-slate-900 mb-7">Beneficios principales</h2>
            <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
              <article className="rounded-2xl bg-slate-50 border border-slate-200 p-6 hover:bg-white hover:shadow-md transition-all">
                <h3 className="font-bold text-slate-900 mb-2">Operación centralizada</h3>
                <p className="text-slate-600 text-sm">Unificá agenda, perfiles y atención en un único ecosistema digital.</p>
              </article>
              <article className="rounded-2xl bg-slate-50 border border-slate-200 p-6 hover:bg-white hover:shadow-md transition-all">
                <h3 className="font-bold text-slate-900 mb-2">Seguridad y trazabilidad</h3>
                <p className="text-slate-600 text-sm">Control de acceso por perfiles y registro de cambios para auditoría clínica.</p>
              </article>
              <article className="rounded-2xl bg-slate-50 border border-slate-200 p-6 hover:bg-white hover:shadow-md transition-all">
                <h3 className="font-bold text-slate-900 mb-2">Microservicios</h3>
                <p className="text-slate-600 text-sm">Arquitectura preparada para crecimiento de turnos e historiales clínicos.</p>
              </article>
            </div>
          </div>
        </section>
      </main>

      <footer id="nosotros" className="w-full mt-10 py-10 bg-white border-t border-slate-200">
        <div className="max-w-7xl mx-auto px-6 flex flex-col md:flex-row gap-4 md:items-center md:justify-between">
          <div>
            <p className="text-slate-800 font-bold">Sistema de Gestión Clínica Salud</p>
            <p className="text-slate-500 text-sm">Atención moderna y eficiente para el siglo XXI.</p>
          </div>
          <p className="text-slate-400 text-xs font-medium uppercase tracking-widest">
            © 2026 Portal Salud • Desarrollado por Ignacio Taboada
          </p>
        </div>
      </footer>
    </div>
  );
};

export default Landing;