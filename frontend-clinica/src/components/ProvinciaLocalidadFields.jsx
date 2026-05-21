import React, { useCallback } from 'react';
import GeoAutocomplete from './GeoAutocomplete';
import { useGeoCatalog } from '../hooks/useGeoCatalog';

const ProvinciaLocalidadFields = ({
  provinciaId,
  localidadId,
  onProvinciaChange,
  onLocalidadChange,
  excludeSentinel = true,
  inputClassName,
  className = 'grid grid-cols-1 md:grid-cols-2 gap-4',
  provinciaPlaceholder = 'Provincia',
  localidadPlaceholder = 'Localidad',
  localidadDisabled,
  provinciaRequired = true,
  localidadRequired = true,
  showLabels = false,
  labelClassName = 'block text-xs font-bold text-gray-400 uppercase mb-2',
}) => {
  const geo = useGeoCatalog({ excludeSentinel });
  const locDisabled = localidadDisabled ?? !provinciaId;
  const localidadLabel = useCallback((o) => o.label, []);

  return (
    <>
      {geo.error && <p className="text-sm text-red-600 col-span-full">{geo.error}</p>}
      <div className={className}>
        <div>
          {showLabels && <label className={labelClassName}>Provincia</label>}
          <GeoAutocomplete
            options={geo.provincias}
            value={provinciaId ? String(provinciaId) : ''}
            onChange={(val) => onProvinciaChange(val)}
            filterOptions={geo.filtrarProvincias}
            placeholder={geo.loading ? 'Cargando...' : `${provinciaPlaceholder} - escribi para buscar`}
            disabled={geo.loading}
            required={provinciaRequired}
            inputClassName={inputClassName}
          />
        </div>
        <div>
          {showLabels && <label className={labelClassName}>Localidad</label>}
          <GeoAutocomplete
            options={geo.localidadesPorProvincia(provinciaId)}
            value={localidadId ? String(localidadId) : ''}
            onChange={(val) => onLocalidadChange(val)}
            filterOptions={(q) => geo.filtrarLocalidades(provinciaId, q)}
            placeholder={
              !provinciaId
                ? 'Elegi primero una provincia'
                : geo.loading
                  ? 'Cargando...'
                  : `${localidadPlaceholder} - escribi para buscar`
            }
            disabled={geo.loading || locDisabled}
            required={localidadRequired}
            inputClassName={inputClassName}
            getOptionLabel={localidadLabel}
          />
        </div>
      </div>
    </>
  );
};

export default ProvinciaLocalidadFields;