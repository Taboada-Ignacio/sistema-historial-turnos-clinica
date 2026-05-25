import React from 'react';
import { Link } from 'react-router-dom';
import ProfesionalPortalNav from '../../components/ProfesionalPortalNav';
import ProfesionalModuloCard from '../../components/ProfesionalModuloCard';
import { useProfesionalSession } from '../../context/ProfesionalSessionContext';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';

const MEMBRESIA_SIN_VERIFICAR = 'SIN_VERIFICAR';

const iconBoxClass = (active) =>
  `w-12 h-12 rounded-2xl flex items-center justify-center mb-6 transition-colors ${
    active ? 'bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white' : 'bg-gray-200 text-gray-400'
  }`;

const iconoDuoBoxClass = (active) =>
  `inline-flex items-center gap-3 px-4 py-2.5 rounded-2xl mb-6 transition-colors ${
    active
      ? 'bg-blue-50 text-blue-600 group-hover:bg-blue-600 group-hover:text-white'
      : 'bg-gray-200 text-gray-400'
  }`;

const svgProps = {
  xmlns: 'http://www.w3.org/2000/svg',
  width: 22,
  height: 22,
  viewBox: '0 0 24 24',
  fill: 'none',
  stroke: 'currentColor',
  strokeWidth: 2,
  strokeLinecap: 'round',
  strokeLinejoin: 'round',
  'aria-hidden': true,
};

/** Persona y ubicación uno al lado del otro (bloque rectangular). */
const IconoPacienteEnCiudad = ({ active }) => (
  <div className={iconoDuoBoxClass(active)}>
    <svg {...svgProps}>
      <path d="M20 21v-2a4 4 0 0 0-4-4H8a4 4 0 0 0-4 4v2" />
      <circle cx="12" cy="7" r="4" />
    </svg>
    <span className={`w-px h-6 shrink-0 ${active ? 'bg-blue-200 group-hover:bg-blue-300' : 'bg-gray-300'}`} />
    <svg {...svgProps}>
      <path d="M20 10c0 4.993-5.539 10.193-7.399 11.799a1 1 0 0 1-1.202 0C9.539 20.193 4 14.993 4 10a8 8 0 0 1 16 0" />
      <circle cx="12" cy="10" r="3" />
    </svg>
  </div>
);

