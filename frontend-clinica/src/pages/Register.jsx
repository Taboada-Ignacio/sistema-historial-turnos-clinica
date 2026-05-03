import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import clienteAxios from '../api/axiosConfig'; // Usamos nuestra instancia configurada

const Register = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  
  // --- ESTADOS PARA SELECTS DINÁMICOS ---
  const [provincias, setProvincias] = useState([]);
  const [localidades, setLocalidades] = useState([]);
  const [provinciaSeleccionada, setProvinciaSeleccionada] = useState('');

  const [formData, setFormData] = useState({
    nombre: '',
    apellido: '',
    dni: '',
    email: '',
    telefono: '',
    fechaNacimiento: '',
    password: '',
    idLocalidad: '' // Agregamos idLocalidad al estado inicial
  });

  // --- CARGAR PROVINCIAS AL INICIAR ---
  useEffect(() => {
    const fetchProvincias = async () => {
      try {
        // Usamos la URL que mostraste en Postman
        const response = await clienteAxios.get('/usuarios/api/provincias');
        setProvincias(response.data);
      } catch (err) {
        console.error("Error al cargar provincias:", err);
        setError("No se pudieron cargar las provincias.");
      }
    };
    fetchProvincias();
  }, []);

  // --- CARGAR LOCALIDADES CUANDO CAMBIA LA PROVINCIA ---
  useEffect(() => {
    const fetchLocalidades = async () => {
      if (!provinciaSeleccionada) {
        setLocalidades([]); // Limpiamos si no hay provincia
        return;
      }
      
      try {
        // Usamos la URL dinámica según la provincia elegida
        const response = await clienteAxios.get(`/usuarios/api/localidades/provincia/${provinciaSeleccionada}`);
        setLocalidades(response.data);
      } catch (err) {
        console.error("Error al cargar localidades:", err);
        setError("No se pudieron cargar las localidades de esa provincia.");
      }
    };

    fetchLocalidades();
  }, [provinciaSeleccionada]);


  const handleChange = (e) => {
    setFormData({
      ...formData,
      [e.target.name]: e.target.value
    });
  };

  const handleProvinciaChange = (e) => {
    setProvinciaSeleccionada(e.target.value);
    // Reseteamos la localidad seleccionada al cambiar de provincia
    setFormData({ ...formData, idLocalidad: '' }); 
  };

  // --- VALIDACIÓN DE CONTRASEÑA EN TIEMPO REAL ---
  const validacionesPassword = {
    longitud: formData.password.length >= 8,
    mayuscula: /[A-Z]/.test(formData.password),
    minuscula: /[a-z]/.test(formData.password),
    numero: /[0-9]/.test(formData.password),
    especial: /[@#$%^&+=!]/.test(formData.password)
  };

  const isPasswordValid = Object.values(validacionesPassword).every(Boolean);


  const handleRegister = async (e) => {
    e.preventDefault();
    
    // Doble check de seguridad antes de enviar
    if (!isPasswordValid) {
      setError("La contraseña no cumple con los requisitos de seguridad.");
      return;
    }

    if (!formData.idLocalidad) {
      setError("Debes seleccionar una provincia y una localidad.");
      return;
    }

    setError('');
    setLoading(true);

    try {
      const payload = {
        ...formData,
        dni: parseInt(formData.dni),
        idLocalidad: parseInt(formData.idLocalidad),
        rolesIds: [2] 
      };

      const response = await clienteAxios.post('/usuarios/api/pacientes/registro', payload);

      if (response.status === 201 || response.status === 200) {
        console.log('¡Registro exitoso!');
        navigate('/');
      }
    } catch (err) {
       if (err.response && err.response.data && err.response.data.message) {
         setError(`Error: ${err.response.data.message}`);
       } else {
         setError('Ocurrió un error al registrar el usuario. Verifica los datos e intenta nuevamente.');
       }
    } finally {
      setLoading(false);
    }
  };

  // --- COMPONENTE VISUAL PARA EL CHECKLIST ---
  const ChecklistItem = ({ cumple, texto }) => (
    <div className={`flex items-center text-xs mt-1 ${cumple ? 'text-green-600' : 'text-gray-500'}`}>
      <svg className={`w-4 h-4 mr-1 ${cumple ? 'fill-current' : 'text-gray-300 fill-current'}`} viewBox="0 0 20 20">
        <path d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" />
      </svg>
      {texto}
    </div>
  );

  return (
    <div className="min-h-screen bg-clinica-light flex items-center justify-center p-4 font-sans">
      <div className="bg-white max-w-2xl w-full rounded-3xl shadow-xl overflow-hidden my-8">
        
        <div className="p-8">
          <div className="text-center mb-8">
            <h1 className="text-3xl font-bold text-clinica-dark tracking-tight">Crear Cuenta</h1>
            <p className="text-gray-500 mt-2 text-sm">Ingresa tus datos personales para registrarte como paciente</p>
          </div>

          {error && (
             <div className="bg-red-50 text-red-600 p-3 rounded-lg text-sm mb-6 text-center border border-red-200">
               {error}
             </div>
          )}

          <form onSubmit={handleRegister} className="space-y-5">
            {/* Fila 1: Nombre y Apellido */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Nombre</label>
                <input type="text" name="nombre" required value={formData.nombre} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Apellido</label>
                <input type="text" name="apellido" required value={formData.apellido} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              </div>
            </div>

            {/* Fila 2: DNI y Fecha de Nacimiento */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">DNI</label>
                <input type="number" name="dni" required value={formData.dni} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Fecha de Nacimiento</label>
                <input type="date" name="fechaNacimiento" required value={formData.fechaNacimiento} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              </div>
            </div>

            {/* Fila 3: Teléfono y Correo */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Teléfono</label>
                <input type="text" name="telefono" required value={formData.telefono} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              </div>
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Correo Electrónico</label>
                <input type="email" name="email" required value={formData.email} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              </div>
            </div>

            {/* Fila 4: SELECTS DINÁMICOS (Provincia y Localidad) */}
            <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-gray-50 p-4 rounded-xl border border-gray-100">
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Provincia</label>
                <select 
                  value={provinciaSeleccionada} 
                  onChange={handleProvinciaChange} 
                  required
                  className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all bg-white"
                >
                  <option value="">Seleccione una provincia...</option>
                  {provincias.map(prov => (
                    <option key={prov.idProvincia} value={prov.idProvincia}>
                      {prov.nombre}
                    </option>
                  ))}
                </select>
              </div>
              
              <div>
                <label className="block text-sm font-medium text-gray-700 mb-1">Localidad</label>
                <select 
                  name="idLocalidad" 
                  value={formData.idLocalidad} 
                  onChange={handleChange} 
                  required
                  disabled={!provinciaSeleccionada || localidades.length === 0}
                  className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all bg-white disabled:bg-gray-100 disabled:text-gray-400"
                >
                  <option value="">Seleccione una localidad...</option>
                  {localidades.map(loc => (
                    <option key={loc.idLocalidad} value={loc.idLocalidad}>
                      {loc.nombre}
                    </option>
                  ))}
                </select>
              </div>
            </div>

            {/* Fila 5: Contraseña y Checklist */}
            <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
              <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña Segura</label>
              <input 
                type="password" 
                name="password" 
                required 
                value={formData.password} 
                onChange={handleChange} 
                className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all mb-3" 
                placeholder="Escribe tu contraseña..."
              />
              
              {/* Contenedor del Checklist */}
              <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 bg-white p-3 rounded-lg border border-gray-200">
                <ChecklistItem cumple={validacionesPassword.longitud} texto="Mínimo 8 caracteres" />
                <ChecklistItem cumple={validacionesPassword.mayuscula} texto="Una letra mayúscula" />
                <ChecklistItem cumple={validacionesPassword.minuscula} texto="Una letra minúscula" />
                <ChecklistItem cumple={validacionesPassword.numero} texto="Un número" />
                <ChecklistItem cumple={validacionesPassword.especial} texto="Un carácter especial (@#$%^&+=!)" />
              </div>
            </div>

            <button
              type="submit"
              disabled={loading || !isPasswordValid}
              className="w-full bg-clinica-dark text-white font-semibold py-3 mt-2 rounded-xl hover:bg-clinica-hover transition-colors shadow-lg shadow-clinica-dark/30 disabled:opacity-50 disabled:cursor-not-allowed flex justify-center items-center"
            >
              {loading ? <span className="animate-pulse">Registrando...</span> : "Confirmar Registro"}
            </button>
          </form>
        </div>
        
        <div className="bg-gray-50 p-6 text-center border-t border-gray-100">
          <p className="text-sm text-gray-600">
            ¿Ya tienes una cuenta?{' '}
            <Link to="/" className="text-clinica-dark font-semibold hover:underline">
              Inicia sesión aquí
            </Link>
          </p>
        </div>

      </div>
    </div>
  );
};

export default Register;