import React, { useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';
import ProfesionalPortalNav from '../../components/ProfesionalPortalNav';
import { useProfesionalSession } from '../../context/ProfesionalSessionContext';
import { apiErrorMessage } from '../../utils/adminApiError';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';

const MEMBRESIA_SIN_VERIFICAR = 'SIN_VERIFICAR';

const TIPO_CUENTA_LABEL = {
  PACIENTE: 'Paciente',
  PROFESIONAL: 'Profesional',
  ADMINISTRADOR: 'Administrador',
};

const outlineButtonClass =
  'inline-flex items-center justify-center px-4 py-2.5 rounded-lg border border-blue-600/35 bg-white text-blue-700 text-sm font-semibold hover:bg-blue-50 hover:border-blue-600/50 transition-colors';

const inputClass =
  'w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-blue-500/30 disabled:opacity-60';

const ProfesionalPacientesBuscarGeneralPage = () => {
  const navigate = useNavigate();
  const { membresiaActual, cargandoPerfil } = useProfesionalSession();
  const [texto, setTexto] = useState('');
  const [idProvincia, setIdProvincia] = useState('');
  const [idLocalidad, setIdLocalidad] = useState('');
  const [resultados, setResultados] = useState([]);
  const [total, setTotal] = useState(0);
  const [criteriosAplicados, setCriteriosAplicados] = useState('');
  const [buscado, setBuscado] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const pendienteVerificacion =
    !cargandoPerfil && membresiaActual?.toUpperCase() === MEMBRESIA_SIN_VERIFICAR;

  const puedeBuscar =
    !pendienteVerificacion &&
    texto.trim().length > 0 &&
    Boolean(idProvincia) &&
    Boolean(idLocalidad);

  const handleBuscar = async (e) => {
    e?.preventDefault();
    setError('');
    if (!texto.trim()) {
      setError('Ingresá apellido, nombre o DNI para buscar.');
      return;
    }
    if (!idProvincia || !idLocalidad) {
      setError('Seleccioná provincia y localidad.');
      return;
    }
    if (pendienteVerificacion) return;

    setLoading(true);
    setBuscado(true);
    try {
      const { data } = await clienteAxios.get('/usuarios/api/profesionales/me/pacientes/buscar-general', {
        params: {
          q: texto.trim(),
          idProvincia,
          idLocalidad,
        },
      });
      const lista = Array.isArray(data?.personas) ? data.personas : [];
      setResultados(lista);
      setTotal(typeof data?.total === 'number' ? data.total : lista.length);
      setCriteriosAplicados(data?.criteriosAplicados ?? '');
    } catch (err) {
      setResultados([]);
      setTotal(0);
      setCriteriosAplicados('');
      setError(apiErrorMessage(err, 'No se pudo realizar la búsqueda.'));
    } finally {
      setLoading(false);
    }
  };

  const limpiar = () => {
    setTexto('');
    setIdProvincia('');
    setIdLocalidad('');
    setResultados([]);
    setTotal(0);
    setCriteriosAplicados('');
    setBuscado(false);
    setError('');
  };

  const seleccionarPersona = (idUsuario) => {
    navigate(PROFESIONAL_PATHS.pacienteDetalle(idUsuario), {
      state: { idProvincia, idLocalidad, origen: 'general' },
    });
  };

  const mensajeContador = () => {
    if (total === 0) {
      return 'No se encontraron personas con ese criterio en la zona elegida.';
    }
    if (total === 1) {
      return 'Se encontró 1 persona. Seleccioná para ver el detalle.';
    }
    return `Se encontraron ${total} personas. Seleccioná una para ver el detalle.`;
  };

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      <ProfesionalPortalNav />

      <main className="max-w-4xl mx-auto px-6 py-10">
        <Link to={PROFESIONAL_PATHS.pacientes} className={`${outlineButtonClass} mb-6`}>
          ← Volver a Mis pacientes
        </Link>

        <header className="mb-6">
          <h1 className="text-2xl font-black text-gray-900">Buscar paciente en general</h1>
          <p className="text-sm text-gray-600 mt-1">
            Elegí provincia y localidad, e ingresá apellido, nombre o DNI. Se listan pacientes,
            profesionales y administradores de esa zona.
          </p>
        </header>

        {pendienteVerificacion && (
          <div
            role="alert"
            className="mb-6 rounded-2xl border border-amber-300 bg-amber-50 px-5 py-4 text-amber-900 text-sm"
          >
            <p className="font-bold text-amber-950 mb-1">Cuenta pendiente de verificación</p>
            <p>La búsqueda estará disponible cuando un administrador habilite tu matrícula.</p>
          </div>
        )}

        <section className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
          <form onSubmit={handleBuscar} className="space-y-4">
            <ProvinciaLocalidadFields
              provinciaId={idProvincia}
              localidadId={idLocalidad}
              onProvinciaChange={(val) => {
                setIdProvincia(val);
                setIdLocalidad('');
              }}
              onLocalidadChange={setIdLocalidad}
              localidadDisabled={pendienteVerificacion || !idProvincia}
              inputClassName={inputClass}
              showLabels
              labelClassName="block text-xs font-bold text-gray-500 uppercase mb-2"
            />

            <div>
              <label
                htmlFor="busqueda-paciente-general"
                className="block text-xs font-bold text-gray-500 uppercase mb-2"
              >
                Apellido, nombre o DNI
              </label>
              <input
                id="busqueda-paciente-general"
                type="text"
                value={texto}
                onChange={(e) => setTexto(e.target.value)}
                disabled={pendienteVerificacion}
                placeholder="Ej.: García, Juan García o 30111222"
                className={inputClass}
              />
            </div>

            {error && (
              <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700" role="alert">
                {error}
              </div>
            )}

            <div className="flex flex-wrap gap-3">
              <button
                type="submit"
                disabled={loading || !puedeBuscar}
                className="px-5 py-2.5 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 disabled:opacity-50"
              >
                {loading ? 'Buscando…' : 'Buscar'}
              </button>
              <button
                type="button"
                onClick={limpiar}
                className="px-5 py-2.5 rounded-lg border border-gray-300 text-gray-800 font-semibold hover:bg-gray-50"
              >
                Limpiar
              </button>
            </div>
          </form>
        </section>

        {buscado && !loading && (
          <section className="mt-6 bg-white rounded-2xl border border-gray-200 p-6 shadow-sm">
            <div
              className={`mb-4 rounded-xl border px-4 py-3 ${
                total > 0
                  ? 'border-blue-200 bg-blue-50 text-blue-900'
                  : 'border-amber-200 bg-amber-50 text-amber-900'
              }`}
              role="status"
            >
              <p className="font-bold">{mensajeContador()}</p>
              {criteriosAplicados && (
                <p className="text-sm mt-1 opacity-90">Criterios: {criteriosAplicados}</p>
              )}
            </div>

            {resultados.length > 0 && (
              <ul className="divide-y divide-gray-100 rounded-xl border border-gray-200 overflow-hidden">
                {resultados.map((p) => (
                  <li key={p.idUsuario}>
                    <button
                      type="button"
                      onClick={() => seleccionarPersona(p.idUsuario)}
                      className="w-full text-left px-4 py-4 hover:bg-blue-50/80 focus:bg-blue-50 focus:outline-none focus:ring-2 focus:ring-inset focus:ring-blue-400 transition-colors"
                    >
                      <div className="flex flex-wrap items-center justify-between gap-2">
                        <div>
                          <p className="font-bold text-gray-900">
                            {p.apellido}, {p.nombre}
                          </p>
                          <p className="text-sm text-gray-600 mt-0.5">
                            DNI {p.dni ?? '—'}
                            {[p.nombreLocalidad, p.nombreProvincia].filter(Boolean).length > 0 && (
                              <span>
                                {' '}
                                · {[p.nombreLocalidad, p.nombreProvincia].filter(Boolean).join(', ')}
                              </span>
                            )}
                          </p>
                        </div>
                        {p.tipoCuenta && (
                          <span className="shrink-0 px-2.5 py-1 rounded-full text-xs font-bold bg-gray-100 text-gray-700">
                            {TIPO_CUENTA_LABEL[p.tipoCuenta] ?? p.tipoCuenta}
                          </span>
                        )}
                      </div>
                      <p className="text-xs text-blue-700 font-semibold mt-2">Ver detalle →</p>
                    </button>
                  </li>
                ))}
              </ul>
            )}
          </section>
        )}
      </main>
    </div>
  );
};

export default ProfesionalPacientesBuscarGeneralPage;
