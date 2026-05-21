/** Catálogo maestro del panel admin (ms-usuarios). */
export const SIN_ESPECIFICAR = 'SIN ESPECIFICAR';

export function filaEsReservada(row, config) {
  const v = row?.[config.sentinelField];
  return typeof v === 'string' && v.trim().toUpperCase() === SIN_ESPECIFICAR;
}

/**
 * tipo: segmento de URL y de API tras /api/
 */
export const ADMIN_CATALOG_CONFIG = {
  roles: {
    label: 'Roles',
    apiSegment: 'roles',
    idKey: 'idRol',
    listTitle: 'descripcion',
    readOnly: true,
    sentinelField: 'descripcion',
  },
  'obras-sociales': {
    label: 'Obras sociales',
    apiSegment: 'obras-sociales',
    idKey: 'idObraSocial',
    listTitle: 'descripcion',
    readOnly: false,
    sentinelField: 'descripcion',
    editableKeys: ['descripcion'],
    fieldLabels: { descripcion: 'Descripción' },
  },
  especialidades: {
    label: 'Especialidades',
    apiSegment: 'especialidades',
    idKey: 'idEspecialidad',
    listTitle: 'descripcion',
    readOnly: false,
    sentinelField: 'descripcion',
    editableKeys: ['descripcion'],
    fieldLabels: { descripcion: 'Descripción' },
  },
  provincias: {
    label: 'Provincias',
    apiSegment: 'provincias',
    idKey: 'idProvincia',
    listTitle: 'nombre',
    readOnly: false,
    sentinelField: 'nombre',
    editableKeys: ['nombre'],
    fieldLabels: { nombre: 'Nombre' },
  },
  localidades: {
    label: 'Localidades',
    apiSegment: 'localidades',
    idKey: 'idLocalidad',
    listTitle: 'nombre',
    subtitleKey: 'nombreProvincia',
    readOnly: false,
    sentinelField: 'nombre',
    supportsProvinciaFilter: true,
    editableKeys: ['nombre', 'idProvincia'],
    fieldLabels: { nombre: 'Nombre', idProvincia: 'ID provincia' },
  },
  estados: {
    label: 'Estados de cuenta',
    apiSegment: 'estados',
    idKey: 'idEstado',
    listTitle: 'nombre',
    readOnly: true,
    sentinelField: 'nombre',
    listHint: 'PENDIENTE, ACTIVO, BLOQUEADO',
  },
};

export function getCatalogConfig(tipo) {
  return ADMIN_CATALOG_CONFIG[tipo] ?? null;
}
