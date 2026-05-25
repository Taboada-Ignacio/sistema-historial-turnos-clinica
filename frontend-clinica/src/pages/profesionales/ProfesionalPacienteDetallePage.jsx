import React, { useEffect, useState } from 'react';
import { Link, useLocation, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import ProfesionalPortalNav from '../../components/ProfesionalPortalNav';
import { apiErrorMessage } from '../../utils/adminApiError';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';
import { formatSexoLabel } from '../../components/SexoSelectField';

const outlineButtonClass =
  'inline-flex items-center justify-center px-4 py-2.5 rounded-lg border border-blue-600/35 bg-white text-blue-700 text-sm font-semibold hover:bg-blue-50 hover:border-blue-600/50 transition-colors';

const TIPO_CUENTA_LABEL = {
  PACIENTE: 'Paciente',
  PROFESIONAL: 'Profesional',
  ADMINISTRADOR: 'Administrador',
};

const formatearFecha = (fecha) => {
  if (!fecha) return '—';
  try {
    const d = new Date(fecha);
    if (Number.isNaN(d.getTime())) return fecha;
    return d.toLocaleDateString('es-AR', { day: '2-digit', month: '2-digit', year: 'numeric' });
  } catch {
    return fecha;
  }
};

const CampoDetalle = ({ etiqueta, valor }) => (
  <div>
    <dt className="text-xs font-bold text-gray-500 uppercase tracking-wide">{etiqueta}</dt>
    <dd className="mt-1 text-sm font-semibold text-gray-900">{valor ?? '—'}</dd>
  </div>
);

const ProfesionalPacienteDetallePage = () => {
  const { id } = useParams();
  const location = useLocation();
  const origenGeneral = location.state?.origen === 'general';
  const idProvinciaZona = location.state?.idProvincia;
  const idLocalidadZona = location.state?.idLocalidad;
  const volverA = origenGeneral
    ? PROFESIONAL_PATHS.pacientesBuscarGeneral
    : PROFESIONAL_PATHS.pacientesBuscar;

  const [persona, setPersona] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');

  useEffect(() => {
    let cancelado = false;
    const cargar = async () => {
      setLoading(true);
      setError('');
      try {
        const params = {};
        if (origenGeneral && idProvinciaZona && idLocalidadZona) {
          params.idProvincia = idProvinciaZona;
          params.idLocalidad = idLocalidadZona;
        }
        const { data } = await clienteAxios.get(`/usuarios/api/profesionales/me/pacientes/${id}`, {
          params,
        });
        if (!cancelado) setPersona(data);
      } catch (err) {
        if (!cancelado) {
          setPersona(null);
          setError(apiErrorMessage(err, 'No se pudo cargar el detalle.'));
        }
      } finally {
        if (!cancelado) setLoading(false);
      }
    };
    if (id) cargar();
    return () => {
      cancelado = true;
    };
  }, [id, origenGeneral, idProvinciaZona, idLocalidadZona]);

  const ubicacionLabel =
    persona?.nombreLocalidad && persona?.nombreProvincia
      ? `${persona.nombreLocalidad}, ${persona.nombreProvincia}`
      : persona?.nombreLocalidad || persona?.nombreProvincia || '—';

  const direccionCompleta = [persona?.direccion, ubicacionLabel !== '—' ? ubicacionLabel : null]
    .filter(Boolean)
    .join(' — ');

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      <ProfesionalPortalNav />

      <main className="max-w-2xl mx-auto px-6 py-10">
        <Link to={volverA} className={`${outlineButtonClass} mb-6`}>
          ← Volver a la búsqueda
        </Link>

        <header className="mb-6">
          <h1 className="text-2xl font-black text-gray-900">Detalle de la persona</h1>
          <p className="text-sm text-gray-600 mt-1">Datos registrados en tu misma ciudad.</p>
        </header>

        {loading && (
          <p className="text-sm text-gray-500" role="status">
            Cargando…
          </p>
        )}

        {error && !loading && (
          <div
            role="alert"
            className="rounded-xl border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700"
          >
            {error}
          </div>
        )}

        {persona && !loading && (
          <section className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            {persona.tipoCuenta && (
              <span className="inline-block mb-4 px-3 py-1 rounded-full text-xs font-bold bg-blue-100 text-blue-800">
                {TIPO_CUENTA_LABEL[persona.tipoCuenta] ?? persona.tipoCuenta}
              </span>
            )}

            <dl className="grid gap-5 sm:grid-cols-2">
              <CampoDetalle etiqueta="Apellido" valor={persona.apellido} />
              <CampoDetalle etiqueta="Nombre" valor={persona.nombre} />
              <CampoDetalle etiqueta="DNI" valor={persona.dni} />
              <CampoDetalle etiqueta="Teléfono" valor={persona.telefono} />
              <CampoDetalle etiqueta="Fecha de nacimiento" valor={formatearFecha(persona.fechaNacimiento)} />
              <CampoDetalle etiqueta="Sexo" valor={formatSexoLabel(persona.sexo)} />
              <CampoDetalle etiqueta="Localidad" valor={ubicacionLabel} />
              <div className="sm:col-span-2">
                <CampoDetalle etiqueta="Dirección" valor={direccionCompleta || '—'} />
              </div>
            </dl>
          </section>
        )}
      </main>
    </div>
  );
};

export default ProfesionalPacienteDetallePage;
