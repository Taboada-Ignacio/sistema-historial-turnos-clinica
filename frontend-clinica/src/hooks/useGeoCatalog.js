import { useCallback, useEffect, useMemo, useState } from 'react';
import clienteAxios from '../api/axiosConfig';
import {
  esRegistroGeoReservado,
  filtrarPorTexto,
  localidadToOption,
  provinciaToOption,
} from '../utils/geoFilter';

/**
 * Catálogo geo en memoria; el filtrado por texto es en el cliente (GeoAutocomplete).
 */
export function useGeoCatalog({ excludeSentinel = true } = {}) {
  const [provinciasRaw, setProvinciasRaw] = useState([]);
  const [localidadesRaw, setLocalidadesRaw] = useState([]);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [reloadKey, setReloadKey] = useState(0);

  const reload = useCallback(() => setReloadKey((k) => k + 1), []);

  useEffect(() => {
    let cancelled = false;
    (async () => {
      setLoading(true);
      setError('');
      try {
        const [provRes, locRes] = await Promise.all([
          clienteAxios.get('/usuarios/api/provincias'),
          clienteAxios.get('/usuarios/api/localidades'),
        ]);
        if (cancelled) return;
        setProvinciasRaw(Array.isArray(provRes.data) ? provRes.data : []);
        setLocalidadesRaw(Array.isArray(locRes.data) ? locRes.data : []);
      } catch {
        if (!cancelled) setError('No se pudieron cargar provincias y localidades.');
      } finally {
        if (!cancelled) setLoading(false);
      }
    })();
    return () => {
      cancelled = true;
    };
  }, [reloadKey]);

  const provincias = useMemo(() => {
    let list = provinciasRaw;
    if (excludeSentinel) {
      list = list.filter((p) => !esRegistroGeoReservado(p.nombre));
    }
    return list.map(provinciaToOption);
  }, [provinciasRaw, excludeSentinel]);

  const localidades = useMemo(() => {
    let list = localidadesRaw;
    if (excludeSentinel) {
      list = list.filter((l) => !esRegistroGeoReservado(l.nombre));
    }
    return list.map(localidadToOption);
  }, [localidadesRaw, excludeSentinel]);

  const localidadesPorProvincia = useCallback(
    (provinciaId) => {
      if (!provinciaId) return [];
      const id = String(provinciaId);
      return localidades.filter((o) => String(o.raw?.idProvincia) === id);
    },
    [localidades],
  );

  const filtrarProvincias = useCallback(
    (query) => filtrarPorTexto(provincias, query),
    [provincias],
  );

  const filtrarLocalidades = useCallback(
    (provinciaId, query) => filtrarPorTexto(localidadesPorProvincia(provinciaId), query),
    [localidadesPorProvincia],
  );

  const filtrarTodasLocalidades = useCallback(
    (query) => filtrarPorTexto(localidades, query),
    [localidades],
  );

  const getProvinciaOption = useCallback(
    (id) => provincias.find((o) => o.value === String(id)),
    [provincias],
  );

  const getLocalidadOption = useCallback(
    (id) => localidades.find((o) => o.value === String(id)),
    [localidades],
  );

  return {
    loading,
    error,
    provincias,
    localidades,
    localidadesPorProvincia,
    filtrarProvincias,
    filtrarLocalidades,
    filtrarTodasLocalidades,
    getProvinciaOption,
    getLocalidadOption,
    reload,
  };
}
