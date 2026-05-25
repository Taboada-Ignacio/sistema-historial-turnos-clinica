import React, {
  createContext,
  useCallback,
  useContext,
  useEffect,
  useRef,
  useState,
} from 'react';
import clienteAxios from '../api/axiosConfig';

const ProfesionalSessionContext = createContext(null);

export const ProfesionalSessionProvider = ({ children }) => {
  const [presentacion, setPresentacion] = useState(null);
  const [membresiaActual, setMembresiaActual] = useState(null);
  const [idUsuario, setIdUsuario] = useState(null);
  const [ubicacion, setUbicacion] = useState(null);
  const [cargandoPerfil, setCargandoPerfil] = useState(true);
  const [error, setError] = useState(null);
  const cacheListoRef = useRef(false);

  const cargarPresentacion = useCallback(async (force = false) => {
    if (cacheListoRef.current && !force) return;

    const esPrimeraCarga = !cacheListoRef.current;
    if (esPrimeraCarga) setCargandoPerfil(true);
    setError(null);

    try {
      const { data: me } = await clienteAxios.get('/usuarios/api/profesionales/me');
      setMembresiaActual(me?.membresiaActual ?? null);
      setIdUsuario(me?.idUsuario ?? null);
      setUbicacion({
        nombreProvincia: me?.nombreProvincia ?? null,
        nombreLocalidad: me?.nombreLocalidad ?? null,
      });

      if (!me?.idUsuario) {
        setPresentacion(null);
        cacheListoRef.current = true;
        return;
      }

      const { data } = await clienteAxios.get(
        `/usuarios/api/profesionales/${me.idUsuario}/presentacion`
      );
      setPresentacion(data);
      cacheListoRef.current = true;
    } catch (err) {
      setPresentacion(null);
      setMembresiaActual(null);
      setIdUsuario(null);
      setUbicacion(null);
      setError(err);
      cacheListoRef.current = false;
    } finally {
      if (esPrimeraCarga) setCargandoPerfil(false);
    }
  }, []);

  useEffect(() => {
    cargarPresentacion();
  }, [cargarPresentacion]);

  const refetch = useCallback(() => cargarPresentacion(true), [cargarPresentacion]);

  const invalidate = useCallback(async () => {
    cacheListoRef.current = false;
    setPresentacion(null);
    setMembresiaActual(null);
    setIdUsuario(null);
    setUbicacion(null);
    setCargandoPerfil(true);
    await cargarPresentacion(true);
  }, [cargarPresentacion]);

  const value = {
    presentacion,
    membresiaActual,
    idUsuario,
    ubicacion,
    cargandoPerfil,
    error,
    refetch,
    invalidate,
  };

  return (
    <ProfesionalSessionContext.Provider value={value}>
      {children}
    </ProfesionalSessionContext.Provider>
  );
};

export const useProfesionalSession = () => {
  const context = useContext(ProfesionalSessionContext);
  if (!context) {
    throw new Error('useProfesionalSession debe usarse dentro de ProfesionalSessionProvider');
  }
  return context;
};
