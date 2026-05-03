import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import clienteAxios from '../api/axiosConfig';

const Register = () => {
  const navigate = useNavigate();
  const [loading, setLoading] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState(false); // Estado para el flujo de mail
  
  const [showPassword, setShowPassword] = useState(false);
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
    idLocalidad: ''
  });

  useEffect(() => {
    const fetchProvincias = async () => {
      try {
        const response = await clienteAxios.get('/usuarios/api/provincias');
        setProvincias(response.data);
      } catch (err) {
        setError("No se pudieron cargar las provincias.");
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
        setError("Error al cargar localidades.");
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
    
    if (!isPasswordValid || !formData.idLocalidad) return;

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
        setSuccess(true);
        // No redirigimos inmediatamente para que el usuario lea la instrucción del mail
      }
    } catch (err) {
       setError(err.response?.data?.message || 'Error al registrar el usuario.');
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

          {/* VISTA DE ÉXITO (MAIL ENVIADO) */}
          {success ? (
            <div className="text-center py-10 animate-fade-in">
              <div className="bg-green-100 text-green-700 p-6 rounded-2xl mb-6">
                <svg className="w-16 h-16 mx-auto mb-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                  <path strokeLinecap="round" strokeLinejoin="round" strokeWidth="2" d="M3 8l7.89 5.26a2 2 0 002.22 0L21 8M5 19h14a2 2 0 002-2V7a2 2 0 00-2-2H5a2 2 0 00-2 2v10a2 2 0 002 2z" />
                </svg>
                <h2 className="text-xl font-bold mb-2">¡Casi listo!</h2>
                <p>Enviamos un enlace de activación a <strong>{formData.email}</strong>.</p>
                <p className="text-sm mt-2 opacity-80">Por favor, verificá tu correo para activar tu cuenta y poder ingresar.</p>
              </div>
              <Link to="/" className="text-clinica-dark font-bold hover:underline">Volver al Inicio</Link>
            </div>
          ) : (
            <form onSubmit={handleRegister} className="space-y-5">
              {/* DATOS PERSONALES */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <input type="text" name="nombre" placeholder="Nombre" required value={formData.nombre} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
                <input type="text" name="apellido" placeholder="Apellido" required value={formData.apellido} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <input type="number" name="dni" placeholder="DNI" required value={formData.dni} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
                <input type="date" name="fechaNacimiento" required value={formData.fechaNacimiento} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all text-gray-500" />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
                <input type="text" name="telefono" placeholder="Teléfono" required value={formData.telefono} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
                <input type="email" name="email" placeholder="Correo Electrónico" required value={formData.email} onChange={handleChange} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" />
              </div>

              {/* UBICACIÓN DINÁMICA */}
              <div className="grid grid-cols-1 md:grid-cols-2 gap-4 bg-gray-50 p-4 rounded-xl border border-gray-100">
                <select value={provinciaSeleccionada} onChange={handleProvinciaChange} required className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none bg-white">
                  <option value="">Provincia</option>
                  {provincias.map(p => <option key={p.idProvincia} value={p.idProvincia}>{p.nombre}</option>)}
                </select>
                <select name="idLocalidad" value={formData.idLocalidad} onChange={handleChange} required disabled={!provinciaSeleccionada} className="w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none bg-white disabled:bg-gray-100">
                  <option value="">Localidad</option>
                  {localidades.map(l => <option key={l.idLocalidad} value={l.idLocalidad}>{l.nombre}</option>)}
                </select>
              </div>

              {/* CONTRASEÑA CON OJO Y CHECKLIST */}
              <div className="bg-gray-50 p-4 rounded-xl border border-gray-100">
                <label className="block text-sm font-medium text-gray-700 mb-1">Contraseña Segura</label>
                <div className="relative mb-3">
                  <input 
                    type={showPassword ? "text" : "password"} 
                    name="password" required value={formData.password} onChange={handleChange} 
                    className="w-full px-4 py-2 pr-10 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none transition-all" 
                    placeholder="Escribí tu clave..."
                  />
                  <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-3 top-1/2 -translate-y-1/2 text-gray-400 hover:text-clinica-dark">
                    {showPassword ? (
                      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M17.94 17.94A10.07 10.07 0 0 1 12 20c-7 0-11-8-11-8a18.45 18.45 0 0 1 5.06-5.94M9.9 4.24A9.12 9.12 0 0 1 12 4c7 0 11 8 11 8a18.5 18.5 0 0 1-2.16 3.19m-6.72-1.07a3 3 0 1 1-4.24-4.24"></path><line x1="1" y1="1" x2="23" y2="23"></line></svg>
                    ) : (
                      <svg xmlns="http://www.w3.org/2000/svg" width="20" height="20" viewBox="0 0 24 24" fill="none" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"><path d="M1 12s4-8 11-8 11 8 11 8-4 8-11 8-11-8-11-8z"></path><circle cx="12" cy="12" r="3"></circle></svg>
                    )}
                  </button>
                </div>
                
                <div className="grid grid-cols-1 sm:grid-cols-2 gap-x-4 bg-white p-3 rounded-lg border border-gray-200">
                  <ChecklistItem cumple={validacionesPassword.longitud} texto="8+ caracteres" />
                  <ChecklistItem cumple={validacionesPassword.mayuscula} texto="Mayúscula" />
                  <ChecklistItem cumple={validacionesPassword.minuscula} texto="Minúscula" />
                  <ChecklistItem cumple={validacionesPassword.numero} texto="Número" />
                  <ChecklistItem cumple={validacionesPassword.especial} texto="Símbolo (@#$%^&+=!)" />
                </div>
              </div>

              <button
                type="submit"
                disabled={loading || !isPasswordValid || !formData.idLocalidad}
                className="w-full bg-clinica-dark text-white font-semibold py-3 mt-2 rounded-xl hover:bg-clinica-hover transition-all shadow-lg disabled:opacity-50 flex justify-center items-center"
              >
                {loading ? <span className="animate-pulse">Registrando...</span> : "Confirmar Registro"}
              </button>
            </form>
          )}
        </div>
        
        {!success && (
          <div className="bg-gray-50 p-6 text-center border-t border-gray-100">
            <p className="text-sm text-gray-600">
              ¿Ya tenés una cuenta? <Link to="/" className="text-clinica-dark font-semibold hover:underline">Iniciá sesión</Link>
            </p>
          </div>
        )}

      </div>
    </div>
  );
};

export default Register;