const ProfesionalPacientesPage = () => {
  const { ubicacion, membresiaActual, cargandoPerfil } = useProfesionalSession();

  const pendienteVerificacion =
    !cargandoPerfil && membresiaActual?.toUpperCase() === MEMBRESIA_SIN_VERIFICAR;

  const zonaLabel =
    ubicacion?.nombreLocalidad && ubicacion?.nombreProvincia
      ? `${ubicacion.nombreLocalidad}, ${ubicacion.nombreProvincia}`
      : null;

  const moduloCiudadActivo = !pendienteVerificacion && Boolean(zonaLabel) && !cargandoPerfil;
  const moduloGeneralActivo = !pendienteVerificacion && !cargandoPerfil;

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      <ProfesionalPortalNav />

      <main className="max-w-7xl mx-auto px-6 py-10">
        <header className="mb-8 flex flex-col sm:flex-row sm:items-start sm:justify-between gap-4">
          <div>
            <Link
              to={PROFESIONAL_PATHS.dashboard}
              className="inline-flex items-center gap-1 text-sm font-semibold text-blue-700 hover:text-blue-900 mb-3"
            >
              <svg xmlns="http://www.w3.org/2000/svg" width="16" height="16" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M19 12H5M12 19l-7-7 7-7" />
              </svg>
              Volver al panel
            </Link>
            <h1 className="text-4xl font-black text-gray-900 leading-tight">Mis pacientes</h1>
            <p className="text-gray-500 font-medium mt-2 max-w-2xl">
              Gestioná el vínculo con tus pacientes: búsqueda en tu ciudad, fichas, historial clínico y turnos.
            </p>
            {zonaLabel && (
              <p className="text-sm text-blue-800 mt-3 font-semibold">
                Ciudad de atención: {zonaLabel}
              </p>
            )}
          </div>
        </header>

        {pendienteVerificacion && (
          <div
            role="alert"
            className="mb-8 rounded-2xl border border-amber-300 bg-amber-50 px-6 py-4 text-amber-900"
          >
            <p className="font-bold text-amber-950 mb-1">Cuenta pendiente de verificación</p>
            <p className="text-sm text-amber-800 leading-relaxed">
              Las opciones de pacientes se habilitan cuando un administrador verifique tu matrícula profesional.
            </p>
          </div>
        )}

        {!pendienteVerificacion && !cargandoPerfil && !zonaLabel && (
          <div className="mb-8 rounded-2xl border border-amber-200 bg-amber-50 px-6 py-4 text-amber-900 text-sm">
            Completá provincia y localidad en{' '}
            <Link to={PROFESIONAL_PATHS.perfil} className="font-bold underline">
              tu perfil profesional
            </Link>{' '}
            para usar la búsqueda por ciudad.
          </div>
        )}

        <div className="grid grid-cols-1 md:grid-cols-2 gap-8">
          <ProfesionalModuloCard
            disabled={!moduloCiudadActivo}
            titulo="Buscar Paciente en mi ciudad"
            descripcion="Encontrá personas por apellido, nombre o DNI. Solo se listan registros de tu misma localidad y provincia."
            linkTo={moduloCiudadActivo ? PROFESIONAL_PATHS.pacientesBuscar : undefined}
            linkLabel="Buscar Paciente en mi ciudad"
          >
            <IconoPacienteEnCiudad active={moduloCiudadActivo} />
          </ProfesionalModuloCard>

          <ProfesionalModuloCard
            disabled={!moduloGeneralActivo}
            titulo="Buscar Paciente en general"
            descripcion="Elegí provincia y localidad, e ingresá apellido, nombre o DNI. Misma búsqueda que en tu ciudad, pero en cualquier zona."
            linkTo={moduloGeneralActivo ? PROFESIONAL_PATHS.pacientesBuscarGeneral : undefined}
            linkLabel="Buscar Paciente en general"
          >
            <div className={iconBoxClass(moduloGeneralActivo)}>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
                <circle cx="11" cy="11" r="8" />
                <path d="m21 21-4.3-4.3" />
                <path d="M11 8v6M8 11h6" />
              </svg>
            </div>
          </ProfesionalModuloCard>

          <ProfesionalModuloCard
            disabled={!moduloGeneralActivo}
            titulo="Cargar paciente con usuario"
            descripcion="Alta de paciente sin contraseña. Se envía un correo para que active su cuenta (72 h) y defina su clave."
            linkTo={moduloGeneralActivo ? PROFESIONAL_PATHS.pacientesCargar : undefined}
            linkLabel="Cargar paciente"
          >
            <div className={iconBoxClass(moduloGeneralActivo)}>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round" aria-hidden>
                <path d="M16 21v-2a4 4 0 0 0-4-4H6a4 4 0 0 0-4 4v2" />
                <circle cx="9" cy="7" r="4" />
                <line x1="19" y1="8" x2="19" y2="14" />
                <line x1="22" y1="11" x2="16" y2="11" />
              </svg>
            </div>
          </ProfesionalModuloCard>

          <ProfesionalModuloCard
            disabled
            titulo="Historial clínico"
            descripcion="Accedé a estudios, recetas, diagnósticos y evoluciones autorizadas del paciente."
            linkLabel="Próximamente"
          >
            <div className={iconBoxClass(false)}>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <path d="M14 2H6a2 2 0 0 0-2 2v16a2 2 0 0 0 2 2h12a2 2 0 0 0 2-2V8z" />
                <polyline points="14 2 14 8 20 8" />
                <line x1="16" y1="13" x2="8" y2="13" />
                <line x1="16" y1="17" x2="8" y2="17" />
              </svg>
            </div>
          </ProfesionalModuloCard>

          <ProfesionalModuloCard
            disabled
            titulo="Turnos del paciente"
            descripcion="Revisá y gestioná los turnos que el paciente tiene o tuvo con vos en la agenda."
            linkLabel="Próximamente"
          >
            <div className={iconBoxClass(false)}>
              <svg xmlns="http://www.w3.org/2000/svg" width="24" height="24" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round">
                <rect x="3" y="4" width="18" height="18" rx="2" ry="2" />
                <line x1="16" y1="2" x2="16" y2="6" />
                <line x1="8" y1="2" x2="8" y2="6" />
                <line x1="3" y1="10" x2="21" y2="10" />
              </svg>
            </div>
          </ProfesionalModuloCard>
        </div>
      </main>
    </div>
  );
};

export default ProfesionalPacientesPage;
