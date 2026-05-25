import React, { useEffect, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';
import SexoSelectField from '../../components/SexoSelectField';
import ProfesionalPortalNav from '../../components/ProfesionalPortalNav';
import { useProfesionalSession } from '../../context/ProfesionalSessionContext';
import { apiErrorMessage } from '../../utils/adminApiError';
import { getMaxBirthDateString, isAtLeastAge } from '../../utils/ageValidation';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';

const MEMBRESIA_SIN_VERIFICAR = 'SIN_VERIFICAR';

const outlineButtonClass =
  'inline-flex items-center justify-center px-4 py-2.5 rounded-lg border border-blue-600/35 bg-white text-blue-700 text-sm font-semibold hover:bg-blue-50 hover:border-blue-600/50 transition-colors';

const inputClass =
  'w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-blue-500/30';

const ProfesionalCargarPacientePage = () => {
  const navigate = useNavigate();
  const { membresiaActual, cargandoPerfil } = useProfesionalSession();
  const [idProvincia, setIdProvincia] = useState('');
  const [idLocalidad, setIdLocalidad] = useState('');
  const [obrasSociales, setObrasSociales] = useState([]);
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [form, setForm] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    email: '',
    telefono: '',
    fechaNacimiento: '',
    sexo: '',
    direccion: '',
    idObraSocial: '',
    numeroAfiliado: '',
  });

  const pendienteVerificacion =
    !cargandoPerfil && membresiaActual?.toUpperCase() === MEMBRESIA_SIN_VERIFICAR;

  useEffect(() => {
    const fetchObras = async () => {
      try {
        const { data } = await clienteAxios.get('/usuarios/api/obras-sociales');
        const lista = Array.isArray(data) ? data : [];
        setObrasSociales(lista);
        const noPosee = lista.find((o) => o.descripcion === 'NO POSEE');
        if (noPosee) {
          setForm((prev) => ({ ...prev, idObraSocial: String(noPosee.idObraSocial) }));
        }
      } catch {
        setError('No se pudieron cargar las obras sociales.');
      }
    };
    fetchObras();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setForm((prev) => ({ ...prev, [name]: value }));
  };

  const handleSubmit = async (e) => {
    e.preventDefault();
    setError('');
    if (pendienteVerificacion) return;
    if (!form.idObraSocial || !idLocalidad || !form.direccion.trim() || !form.sexo) {
      setError('Completá obra social, provincia, localidad y dirección.');
      return;
    }
    if (form.fechaNacimiento && !isAtLeastAge(form.fechaNacimiento)) {
      setError('El paciente debe ser mayor de 18 años.');
      return;
    }

    setLoading(true);
    try {
      const payload = {
        nombre: form.nombre.trim(),
        apellido: form.apellido.trim(),
        dni: parseInt(form.dni, 10),
        email: form.email.trim(),
        telefono: form.telefono.trim(),
        fechaNacimiento: form.fechaNacimiento || null,
        sexo: form.sexo,
        idLocalidad: parseInt(idLocalidad, 10),
        direccion: form.direccion.trim(),
        idObraSocial: parseInt(form.idObraSocial, 10),
        numeroAfiliado: form.numeroAfiliado.trim() || null,
      };
      const { data } = await clienteAxios.post('/usuarios/api/profesionales/me/pacientes', payload);
      navigate(PROFESIONAL_PATHS.pacienteHistorial(data.idUsuario), {
        state: { mensaje: data.mensaje },
      });
    } catch (err) {
      setError(apiErrorMessage(err, 'No se pudo cargar el paciente.'));
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-gray-50 font-sans text-slate-800">
      <ProfesionalPortalNav />
      <main className="max-w-2xl mx-auto px-6 py-10">
        <Link to={PROFESIONAL_PATHS.pacientes} className={`${outlineButtonClass} mb-6`}>
          ← Volver a Mis pacientes
        </Link>

        <header className="mb-6">
          <h1 className="text-2xl font-black text-gray-900">Cargar paciente con usuario</h1>
          <p className="text-sm text-gray-600 mt-1">
            Se creará la cuenta sin contraseña. El paciente recibirá un correo (72 h) para activarla y definir su
            clave.
          </p>
        </header>

        {pendienteVerificacion && (
          <div className="mb-6 rounded-xl border border-amber-300 bg-amber-50 px-4 py-3 text-sm text-amber-900">
            Tu cuenta está pendiente de verificación administrativa.
          </div>
        )}

        <form onSubmit={handleSubmit} className="bg-white rounded-2xl border border-gray-200 p-6 shadow-sm space-y-4">
          <div className="grid gap-4 sm:grid-cols-2">
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Nombre</label>
              <input name="nombre" required value={form.nombre} onChange={handleChange} className={inputClass} />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Apellido</label>
              <input name="apellido" required value={form.apellido} onChange={handleChange} className={inputClass} />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-2">DNI</label>
              <input name="dni" required type="number" value={form.dni} onChange={handleChange} className={inputClass} />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Email</label>
              <input name="email" required type="email" value={form.email} onChange={handleChange} className={inputClass} />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Teléfono</label>
              <input name="telefono" required value={form.telefono} onChange={handleChange} className={inputClass} />
            </div>
            <div>
              <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Fecha de nacimiento</label>
              <input
                name="fechaNacimiento"
                type="date"
                max={getMaxBirthDateString()}
                value={form.fechaNacimiento}
                onChange={handleChange}
                className={inputClass}
              />
            </div>
            <SexoSelectField
              value={form.sexo}
              onChange={handleChange}
              inputClassName={inputClass}
            />
          </div>

          <ProvinciaLocalidadFields
            provinciaId={idProvincia}
            localidadId={idLocalidad}
            onProvinciaChange={(v) => {
              setIdProvincia(v);
              setIdLocalidad('');
            }}
            onLocalidadChange={setIdLocalidad}
            inputClassName={inputClass}
            showLabels
          />

          <div>
            <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Dirección</label>
            <input name="direccion" required value={form.direccion} onChange={handleChange} className={inputClass} />
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Obra social</label>
            <select
              name="idObraSocial"
              required
              value={form.idObraSocial}
              onChange={handleChange}
              className={inputClass}
            >
              <option value="">Seleccionar…</option>
              {obrasSociales.map((o) => (
                <option key={o.idObraSocial} value={o.idObraSocial}>
                  {o.descripcion}
                </option>
              ))}
            </select>
          </div>

          <div>
            <label className="block text-xs font-bold text-gray-500 uppercase mb-2">Nº afiliado (opcional)</label>
            <input name="numeroAfiliado" value={form.numeroAfiliado} onChange={handleChange} className={inputClass} />
          </div>

          {error && (
            <div className="rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">{error}</div>
          )}

          <button
            type="submit"
            disabled={loading || pendienteVerificacion}
            className="w-full py-3 rounded-lg bg-blue-600 text-white font-semibold hover:bg-blue-700 disabled:opacity-50"
          >
            {loading ? 'Cargando…' : 'Cargar paciente'}
          </button>
        </form>
      </main>
    </div>
  );
};

export default ProfesionalCargarPacientePage;
