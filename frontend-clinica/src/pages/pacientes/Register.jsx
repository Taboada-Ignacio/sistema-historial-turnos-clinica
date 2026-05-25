import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import { clienteAxiosPublic } from '../../api/axiosConfig';
import PasswordVisibilityToggle from '../../components/PasswordVisibilityToggle';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';
import SexoSelectField from '../../components/SexoSelectField';
import { getMaxBirthDateString, isAtLeastAge } from '../../utils/ageValidation';
import { PACIENTE_PATHS } from '../../utils/portalPaths';
import { apiErrorMessage } from '../../utils/adminApiError';

const Register = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);
  const [provinciaSeleccionada, setProvinciaSeleccionada] = useState('');

  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    email: '',
    telefono: '',
    fechaNacimiento: '',
    sexo: '',
    password: '',
    confirmarPassword: '',
    idLocalidad: '',
    direccion: '',
    idObraSocial: ''
  });

  useEffect(() => {
    const fetchObras = async () => {
      try {
        const response = await clienteAxiosPublic.get('/usuarios/api/obras-sociales');
        const obras = response.data || [];
        const noPosee = obras.find((o) => o.descripcion === 'NO POSEE');
        setFormData((prev) => ({
          ...prev,
          idObraSocial: noPosee ? String(noPosee.idObraSocial) : prev.idObraSocial
        }));
      } catch {
        setError('No se pudieron cargar las obras sociales.');
      }
    };
    fetchObras();
  }, []);

  const handleChange = (e) => {
    const { name, value } = e.target;
    if (error && (name === 'email' || name === 'dni')) setError('');
    setFormData((prev) => ({
      ...prev,
      [name]: value,
      ...(name === 'idLocalidad' ? { direccion: '' } : {})
    }));
  };

  const validacionesPassword = {
    longitud: formData.password.length >= 8,
    mayuscula: /[A-Z]/.test(formData.password),
    minuscula: /[a-z]/.test(formData.password),
    numero: /[0-9]/.test(formData.password),
    especial: /[@#$%^&+=!]/.test(formData.password)
  };

  const isPasswordValid = Object.values(validacionesPassword).every(Boolean);
  const passwordsMatch =
    formData.password === formData.confirmarPassword && formData.password !== '';

  const handleRegister = async (e) => {
    e.preventDefault();

    if (!isPasswordValid || !passwordsMatch || !formData.idLocalidad || !formData.direccion?.trim() || !formData.idObraSocial || !formData.sexo) {
      return;
    }
    if (!isAtLeastAge(formData.fechaNacimiento)) {
      setError('Debés ser mayor de 18 años para registrarte.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const payload = {
        nombre: formData.nombre,
        apellido: formData.apellido,
        dni: parseInt(formData.dni, 10),
        email: formData.email,
        telefono: formData.telefono,
        fechaNacimiento: formData.fechaNacimiento,
        sexo: formData.sexo,
        password: formData.password,
        idLocalidad: parseInt(formData.idLocalidad, 10),
        direccion: formData.direccion.trim(),
        idObraSocial: parseInt(formData.idObraSocial, 10),
        rolesIds: [2]
      };

      await clienteAxiosPublic.post('/usuarios/api/pacientes/registro', payload);
      navigate(PACIENTE_PATHS.verificarEmail, { state: { email: formData.email } });
    } catch (err) {
      setError(apiErrorMessage(err, 'No se pudo completar el registro. Verificá los datos ingresados.'));
    } finally {
      setLoading(false);
    }
  };

  const ChecklistItem = ({ cumple, texto }) => (
    <div className={`flex items-center text-xs mt-1 ${cumple ? 'text-green-600' : 'text-gray-400'}`}>
      <svg className={`w-4 h-4 mr-1 fill-current`} viewBox="0 0 20 20">
        <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" />
      </svg>
      {texto}
    </div>
  );

  return (
    <div className="min-h-screen bg-clinica-light flex items-center justify-center p-4 font-sans">
      <div className="bg-white max-w-2xl w-full rounded-3xl shadow-xl overflow-hidden my-8 transition-all">

        <div className="p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-clinica-dark tracking-tight">Crear Cuenta</h1>
            <p className="text-gray-500 mt-2 text-sm">Portal de Pacientes</p>
          </div>

          {error && (
            <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm mb-6 text-center border border-red-200">
              {error}
            </div>
          )}

          <form onSubmit={handleRegister} className="space-y-5">
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input type="text" name="nombre" placeholder="Nombre" required value={formData.nombre} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              <input type="text" name="apellido" placeholder="Apellido" required value={formData.apellido} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input type="number" name="dni" placeholder="DNI" required value={formData.dni} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              <input type="date" name="fechaNacimiento" required max={getMaxBirthDateString()} value={formData.fechaNacimiento} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all text-gray-500" />
              <SexoSelectField
                value={formData.sexo}
                onChange={handleChange}
                inputClassName="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all bg-white"
              />
              <p className="text-xs text-gray-500 mt-1 col-span-2">Tenés que tener al menos 18 años.</p>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input type="text" name="telefono" placeholder="Teléfono" required value={formData.telefono} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              <input type="email" name="email" placeholder="Correo Electrónico" required value={formData.email} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-gray-50 p-4 rounded-xl border border-gray-100 space-y-0">
              <ProvinciaLocalidadFields
                provinciaId={provinciaSeleccionada}
                localidadId={formData.idLocalidad}
                onProvinciaChange={(id) => {
                  setProvinciaSeleccionada(id);
                  setFormData((prev) => ({ ...prev, idLocalidad: '', direccion: '' }));
                }}
                onLocalidadChange={(id) => {
                  setFormData((prev) => ({ ...prev, idLocalidad: id, direccion: '' }));
                }}
                inputClassName="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none bg-white"
                className="grid grid-cols-1 md:grid-cols-2 gap-4 col-span-2"
              />
              <input
                type="text"
                name="direccion"
                value={formData.direccion}
                onChange={handleChange}
                required
                disabled={!formData.idLocalidad}
                maxLength={500}
                placeholder="Calle, número, piso/depto (según la localidad elegida)"
                className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none bg-white disabled:bg-gray-100 md:col-span-2 mt-0"
              />
            </div>

            <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña</label>
                  <div className="relative mb-3">
                    <input
                      type={showPassword ? 'text' : 'password'}
                      name="password"
                      required
                      value={formData.password}
                      onChange={handleChange}
                      className="w-full px-4 py-2 pr-12 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all"
                      placeholder="Escribí tu clave..."
                      autoComplete="new-password"
                    />
                    <PasswordVisibilityToggle visible={showPassword} onToggle={() => setShowPassword(!showPassword)} />
                  </div>
                  <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 bg-white p-3 rounded-lg border border-gray-200">
                    <ChecklistItem cumple={validacionesPassword.longitud} texto="8+ caracteres" />
                    <ChecklistItem cumple={validacionesPassword.mayuscula} texto="Mayúscula" />
                    <ChecklistItem cumple={validacionesPassword.minuscula} texto="Minúscula" />
                    <ChecklistItem cumple={validacionesPassword.numero} texto="Número" />
                    <ChecklistItem cumple={validacionesPassword.especial} texto="Símbolo (@#$%^&+=!)" />
                  </div>
                </div>
                <div>
                  <label className="block text-sm font-medium text-gray-700 mb-1">Confirmar contraseña</label>
                  <div className="relative">
                    <input
                      type={showConfirmPassword ? 'text' : 'password'}
                      name="confirmarPassword"
                      required
                      value={formData.confirmarPassword}
                      onChange={handleChange}
                      className={`w-full px-4 py-2 pr-12 rounded-xl border outline-none transition-all focus:ring-2 focus:ring-clinica-dark ${
                        formData.confirmarPassword.length > 0
                          ? passwordsMatch
                            ? 'border-green-500'
                            : 'border-red-500'
                          : 'border-gray-200'
                      }`}
                      placeholder="Repetí la contraseña"
                      autoComplete="new-password"
                    />
                    <PasswordVisibilityToggle
                      visible={showConfirmPassword}
                      onToggle={() => setShowConfirmPassword(!showConfirmPassword)}
                    />
                  </div>
                  {formData.confirmarPassword.length > 0 && (
                    <p className={`text-xs mt-2 font-medium ${passwordsMatch ? 'text-green-600' : 'text-red-600'}`}>
                      {passwordsMatch ? 'Las contraseñas coinciden' : 'Las contraseñas no coinciden'}
                    </p>
                  )}
                </div>
              </div>
            </div>

            <button
              type="submit"
              disabled={
                loading ||
                !isPasswordValid ||
                !passwordsMatch ||
                !formData.idLocalidad ||
                !formData.direccion?.trim() ||
                !formData.idObraSocial
              }
              className="w-full bg-clinica-dark text-white font-semibold py-3 mt-2 rounded-xl hover:bg-clinica-hover transition-all shadow-lg disabled:opacity-50 flex justify-center items-center"
            >
              {loading ? <span className="animate-pulse">Registrando...</span> : 'Confirmar Registro'}
            </button>
          </form>
        </div>

        <div className="bg-gray-50 p-6 text-center border-t border-gray-100">
          <p className="text-sm text-gray-600">
            ¿Ya tenés una cuenta?{' '}
            <Link to={PACIENTE_PATHS.login} className="text-clinica-dark font-semibold hover:underline">
              Iniciá sesión
            </Link>
          </p>
        </div>
      </div>
    </div>
  );
};

export default Register;

