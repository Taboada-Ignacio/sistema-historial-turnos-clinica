/** Normaliza texto para comparar provincias/localidades (mayúsculas, sin espacios extra). */
export function normalizarGeoTexto(texto) {
  return (texto ?? '').trim().toUpperCase();
}

export const SIN_ESPECIFICAR_GEO = 'SIN ESPECIFICAR';

/** Filtra opciones { label, ... } por coincidencia parcial en label (cliente). */
export function filtrarPorTexto(opciones, query, max = 80) {
  if (!Array.isArray(opciones)) return [];
  const q = normalizarGeoTexto(query);
  if (!q) return opciones.slice(0, max);
  return opciones.filter((o) => normalizarGeoTexto(o.label).includes(q)).slice(0, max);
}

export function provinciaToOption(p) {
  return {
    value: String(p.idProvincia),
    label: p.nombre ?? '',
    raw: p,
  };
}

export function localidadToOption(l) {
  const label = l.nombre ?? '';
  const sub = l.nombreProvincia ? ` (${l.nombreProvincia})` : '';
  return {
    value: String(l.idLocalidad),
    label,
    subLabel: l.nombreProvincia,
    displayLabel: `${label}${sub}`,
    raw: l,
  };
}

export function esRegistroGeoReservado(nombre) {
  return normalizarGeoTexto(nombre) === SIN_ESPECIFICAR_GEO;
}
