import { useEffect, useState } from 'react';
import clienteAxios from '../api/axiosConfig';

/**
 * ID del administrador autenticado (GET /api/administradores/me).
 */
export function useAdminSesion() {
  const [idUsuario, setIdUsuario] = useState(null);
  const [loading, setLoading] = useState(true);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      try {
        const { data } = await clienteAxios.get('/usuarios/api/administradores/me');
        if (!cancelled) setIdUsuario(data?.idUsuario ?? null);
      } catch {
        if (!cancelled) setIdUsuario(null);
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, []);

  const esPropio = (id) =>
    idUsuario != null && id != null && Number(idUsuario) === Number(id);

  return { idUsuario, loading, esPropio };
}

export default useAdminSesion;
