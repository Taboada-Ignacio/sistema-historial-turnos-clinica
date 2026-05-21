import React, { useEffect, useState } from 'react';
import clienteAxios from '../api/axiosConfig';
import { profesionalFotoRequestPath } from '../utils/profesionalFotoUrl';

/**
 * Muestra la foto de perfil vía GET autenticado (solo ROLE_PROFESIONAL / ROLE_ADMINISTRADOR).
 */
const ProfesionalFoto = ({ fotoPerfil, alt = '', className = '', placeholderClassName = '' }) => {
  const [src, setSrc] = useState(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    const path = profesionalFotoRequestPath(fotoPerfil);
    if (!path) {
      setSrc(null);
      setFailed(false);
      return undefined;
    }

    let cancelled = false;
    let objectUrl;

    (async () => {
      setFailed(false);
      try {
        const { data } = await clienteAxios.get(path, { responseType: 'blob' });
        if (cancelled) return;
        objectUrl = URL.createObjectURL(data);
        setSrc(objectUrl);
      } catch {
        if (!cancelled) {
          setSrc(null);
          setFailed(true);
        }
      }
    })();

    return () => {
      cancelled = true;
      if (objectUrl) URL.revokeObjectURL(objectUrl);
    };
  }, [fotoPerfil]);

  const placeholder =
    placeholderClassName ||
    'rounded-lg border border-slate-200 bg-slate-100 flex items-center justify-center text-[10px] text-slate-500 text-center leading-tight';

  if (!fotoPerfil || failed || !src) {
    return (
      <div className={placeholder} aria-hidden={!alt}>
        {failed ? 'Error' : 'Sin foto'}
      </div>
    );
  }

  return <img src={src} alt={alt} className={className} />;
};

export default ProfesionalFoto;
