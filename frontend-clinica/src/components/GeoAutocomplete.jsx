import React, { useEffect, useId, useRef, useState } from 'react';

import { filtrarPorTexto } from '../utils/geoFilter';



function defaultOptionLabel(o) {

  return o?.displayLabel || o?.label || '';

}



/**

 * Input con lista desplegable; filtra `options` en el cliente según lo que escribe el usuario.

 *

 * @param {Object[]} options - { value, label, displayLabel?, subLabel? }

 * @param {string} value - id seleccionado

 * @param {(value: string, option: object|null) => void} onChange

 * @param {(query: string) => Object[]} [filterOptions] - si se omite, usa filtrarPorTexto(options, query)

 */

const GeoAutocomplete = ({

  options = [],

  value = '',

  onChange,

  filterOptions,

  placeholder = 'Escribí para buscar…',

  disabled = false,

  required = false,

  id: idProp,

  name,

  className = '',

  inputClassName = 'w-full px-4 py-2 rounded-xl border border-gray-200 focus:ring-2 focus:ring-clinica-dark outline-none bg-white',

  emptyHint = 'Sin coincidencias',

  getOptionLabel,

}) => {

  const labelFor = getOptionLabel ?? defaultOptionLabel;



  const autoId = useId();

  const inputId = idProp || `geo-ac-${autoId}`;

  const listId = `${inputId}-list`;

  const wrapperRef = useRef(null);

  const isTypingRef = useRef(false);



  const [open, setOpen] = useState(false);

  const [inputText, setInputText] = useState('');

  const [highlight, setHighlight] = useState(0);



  const selected = options.find((o) => o.value === String(value));



  const syncInputFromValue = () => {

    const sel = options.find((o) => o.value === String(value));

    if (value && sel) {

      setInputText(labelFor(sel));

    } else if (!value) {

      setInputText('');

    }

  };



  // Sincronizar texto solo cuando cambia el id externo o llegan opciones (carga async), no al tipear.

  useEffect(() => {

    if (isTypingRef.current) return;

    syncInputFromValue();

    // eslint-disable-next-line react-hooks/exhaustive-deps -- labelFor estable; options cuando cargan catálogo

  }, [value, options]);



  const resolveFiltered = (query) => {

    if (filterOptions) return filterOptions(query);

    return filtrarPorTexto(options, query);

  };



  const filtered = open ? resolveFiltered(inputText) : [];



  useEffect(() => {

    setHighlight(0);

  }, [inputText, open]);



  useEffect(() => {

    const onDocClick = (e) => {

      if (wrapperRef.current && !wrapperRef.current.contains(e.target)) {

        setOpen(false);

        isTypingRef.current = false;

        syncInputFromValue();

      }

    };

    document.addEventListener('mousedown', onDocClick);

    return () => document.removeEventListener('mousedown', onDocClick);

    // eslint-disable-next-line react-hooks/exhaustive-deps

  }, [value, options]);



  const pick = (option) => {

    isTypingRef.current = false;

    onChange(option.value, option);

    setInputText(labelFor(option));

    setOpen(false);

  };



  const onInputChange = (e) => {

    const text = e.target.value;

    isTypingRef.current = true;

    setInputText(text);

    setOpen(true);

    if (!text.trim()) {

      onChange('', null);

    } else if (value) {

      onChange('', null);

    }

  };



  const onFocus = () => {

    if (disabled) return;

    setOpen(true);

    if (selected) {

      isTypingRef.current = true;

      setInputText('');

    }

  };



  const onKeyDown = (e) => {

    if (!open || filtered.length === 0) {

      if (e.key === 'Escape') {

        setOpen(false);

        isTypingRef.current = false;

        syncInputFromValue();

      }

      return;

    }

    if (e.key === 'ArrowDown') {

      e.preventDefault();

      setHighlight((h) => Math.min(h + 1, filtered.length - 1));

    } else if (e.key === 'ArrowUp') {

      e.preventDefault();

      setHighlight((h) => Math.max(h - 1, 0));

    } else if (e.key === 'Enter') {

      e.preventDefault();

      pick(filtered[highlight]);

    } else if (e.key === 'Escape') {

      setOpen(false);

      isTypingRef.current = false;

      syncInputFromValue();

    }

  };



  return (

    <div ref={wrapperRef} className={`relative ${className}`}>

      <input

        type="text"

        id={inputId}

        name={name}

        role="combobox"

        aria-expanded={open}

        aria-controls={listId}

        aria-autocomplete="list"

        autoComplete="off"

        disabled={disabled}

        required={required}

        placeholder={placeholder}

        value={inputText}

        onChange={onInputChange}

        onFocus={onFocus}

        onKeyDown={onKeyDown}

        className={`${inputClassName} ${disabled ? 'opacity-50 cursor-not-allowed bg-gray-100' : ''}`}

      />

      {open && !disabled && (

        <ul

          id={listId}

          role="listbox"

          className="absolute z-50 mt-1 max-h-56 w-full overflow-auto rounded-xl border border-slate-200 bg-white py-1 shadow-lg"

        >

          {filtered.length === 0 ? (

            <li className="px-3 py-2 text-sm text-slate-500">{emptyHint}</li>

          ) : (

            filtered.map((opt, idx) => (

              <li

                key={opt.value}

                role="option"

                aria-selected={opt.value === String(value)}

                className={`cursor-pointer px-3 py-2 text-sm ${

                  idx === highlight ? 'bg-emerald-50 text-emerald-900' : 'text-slate-800 hover:bg-slate-50'

                }`}

                onMouseDown={(e) => e.preventDefault()}

                onClick={() => pick(opt)}

                onMouseEnter={() => setHighlight(idx)}

              >

                {labelFor(opt)}

              </li>

            ))

          )}

        </ul>

      )}

    </div>

  );

};



export default GeoAutocomplete;


