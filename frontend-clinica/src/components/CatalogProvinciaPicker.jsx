import React from 'react';
import GeoAutocomplete from './GeoAutocomplete';
import { useGeoCatalog } from '../hooks/useGeoCatalog';

/** Selector de provincia (autocomplete, filtro en cliente). */
const CatalogProvinciaPicker = ({
  value,
  onChange,
  excludeSentinel = false,
  required = false,
  inputClassName = 'w-full rounded-lg border border-slate-200 px-3 py-2 outline-none focus:ring-2 focus:ring-emerald-600/30',
  disabled = false,
}) => {
  const geo = useGeoCatalog({ excludeSentinel });

  return (
    <GeoAutocomplete
      options={geo.provincias}
      value={value ? String(value) : ''}
      onChange={(val) => onChange(val)}
      filterOptions={geo.filtrarProvincias}
      placeholder={geo.loading ? 'Cargando...' : 'Provincia - escribi para buscar'}
      disabled={disabled || geo.loading}
      required={required}
      inputClassName={inputClassName}
    />
  );
};

export default CatalogProvinciaPicker;
