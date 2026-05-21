import React, { useMemo, useState } from 'react';
import { Link, useLocation, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';
import { adminApiErrorMessage } from '../../utils/adminApiError';
import { ADMIN_PATHS } from '../../utils/portalPaths';

/** Etiqueta del modo de búsqueda según filtros activos (solo UI). */
function describirModoBusqueda(texto, idProvincia, idLocalidad) {
  const tieneNombre = texto.trim().length > 0;
  const tieneProvincia = Boolean(idProvincia);
  const tieneLocalidad = Boolean(idLocalidad);

  if (tieneNombre && !tieneProvincia) return '1) Solo apellido y/o nombre';
  if (tieneProvincia && tieneLocalidad && tieneNombre) return '5) Provincia + localidad + apellido/nombre';
  if (tieneProvincia && tieneLocalidad) return '3) Solo provincia + localidad';
  if (tieneProvincia && tieneNombre) return '4) Provincia + apellido/nombre';
  if (tieneProvincia) return '2) Solo provincia';
  return null;
}

const MODOS_AYUDA = [
  { id: 1, titulo: 'Solo apellido y/o nombre', detalle: 'Completá el campo de nombre. No hace falta elegir provincia ni localidad.' },
  { id: 2, titulo: 'Solo provincia', detalle: 'Elegí una provincia. Se listan todos los pacientes de esa provincia.' },
  { id: 3, titulo: 'Provincia + localidad', detalle: 'Elegí provincia y localidad. Se listan todos los pacientes de esa localidad.' },
  { id: 4, titulo: 'Provincia + apellido/nombre', detalle: 'Provincia obligatoria más texto en nombre/apellido.' },
  { id: 5, titulo: 'Provincia + localidad + apellido/nombre', detalle: 'Los tres filtros activos a la vez.' },
];

const AdminPacientesBuscarPage = () => {
  const navigate = useNavigate();
  const location = useLocation();
  const flashMensaje = location.state?.mensaje;
  const [texto, setTexto] = useState('');
  const [idProvincia, setIdProvincia] = useState('');
  const [idLocalidad, setIdLocalidad] = useState('');
  const [resultados, setResultados] = useState([]);
  const [total, setTotal] = useState(0);
  const [criteriosAplicados, setCriteriosAplicados] = useState('');
  const [buscado, setBuscado] = useState(false);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');

  const modoPreview = useMemo(
    () => describirModoBusqueda(texto, idProvincia, idLocalidad),
    [texto, idProvincia, idLocalidad]
  );

  const puedeBuscar = texto.trim().length > 0 || Boolean(idProvincia);

  const handleBuscar = async (e) => {
    e?.preventDefault();
    setError('');
    if (!puedeBuscar) {
      setError('Completá apellido/nombre y/o seleccioná una provincia. Cada campo se puede usar por separado.');
      return;
    }

    setLoading(true);
    setBuscado(true);
    try {
      const params = {};
      if (texto.trim()) params.q = texto.trim();
      if (idProvincia) params.idProvincia = idProvincia;
      if (idLocalidad) params.idLocalidad = idLocalidad;

      const { data } = await clienteAxios.get('/usuarios/api/pacientes/buscar', { params });
      const lista = Array.isArray(data?.pacientes) ? data.pacientes : Array.isArray(data) ? data : [];
      setResultados(lista);
      setTotal(typeof data?.total === 'number' ? data.total : lista.length);
      setCriteriosAplicados(data?.criteriosAplicados ?? '');
    } catch (err) {
      setResultados([]);
      setTotal(0);
      setCriteriosAplicados('');
      setError(adminApiErrorMessage(err, 'No se pudo realizar la búsqueda.'));
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

  const mensajeContador = () => {
    if (total === 0) {
      return 'No se encontraron pacientes que coinciden con la búsqueda.';
    }
    if (total === 1) {
      return 'Se encontró 1 paciente que coincide con la búsqueda.';
    }
    return `Se encontraron ${total} pacientes que coinciden con la búsqueda.`;
  };

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <div className="flex flex-wrap items-center justify-between gap-4 mb-6">
        <div>
          <h2 className="text-xl font-black text-slate-900">Consultar pacientes</h2>
          <p className="text-sm text-slate-600 mt-1">
            Cada filtro funciona por separado o en combinación. La localidad es opcional y solo aplica si elegiste
            provincia.
          </p>
        </div>
        <Link
          to={ADMIN_PATHS.entidades}
          className="inline-flex items-center gap-2 rounded-xl border border-slate-200 bg-slate-100 px-4 py-2.5 text-sm font-bold text-slate-800 shadow-sm hover:bg-slate-200"
        >
          ← Volver a entidades
        </Link>
      </div>

      {flashMensaje && (
        <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900">
          {flashMensaje}
        </div>
      )}

      <details className="mb-6 rounded-xl border border-slate-200 bg-slate-50/80 p-4">
        <summary className="text-sm font-bold text-slate-700 cursor-pointer">Modos de búsqueda disponibles</summary>
        <ul className="mt-3 space-y-2 text-sm text-slate-600">
          {MODOS_AYUDA.map((m) => (
            <li key={m.id}>
              <span className="font-semibold text-slate-800">{m.titulo}:</span> {m.detalle}
            </li>
          ))}
        </ul>
      </details>

      <form onSubmit={handleBuscar} className="max-w-3xl space-y-5 mb-4">
        <div>
          <label htmlFor="busqueda-nombre" className="block text-xs font-bold text-slate-500 uppercase mb-2">
            Apellido y/o nombre <span className="font-normal normal-case text-slate-400">(opcional si elegís provincia)</span>
          </label>
          <input
            id="busqueda-nombre"
            type="text"
            value={texto}
            onChange={(e) => setTexto(e.target.value)}
            placeholder="Ej.: García, Juan o García Juan"
            className="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-emerald-600/30"
          />
        </div>

        <ProvinciaLocalidadFields
          showLabels
          provinciaRequired={false}
          localidadRequired={false}
          provinciaId={idProvincia}
          localidadId={idLocalidad}
          provinciaPlaceholder="Provincia (opcional si buscás solo por nombre)"
          localidadPlaceholder="Localidad (opcional)"
          onProvinciaChange={(id) => {
            setIdProvincia(id);
            setIdLocalidad('');
          }}
          onLocalidadChange={setIdLocalidad}
          inputClassName="w-full rounded-xl border border-slate-200 px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-emerald-600/30"
        />
        <p className="text-xs text-slate-500 -mt-2">
          Podés buscar solo por provincia sin localidad. La localidad solo filtra dentro de la provincia elegida.
        </p>

        {modoPreview && (
          <p className="text-sm text-emerald-800 bg-emerald-50 border border-emerald-100 rounded-lg px-3 py-2">
            Modo detectado: <strong>{modoPreview}</strong>
          </p>
        )}

        {error && (
          <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700" role="alert">
            {error}
          </div>
        )}

        <div className="flex flex-wrap gap-3">
          <button
            type="submit"
            disabled={loading || !puedeBuscar}
            className="px-5 py-2.5 rounded-lg bg-emerald-700 text-white font-semibold hover:bg-emerald-600 disabled:opacity-50"
          >
            {loading ? 'Buscando…' : 'Buscar pacientes'}
          </button>
          <button
            type="button"
            onClick={limpiar}
            className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
          >
            Limpiar filtros
          </button>
        </div>
      </form>

      {buscado && !loading && (
        <>
          <div
            className={`mb-4 rounded-xl border px-4 py-3 ${
              total > 0
                ? 'border-emerald-200 bg-emerald-50 text-emerald-900'
                : 'border-amber-200 bg-amber-50 text-amber-900'
            }`}
            role="status"
          >
            <p className="text-base font-bold">{mensajeContador()}</p>
            {criteriosAplicados && (
              <p className="text-sm mt-1 opacity-90">Criterios: {criteriosAplicados}</p>
            )}
            {total >= 500 && (
              <p className="text-xs mt-2">Se muestran como máximo 500 resultados. Acotá los filtros si necesitás menos.</p>
            )}
          </div>

          {resultados.length > 0 && (
            <div className="overflow-x-auto border border-slate-200 rounded-xl">
              <table className="w-full text-sm text-left">
                <thead className="bg-slate-50 text-xs font-bold uppercase text-slate-500">
                  <tr>
                    <th className="px-4 py-3">Apellido</th>
                    <th className="px-4 py-3">Nombre</th>
                    <th className="px-4 py-3">DNI</th>
                    <th className="px-4 py-3">Provincia</th>
                    <th className="px-4 py-3">Localidad</th>
                    <th className="px-4 py-3 w-24" />
                  </tr>
                </thead>
                <tbody>
                  {resultados.map((p) => (
                    <tr key={p.idUsuario} className="border-t border-slate-100 hover:bg-slate-50/80">
                      <td className="px-4 py-3 font-medium text-slate-900">{p.apellido}</td>
                      <td className="px-4 py-3 text-slate-800">{p.nombre}</td>
                      <td className="px-4 py-3 text-slate-700">{p.dni ?? '—'}</td>
                      <td className="px-4 py-3 text-slate-700">{p.nombreProvincia ?? '—'}</td>
                      <td className="px-4 py-3 text-slate-700">{p.nombreLocalidad ?? '—'}</td>
                      <td className="px-4 py-3">
                        <button
                          type="button"
                          onClick={() => navigate(ADMIN_PATHS.pacienteDetalle(p.idUsuario))}
                          className="px-3 py-1.5 rounded-lg bg-emerald-700 text-white text-xs font-semibold hover:bg-emerald-600 whitespace-nowrap"
                        >
                          Ver paciente
                        </button>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </>
      )}
    </section>
  );
};

export default AdminPacientesBuscarPage;
