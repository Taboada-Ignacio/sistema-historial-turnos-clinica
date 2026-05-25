import React from 'react';

export const SEXO_OPTIONS = [
  { value: 'MASCULINO', label: 'Masculino' },
  { value: 'FEMENINO', label: 'Femenino' },
];

/** Etiqueta legible para valor API (MASCULINO / FEMENINO). */
export const formatSexoLabel = (sexo) => {
  if (!sexo) return '—';
  const found = SEXO_OPTIONS.find((o) => o.value === String(sexo).toUpperCase());
  return found ? found.label : sexo;
};

const SexoSelectField = ({
  value,
  onChange,
  required = true,
  disabled = false,
  label = 'Sexo',
  inputClassName = 'w-full rounded-xl border border-gray-200 px-4 py-3 text-sm outline-none focus:ring-2 focus:ring-blue-500/30',
  showLabels = true,
}) => (
  <div>
    {showLabels && (
      <label className="block text-xs font-bold text-gray-500 uppercase mb-2">
        {label}
        {required && <span className="text-red-500 ml-0.5">*</span>}
      </label>
    )}
    <select
      name="sexo"
      required={required}
      disabled={disabled}
      value={value}
      onChange={onChange}
      className={inputClassName}
    >
      <option value="">Seleccionar…</option>
      {SEXO_OPTIONS.map((opt) => (
        <option key={opt.value} value={opt.value}>
          {opt.label}
        </option>
      ))}
    </select>
  </div>
);

export default SexoSelectField;
