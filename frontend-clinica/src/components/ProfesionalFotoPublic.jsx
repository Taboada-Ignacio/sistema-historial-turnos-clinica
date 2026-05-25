import React, { useEffect, useState } from 'react';
import { clienteAxiosPublic } from '../api/axiosConfig';
import { profesionalFotoPublicRequestPath } from '../utils/profesionalFotoUrl';

/**
 * Foto de perfil para catálogo público (GET sin JWT, solo profesionales ACTIVOS listables).
 */
const ProfesionalFotoPublic = ({ fotoPerfil, alt = '', className = '', placeholderClassName = '' }) => {
  const [src, setSrc] = useState(null);
  const [failed, setFailed] = useState(false);

  useEffect(() => {
    const path = profesionalFotoPublicRequestPath(fotoPerfil);
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
        const { data } = await clienteAxiosPublic.get(path, { responseType: 'blob' });
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
    'rounded-xl border border-slate-200 bg-slate-100 flex items-center justify-center text-xs font-bold text-slate-400';

  if (!fotoPerfil || failed || !src) {
    return (
      <div className={placeholder} aria-hidden={!alt}>
        Sin foto
      </div>
    );
  }

  return <img src={src} alt={alt} className={className} />;
};

export default ProfesionalFotoPublic;
