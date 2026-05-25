import React, { createContext, useCallback, useContext, useEffect, useRef, useState } from 'react';
import clienteAxios from '../api/axiosConfig';
import { esProfesionalEnPortalPaciente } from '../utils/portalPaciente';

const PacienteSessionContext = createContext(null);

export const PacienteSessionProvider = ({ children }) => {
  const [sesion, setSesion] = useState(null);
  const [perfilEditable, setPerfilEditable] = useState(true);
  const [cargandoPerfil, setCargandoPerfil] = useState(true);
  const [error, setError] = useState(null);
  const cacheListoRef = useRef(false);

  const cargarPerfil = useCallback(async (force = false) => {
    if (cacheListoRef.current && !force) return;

    const esPrimeraCarga = !cacheListoRef.current;
    if (esPrimeraCarga) setCargandoPerfil(true);
    setError(null);

    try {
      const { data } = await clienteAxios.get('/usuarios/api/pacientes/me');
      setSesion(data);
      setPerfilEditable(data?.perfilEditable !== false);
      cacheListoRef.current = true;
    } catch (err) {
      setSesion(null);
      setPerfilEditable(true);
      setError(err);
      cacheListoRef.current = false;
    } finally {
      if (esPrimeraCarga) setCargandoPerfil(false);
    }
  }, []);

  useEffect(() => {
    cargarPerfil();
  }, [cargarPerfil]);

  const refetch = useCallback(() => cargarPerfil(true), [cargarPerfil]);

  const invalidate = useCallback(async () => {
    cacheListoRef.current = false;
    setSesion(null);
    setCargandoPerfil(true);
    await cargarPerfil(true);
  }, [cargarPerfil]);

  const esProfesionalDual = esProfesionalEnPortalPaciente(sesion);

  const value = {
    /** @deprecated Usar {@link sesion}. */
    paciente: sesion,
    sesion,
    perfilEditable,
    esProfesionalDual,
    cargandoPerfil,
    error,
    refetch,
    invalidate,
  };

  return (
    <PacienteSessionContext.Provider value={value}>
      {children}
    </PacienteSessionContext.Provider>
  );
};

export const usePacienteSession = () => {
  const context = useContext(PacienteSessionContext);
  if (!context) {
    throw new Error('usePacienteSession debe usarse dentro de PacienteSessionProvider');
  }
  return context;
};
