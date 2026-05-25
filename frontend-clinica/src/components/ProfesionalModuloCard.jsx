import React from 'react';
import { Link } from 'react-router-dom';

const ProfesionalModuloCard = ({ disabled, titulo, descripcion, linkTo, linkLabel, children }) => {
  const cardClass = disabled
    ? 'bg-gray-100 border-gray-200 opacity-75 cursor-not-allowed'
    : 'bg-white border-gray-200 shadow-sm hover:shadow-lg group focus:outline-none focus-visible:ring-2 focus-visible:ring-blue-500/40';

  const contenido = (
    <>
      {children}
      <h3
        className={`font-black text-xl mb-2 ${
          disabled ? 'text-gray-400' : 'text-gray-900 group-hover:text-blue-600 transition-colors'
        }`}
      >
        {titulo}
      </h3>
      <p className={`text-sm mb-6 leading-relaxed ${disabled ? 'text-gray-400' : 'text-gray-500'}`}>
        {descripcion}
      </p>
      <span
        className={`font-bold text-sm flex items-center gap-2 ${
          disabled ? 'text-gray-400' : 'text-blue-600 group-hover:translate-x-2 transition-transform'
        }`}
      >
        {linkLabel}{' '}
        {!disabled && (
          <svg
            xmlns="http://www.w3.org/2000/svg"
            width="16"
            height="16"
            viewBox="0 0 24 24"
            fill="none"
            stroke="currentColor"
            strokeWidth="2.5"
            strokeLinecap="round"
            strokeLinejoin="round"
          >
            <path d="M5 12h14M12 5l7 7-7 7" />
          </svg>
        )}
      </span>
    </>
  );

  if (disabled || !linkTo) {
    return (
      <div className={`p-8 rounded-3xl border transition-all ${cardClass}`} aria-disabled={disabled}>
        {contenido}
      </div>
    );
  }

  return (
    <Link to={linkTo} className={`block p-8 rounded-3xl border transition-all ${cardClass}`}>
      {contenido}
    </Link>
  );
};

export default ProfesionalModuloCard;
