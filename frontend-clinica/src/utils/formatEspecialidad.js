const LEGACY_ESPECIALIDAD_LABELS = {
  MEDICO: 'Medicina general',
};

/**
 * Etiqueta legible para catálogo/API (evita "MEDICO" y mayúsculas crudas en UI).
 */
export function formatEspecialidad(descripcion) {
  if (!descripcion || typeof descripcion !== 'string') return '';
  const trimmed = descripcion.trim();
  if (!trimmed) return '';

  const legacy = LEGACY_ESPECIALIDAD_LABELS[trimmed.toUpperCase()];
  if (legacy) return legacy;

  if (trimmed.toUpperCase() === 'SIN ESPECIFICAR') return 'Sin especificar';

  return trimmed
    .toLowerCase()
    .split(/\s+/)
    .map((word) => word.charAt(0).toUpperCase() + word.slice(1))
    .join(' ');
}
