import React, { useState, useEffect } from 'react';
import { useNavigate } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const AdminRegisterSecret = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false);
  
  const [systemKey, setSystemKey] = useState('');
  const [showSystemKey, setShowSystemKey] = useState(false);
  const [showPassword, setShowPassword] = useState(false);
  
  const [provincias, setProvincias] = useState([]);
  const [localidades, setLocalidades] = useState([]);
  const [provinciaSeleccionada, setProvinciaSeleccionada] = useState('');

  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    email: '',
    password: '',
    telefono: '',
    fechaNacimiento: '',
    idLocalidad: ''
  });

  useEffect(() => {
    const fetchProvincias = async () => {
      try {
        const response = await clienteAxios.get('/usuarios/api/provincias');
        setProvincias(response.data);
      } catch (err) {
        setError("Error: No se pudieron cargar las provincias.");
      }
    };
    fetchProvincias();
  }, []);

  useEffect(() => {
    const fetchLocalidades = async () => {
      if (!provinciaSeleccionada) {
        setLocalidades([]);
        return;
      }
      try {
        const response = await clienteAxios.get(`/usuarios/api/localidades/provincia/${provinciaSeleccionada}`);
        setLocalidades(response.data);
      } catch (err) {
        setError("Error al cargar las localidades.");
      }
    };
    fetchLocalidades();
  }, [provinciaSeleccionada]);

  const handleChange = (e) => {
    setFormData({ ...formData, [e.target.name]: e.target.value });
  };

  const handleProvinciaChange = (e) => {
    setProvinciaSeleccionada(e.target.value);
    setFormData({ ...formData, idLocalidad: '' });
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

    setError('');
    setLoading(true);

    try {
      const dni = Number(formData.dni);
      const idLocalidad = Number(formData.idLocalidad);
      if (!Number.isInteger(dni) || !Number.isInteger(idLocalidad)) {
        setError('DNI o localidad inválidos.');
        return;
      }

      const payload = {
        ...formData,
        dni,
        idLocalidad
      };

      const response = await clienteAxios.post('/usuarios/api/administradores/registro', payload, {
        headers: { 
          'X-System-Key': systemKey.trim(),
          'Content-Type': 'application/json'
        }
      });

      // El backend devuelve 201 y dispara el mail de verificación
      if (response.status === 201) {
        setSuccess(true);
        // Damos más tiempo (5s) para que el usuario lea la instrucción del mail
        setTimeout(() => navigate(ADMIN_PATHS.login), 5000);
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
          
          {success && (
            <div className="bg-green-50 text-green-700 p-4 rounded-lg text-sm mb-4 border border-green-200 text-center">
              <p className="font-bold">¡Registro exitoso!</p>
              <p className="mt-1">Hemos enviado un enlace de activación a <strong>{formData.email}</strong>.</p>
              <p className="text-xs mt-2 italic">Debés confirmar tu cuenta antes de poder iniciar sesión.</p>
            </div>
          )}

          <form onSubmit={handleRegister} className={`space-y-4 ${success ? 'opacity-50 pointer-events-none' : ''}`}>
            
            {/* SECCIÓN X-SYSTEM-KEY (Ingreso manual solicitado) */}
            <div className="bg-gray-100 p-4 rounded-lg border border-gray-300">
              <label className="block text-xs font-bold text-gray-700 mb-1 uppercase">X-System-Key</label>
              <div className="relative">
                <input 
                  type={showSystemKey ? "text" : "password"} 
                  required 
                  value={systemKey} 
                  onChange={(e) => setSystemKey(e.target.value)}
                  className="w-full px-4 py-2 pr-10 rounded border border-gray-400 focus:ring-2 focus:ring-red-500 outline-none text-center font-mono"
                  placeholder="Ingrese Clave Maestra"
                />
                <button
                  type="button"
                  onClick={() => setShowSystemKey(!showSystemKey)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-red-600"
                >
                  {showSystemKey ? (
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                  )}
                </button>
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
              <input type="date" name="fechaNacimiento" required value={formData.fechaNacimiento} onChange={handleChange} className="px-3 py-2 border rounded outline-none text-gray-500" />
            </div>

            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <select value={provinciaSeleccionada} onChange={handleProvinciaChange} required className="px-3 py-2 border rounded outline-none bg-white">
                <option value="">Provincia</option>
                {provincias.map(p => <option key={p.idProvincia} value={p.idProvincia}>{p.nombre}</option>)}
              </select>
              <select name="idLocalidad" value={formData.idLocalidad} onChange={handleChange} required disabled={!provinciaSeleccionada} className="px-3 py-2 border rounded outline-none bg-white disabled:bg-gray-100">
                <option value="">Localidad</option>
                {localidades.map(l => <option key={l.idLocalidad} value={l.idLocalidad}>{l.nombre}</option>)}
              </select>
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
                  className="w-full px-3 py-2 pr-10 border rounded outline-none focus:border-red-500"
                  placeholder="Defina su clave"
                />
                <button
                  type="button"
                  onClick={() => setShowPassword(!showPassword)}
                  className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-red-600"
                >
                  {showPassword ? (
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                  ) : (
                    <svg xmlns="http://www.w3.org/2000/svg" width="18" height="18" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                  )}
                </button>
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
              disabled={loading || !isPasswordValid || !systemKey || success}
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