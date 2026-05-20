import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import PasswordVisibilityToggle from '../../components/PasswordVisibilityToggle';
import { getMaxBirthDateString, isAtLeastAge } from '../../utils/ageValidation';
import { ADMIN_PATHS } from '../../utils/portalPaths';
import ProvinciaLocalidadFields from '../../components/ProvinciaLocalidadFields';

const AdminRegisterSecret = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  const [systemKey, setSystemKey] = useState('');
  const [showSystemKey, setShowSystemKey] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  
  const [provinciaSeleccionada, setProvinciaSeleccionada] = useState('');

  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    email: '',
    password: '',
    telefono: '',
    fechaNacimiento: '',
    idLocalidad: '',
    direccion: ''
  });

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData((prev) => ({
      ...prev,
      [name]: value,
      ...(name === 'idLocalidad' ? { direccion: '' } : {})
    }));
  };

  const validaciones = {
    longitud: formData.password.length >= 8,
    mayuscula: /[A-Z]/.test(formData.password),
    minuscula: /[a-z]/.test(formData.password),
    numero: /[0-9]/.test(formData.password),
    especial: /[@#$%^&+=!]/.test(formData.password)
  };

  const isPasswordValid = Object.values(validaciones).every(Boolean);

  const handleRegister = async (e) => {
    e.preventDefault();
    if (!isPasswordValid) {
      setError('La contraseña no cumple los requisitos de seguridad.');
      return;
    }
    if (!systemKey.trim()) {
      setError('Debes ingresar la X-System-Key para continuar.');
      return;
    }
    if (!isAtLeastAge(formData.fechaNacimiento)) {
      setError('Debés ser mayor de 18 años.');
      return;
    }

    setError('');
    setLoading(true);

    try {
      const dni = Number(formData.dni);
      const idLocalidad = Number(formData.idLocalidad);
      if (!Number.isInteger(dni) || !Number.isInteger(idLocalidad) || !formData.direccion?.trim()) {
        setError('DNI, localidad o dirección inválidos.');
        return;
      }

      const payload = {
        ...formData,
        dni,
        idLocalidad,
        direccion: formData.direccion.trim()
      };

      const response = await clienteAxios.post('/usuarios/api/administradores/registro', payload, {
        headers: { 
          'X-System-Key': systemKey.trim(),
          'Content-Type': 'application/json'
        }
      });

      if (response.status === 201) {
        navigate(ADMIN_PATHS.verificarEmail, { state: { email: formData.email } });
      }
    } catch (err) {
      if (err.response && err.response.status === 403) {
        setError('Acceso denegado: X-System-Key incorrecta o inválida.');
      } else {
        setError(err.response?.data?.message || 'Error en el registro. Verificá si el email ya existe.');
      }
    } finally {
      setLoading(false);
    }
  };

  const ChecklistItem = ({ cumple, texto }) => (
    <div className={`flex items-center text-xs ${cumple ? 'text-green-500' : 'text-gray-400'}`}>
      <svg className="w-3 h-3 mr-1 fill-current" viewBox="0 0 20 20">
        <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" />
      </svg>
      {texto}
    </div>
  );

  return (
    <div className="min-h-screen bg-gray-900 flex items-center justify-center p-4 font-sans">
      <div className="bg-white max-w-2xl w-full rounded-xl shadow-2xl border-t-4 border-red-600 overflow-hidden">
        <div className="p-8">
          <div className="text-center mb-6">
            <h1 className="text-2xl font-bold text-gray-900 uppercase tracking-wider">Setup de Administrador</h1>
            <p className="text-red-600 text-xs font-bold uppercase tracking-tight">Registro Maestro de Sistema</p>
          </div>

          {error && (
            <div className="bg-red-50 text-red-700 p-3 rounded-lg text-xs mb-4 border border-red-200 animate-shake">
              {error}
            </div>
          )}
          
          <form onSubmit={handleRegister} className="space-y-4">
            
            {/* SECCIÓN X-SYSTEM-KEY (Ingreso manual solicitado) */}
            <div className="bg-gray-100 p-4 rounded-lg border border-gray-300">
              <label className="block text-xs font-bold text-gray-700 mb-1 uppercase">X-System-Key</label>
              <div className="relative">
                <input 
                  type={showSystemKey ? "text" : "password"} 
                  required 
                  value={systemKey} 
                  onChange={(e) => setSystemKey(e.target.value)}
                  className="w-full px-4 py-2 pr-12 rounded border border-gray-400 focus:ring-2 focus:ring-red-500 outline-none text-center font-mono"
                  placeholder="Ingrese Clave Maestra"
                />
                <PasswordVisibilityToggle visible={showSystemKey} onToggle={() => setShowSystemKey(!showSystemKey)} />
              </div>
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input type="text" name="nombre" placeholder="Nombre" required value={formData.nombre} onChange={handleChange} className="px-3 py-2 border rounded outline-none focus:border-gray-400" />
              <input type="text" name="apellido" placeholder="Apellido" required value={formData.apellido} onChange={handleChange} className="px-3 py-2 border rounded outline-none focus:border-gray-400" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input type="number" name="dni" placeholder="DNI" required value={formData.dni} onChange={handleChange} className="px-3 py-2 border rounded outline-none focus:border-gray-400" />
              <input type="text" name="telefono" placeholder="Teléfono" required value={formData.telefono} onChange={handleChange} className="px-3 py-2 border rounded outline-none focus:border-gray-400" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <input type="email" name="email" placeholder="Email" required value={formData.email} onChange={handleChange} className="px-3 py-2 border rounded outline-none focus:border-gray-400" />
              <input type="date" name="fechaNacimiento" required max={getMaxBirthDateString()} value={formData.fechaNacimiento} onChange={handleChange} className="px-3 py-2 border rounded outline-none text-gray-500" title="Mayor de 18 años" />
            </div>

            <div className="space-y-4">
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
                inputClassName="px-3 py-2 border rounded outline-none bg-white w-full"
              />
              <input
                type="text"
                name="direccion"
                value={formData.direccion}
                onChange={handleChange}
                required
                disabled={!formData.idLocalidad}
                maxLength={500}
                placeholder="Calle, número, piso/depto"
                className="px-3 py-2 border rounded outline-none bg-white disabled:bg-gray-100 md:col-span-2"
              />
            </div>

            <div className="bg-gray-50 p-4 rounded-lg border">
              <label className="block text-xs font-bold text-gray-600 mb-1 uppercase">Contraseña</label>
              <div className="relative mb-3">
                <input 
                  type={showPassword ? "text" : "password"} 
                  name="password" 
                  required 
                  value={formData.password} 
                  onChange={handleChange}
                  className="w-full px-3 py-2 pr-12 border rounded outline-none focus:border-red-500"
                  placeholder="Defina su clave"
                />
                <PasswordVisibilityToggle visible={showPassword} onToggle={() => setShowPassword(!showPassword)} />
              </div>
              
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-1">
                <ChecklistItem cumple={validaciones.longitud} texto="8+ caracteres" />
                <ChecklistItem cumple={validaciones.mayuscula} texto="Mayúscula" />
                <ChecklistItem cumple={validaciones.minuscula} texto="Minúscula" />
                <ChecklistItem cumple={validaciones.numero} texto="Número" />
                <ChecklistItem cumple={validaciones.especial} texto="Especial" />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading || !isPasswordValid || !systemKey || !formData.idLocalidad || !formData.direccion?.trim()}
              className="w-full bg-red-600 text-white font-bold py-3 mt-4 rounded hover:bg-red-700 transition-colors disabled:opacity-50 uppercase tracking-wide"
            >
              {loading ? "Sincronizando..." : "Ejecutar Alta Administrativa"}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};

export default AdminRegisterSecret;