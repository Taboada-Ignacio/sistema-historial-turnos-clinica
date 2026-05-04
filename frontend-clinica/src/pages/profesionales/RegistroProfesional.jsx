import React, { useState, useEffect } from 'react';
import { useNavigate, Link } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig'; 
import imageCompression from 'browser-image-compression'; 

const RegistroProfesional = () => {
  const navigate = useNavigate();
  const [step, setStep] = useState(1);
  const [loading, setLoading] = useState(false);
  
  const [showPassword, setShowPassword] = useState(false);
  const [showConfirmPassword, setShowConfirmPassword] = useState(false);

  const [formData, setFormData] = useState({
    nombre: '', apellido: '', dni: '', telefono: '', fechaNacimiento: '',
    provincia: '', localidad: '', especialidad: '', matricula: '',
    email: '', password: '', confirmarPassword: '', foto: null
  });

  const [especialidades, setEspecialidades] = useState([]);
  const [provincias, setProvincias] = useState([]);
  const [localidades, setLocalidades] = useState([]);
  const [rolesIds, setRolesIds] = useState([]); 
  const [errorFoto, setErrorFoto] = useState('');

  // 1. CARGA INICIAL
  useEffect(() => {
    const cargarDatosIniciales = async () => {
      // [DEBUG] Iniciando carga de datos maestros
      console.log("%c[DEBUG] Cargando datos iniciales (Roles, Provincias, Especialidades)...", "color: blue; font-weight: bold;");
      
      try {
        const [resEsp, resProv, resRoles] = await Promise.all([
          clienteAxios.get('/usuarios/api/especialidades'),
          clienteAxios.get('/usuarios/api/provincias'),
          clienteAxios.get('/usuarios/api/roles')
        ]);
        
        setEspecialidades(resEsp.data);
        setProvincias(resProv.data);

        const ids = resRoles.data
          .filter(r => r.descripcion === 'ROLE_PROFESIONAL' || r.descripcion === 'ROLE_PACIENTE')
          .map(r => r.idRol);
        
        setRolesIds(ids);
        console.log("[DEBUG] Roles asignados automáticamente:", ids);

      } catch (error) {
        console.error("[DEBUG] Error en carga inicial:", error);
      }
    };
    cargarDatosIniciales();
  }, []);

  // 2. LOCALIDADES
  useEffect(() => {
    if (formData.provincia) {
      console.log(`[DEBUG] Buscando localidades para Provincia ID: ${formData.provincia}`);
      clienteAxios.get(`/usuarios/api/localidades/provincia/${formData.provincia}`)
        .then(res => setLocalidades(res.data))
        .catch(err => console.error("[DEBUG] Error cargando localidades:", err));
    }
  }, [formData.provincia]);

  const handleChange = (e) => {
    const { name, value } = e.target;
    setFormData(prev => ({ 
      ...prev, 
      [name]: value,
      ...(name === 'provincia' && { localidad: '' }) 
    }));
  };

  // 3. FOTO: Compresión y cambio de extensión
  const handleFileChange = async (e) => {
    const file = e.target.files[0];
    setErrorFoto('');
    if (!file) return;

    console.log(`%c[DEBUG] Archivo original: ${file.name} | Tipo: ${file.type}`, "color: orange;");

    if (!file.type.includes('jpeg') && !file.name.toLowerCase().endsWith('.jpg')) {
      setErrorFoto('Solo se permiten imágenes en formato JPG.');
      return;
    }

    const options = {
      maxSizeMB: 1,
      maxWidthOrHeight: 1024,
      useWebWorker: true,
      fileType: 'image/webp'
    };

    try {
      console.log("[DEBUG] Comprimiendo y convirtiendo a WebP...");
      const compressedBlob = await imageCompression(file, options);
      
      const fileName = file.name.split('.')[0] + "_" + Date.now() + ".webp";
      const webpFile = new File([compressedBlob], fileName, { type: 'image/webp' });

      console.log(`%c[DEBUG] NUEVO ARCHIVO LISTO: ${webpFile.name}`, "color: green; font-weight: bold;");
      setFormData(prev => ({ ...prev, foto: webpFile }));
    } catch (err) {
      console.error("[DEBUG] Error procesando imagen:", err);
      setErrorFoto('Error al optimizar la imagen.');
    }
  };

  // Validaciones
  const reqLength = formData.password.length >= 8;
  const reqUpper = /[A-Z]/.test(formData.password);
  const reqNumber = /[0-9]/.test(formData.password);
  const reqSpecial = /[^A-Za-z0-9]/.test(formData.password);
  const isPasswordValid = reqLength && reqUpper && reqNumber && reqSpecial;
  const passwordsMatch = formData.password === formData.confirmarPassword && formData.password !== '';
  
  // FUNCIONES DEL STEPPER (Las que faltaban)
  const nextStep = () => setStep(step + 1);
  const prevStep = () => setStep(step - 1);

  const isFormValid = isPasswordValid && passwordsMatch && formData.foto && formData.fechaNacimiento;

  // 4. ENVÍO
  const handleSubmit = async (e) => {
    e.preventDefault();
    setLoading(true);

    console.log("%c[DEBUG] Iniciando envío de Form Data...", "color: purple; font-weight: bold;");

    try {
      const dataToSend = new FormData();
      const dto = {
        nombre: formData.nombre,
        apellido: formData.apellido,
        dni: Number(formData.dni),
        email: formData.email,
        password: formData.password,
        telefono: formData.telefono,
        fechaNacimiento: formData.fechaNacimiento,
        matricula: formData.matricula,
        idLocalidad: Number(formData.localidad),
        idEspecialidad: Number(formData.especialidad),
        rolesIds: rolesIds
      };

      console.log("[DEBUG] DTO a enviar:", dto);

      dataToSend.append('datos', new Blob([JSON.stringify(dto)], { type: 'application/json' }));
      dataToSend.append('foto', formData.foto);

      console.log("[DEBUG] Foto en el FormData:", dataToSend.get('foto').name);

      const response = await clienteAxios.post('/usuarios/api/profesionales/registro', dataToSend);
      console.log("[DEBUG] Respuesta exitosa:", response.data);
      
      navigate('/verificar-email-profesional', { state: { email: formData.email } });

    } catch (error) {
      console.error("%c[DEBUG] ERROR EN EL POST:", "color: red; font-weight: bold;");
      if (error.response) {
        console.log("[DEBUG] Status:", error.response.status);
        console.log("[DEBUG] Detalle:", error.response.data);
      }
      alert("Error al registrar. Revisá los logs de la consola.");
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-[#eef5ff] flex flex-col font-sans py-12 px-4 sm:px-6 lg:px-8">
      
      <div className="max-w-3xl w-full mx-auto mb-8">
        <Link to="/login-profesional" className="text-blue-600 font-bold text-sm hover:underline flex items-center gap-1 mb-6">
          ← Volver al login
        </Link>
        <h2 className="text-3xl font-black text-gray-900">Solicitud de Alta Profesional</h2>
        <p className="text-gray-500 mt-2">Completá tus datos para unirte a nuestro staff de profesionales.</p>
      </div>

      <div className="max-w-3xl w-full mx-auto bg-white rounded-[2rem] shadow-xl overflow-hidden border border-gray-100">
        
        {/* Barra de Progreso */}
        <div className="bg-gray-50 px-8 py-6 border-b border-gray-100">
          <div className="flex justify-between items-center mb-2">
            <span className="text-sm font-bold text-gray-500 uppercase">Paso {step} de 3</span>
            <span className="text-sm font-black text-blue-600 uppercase tracking-tight">
              {step === 1 && "Datos Personales"}
              {step === 2 && "Datos Profesionales"}
              {step === 3 && "Seguridad y Perfil"}
            </span>
          </div>
          <div className="w-full bg-gray-200 rounded-full h-2">
            <div className="bg-blue-600 h-2 rounded-full transition-all duration-500" style={{ width: `${(step/3)*100}%` }}></div>
          </div>
        </div>

        <form onSubmit={step === 3 ? handleSubmit : (e) => { e.preventDefault(); nextStep(); }} className="p-8">
          
          {step === 1 && (
            <div className="grid grid-cols-1 md:grid-cols-2 gap-6 animate-fadeIn">
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Nombre</label>
                <input type="text" name="nombre" required value={formData.nombre} onChange={handleChange} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Apellido</label>
                <input type="text" name="apellido" required value={formData.apellido} onChange={handleChange} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">DNI</label>
                <input type="number" name="dni" required value={formData.dni} onChange={handleChange} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Teléfono</label>
                <input type="tel" name="telefono" required value={formData.telefono} onChange={handleChange} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Fecha de Nacimiento</label>
                <input type="date" name="fechaNacimiento" required value={formData.fechaNacimiento} onChange={handleChange} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all" />
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Provincia</label>
                <select name="provincia" required value={formData.provincia} onChange={handleChange} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50">
                  <option value="">Seleccione provincia</option>
                  {provincias.map(p => <option key={p.idProvincia} value={p.idProvincia}>{p.nombre}</option>)}
                </select>
              </div>
              <div className="md:col-span-2">
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Localidad</label>
                <select name="localidad" required value={formData.localidad} onChange={handleChange} disabled={!formData.provincia} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 disabled:opacity-50">
                  <option value="">Seleccione localidad</option>
                  {localidades.map(l => <option key={l.idLocalidad} value={l.idLocalidad}>{l.nombre}</option>)}
                </select>
              </div>
            </div>
          )}

          {step === 2 && (
            <div className="space-y-6 animate-fadeIn">
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Especialidad Principal</label>
                <select name="especialidad" required value={formData.especialidad} onChange={handleChange} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all">
                  <option value="">Seleccione especialidad</option>
                  {especialidades.map(e => <option key={e.idEspecialidad} value={e.idEspecialidad}>{e.descripcion}</option>)}
                </select>
              </div>
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Matrícula Médica</label>
                <input type="text" name="matricula" required value={formData.matricula} onChange={handleChange} placeholder="Ej: MN 12345" className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all" />
              </div>
            </div>
          )}

          {step === 3 && (
            <div className="space-y-6 animate-fadeIn">
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Foto de Perfil (JPG)</label>
                <input type="file" accept=".jpg,.jpeg" required onChange={handleFileChange} className={`w-full px-4 py-3 rounded-xl border file:mr-4 file:py-2 file:px-4 file:rounded-full file:border-0 file:text-sm file:font-bold file:bg-blue-50 file:text-blue-700 bg-gray-50 ${errorFoto ? 'border-red-500' : 'border-gray-200'}`} />
                {errorFoto && <p className="text-red-500 text-xs mt-1 font-bold">{errorFoto}</p>}
              </div>
              
              <div>
                <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Email personal / profesional</label>
                <input type="email" name="email" required value={formData.email} onChange={handleChange} className="w-full px-4 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all" />
              </div>

              <div className="grid grid-cols-1 md:grid-cols-2 gap-6">
                <div>
                  <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Contraseña</label>
                  <div className="relative">
                    <input type={showPassword ? "text" : "password"} name="password" required value={formData.password} onChange={handleChange} className="w-full pl-4 pr-12 py-3 rounded-xl border border-gray-200 focus:ring-2 focus:ring-blue-500 outline-none bg-gray-50 transition-all" />
                    <button type="button" onClick={() => setShowPassword(!showPassword)} className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-blue-600 transition-colors">
                      {showPassword ? "👁️‍🗨️" : "👁️"}
                    </button>
                  </div>
                  <div className="mt-4 bg-gray-50 p-4 rounded-xl border border-gray-100">
                    <ul className="space-y-1">
                      <li className={`text-[10px] flex items-center gap-1 ${reqLength ? 'text-green-600 font-bold' : 'text-gray-400'}`}>✓ 8+ caracteres</li>
                      <li className={`text-[10px] flex items-center gap-1 ${reqUpper ? 'text-green-600 font-bold' : 'text-gray-400'}`}>✓ 1 Mayúscula</li>
                      <li className={`text-[10px] flex items-center gap-1 ${reqNumber ? 'text-green-600 font-bold' : 'text-gray-400'}`}>✓ 1 Número</li>
                      <li className={`text-[10px] flex items-center gap-1 ${reqSpecial ? 'text-green-600 font-bold' : 'text-gray-400'}`}>✓ 1 Especial</li>
                    </ul>
                  </div>
                </div>
                <div>
                  <label className="block text-xs font-bold text-gray-400 uppercase mb-2">Confirmar</label>
                  <div className="relative">
                    <input type={showConfirmPassword ? "text" : "password"} name="confirmarPassword" required value={formData.confirmarPassword} onChange={handleChange} className={`w-full pl-4 pr-12 py-3 rounded-xl border outline-none bg-gray-50 transition-all ${formData.confirmarPassword.length > 0 ? (passwordsMatch ? 'border-green-500' : 'border-red-500') : 'border-gray-200'}`} />
                    <button type="button" onClick={() => setShowConfirmPassword(!showConfirmPassword)} className="absolute right-4 top-1/2 -translate-y-1/2 text-gray-400 hover:text-blue-600 transition-colors">
                      {showConfirmPassword ? "👁️‍🗨️" : "👁️"}
                    </button>
                  </div>
                </div>
              </div>
            </div>
          )}

          <div className="mt-10 flex justify-between pt-6 border-t border-gray-100">
            {step > 1 ? (
              <button type="button" onClick={prevStep} disabled={loading} className="px-6 py-3 text-gray-500 font-bold hover:bg-gray-100 rounded-xl transition-colors">Volver</button>
            ) : <div />}
            
            {step < 3 ? (
              <button type="submit" className="px-8 py-3 bg-blue-50 text-blue-700 font-black rounded-xl border border-blue-200 hover:bg-blue-100 transition-all">Siguiente Paso</button>
            ) : (
              <button 
                type="submit" 
                disabled={!isFormValid || loading}
                className={`px-8 py-3 font-bold rounded-xl transition-all ${isFormValid && !loading ? 'bg-blue-600 text-white shadow-lg shadow-blue-600/30 hover:bg-blue-700 cursor-pointer' : 'bg-gray-300 text-gray-500 cursor-not-allowed'}`}
              >
                {loading ? 'Procesando...' : 'Finalizar Solicitud'}
              </button>
            )}
          </div>
        </form>
      </div>
    </div>
  );
};

export default RegistroProfesional;