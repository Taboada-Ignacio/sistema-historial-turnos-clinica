import React from 'react';
import { Link } from 'react-router-dom';
import { ADMIN_PATHS } from '../../utils/portalPaths';
import { ADMIN_CATALOG_CONFIG } from './adminCatalogConfig';

const entidadesPlaceholder = [
  { nombre: 'Administradores', descripcion: 'Gestión de cuentas admin (fuera de este módulo por ahora).' },
  { nombre: 'Pacientes', descripcion: 'Altas y edición en el portal de pacientes.' },
  { nombre: 'Profesionales', descripcion: 'Altas y aprobación desde el panel correspondiente.' },
  { nombre: 'Estados', descripcion: 'Catálogo interno; sin pantalla de administración por ahora.' },
];

const catalogoTipos = ['roles', 'obras-sociales', 'especialidades', 'provincias', 'localidades'];

const AdministrarEntidadesPage = () => {
  const base = ADMIN_PATHS.dashboard;

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <h2 className="text-xl font-black text-slate-900 mb-2">Administrar entidades</h2>
      <p className="text-slate-600 mb-6">
        Catálogos del microservicio de usuarios. Los roles solo se consultan; el resto permite editar y eliminar (con
        contraseña al confirmar cambios). Existe un registro reservado <strong>SIN ESPECIFICAR</strong> para mantener
        integridad al borrar.
      </p>

      <h3 className="text-sm font-bold text-slate-500 uppercase tracking-wide mb-3">Catálogos</h3>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4 mb-8">
        {catalogoTipos.map((tipo) => {
          const cfg = ADMIN_CATALOG_CONFIG[tipo];
          return (
            <Link
              key={tipo}
              to={`${base}/catalogo/${tipo}`}
              className="block border border-slate-200 rounded-xl p-4 bg-slate-50 hover:border-emerald-300 hover:bg-emerald-50/30 transition-colors"
            >
              <h3 className="font-bold text-slate-900">{cfg.label}</h3>
              <p className="text-sm text-slate-600 mt-1">
                {cfg.readOnly ? 'Solo listado.' : 'Listar, modificar y eliminar.'}
              </p>
              <p className="text-xs text-slate-500 mt-2 font-mono">/api/{cfg.apiSegment}</p>
            </Link>
          );
        })}
      </div>

      <h3 className="text-sm font-bold text-slate-500 uppercase tracking-wide mb-3">Otras entidades</h3>
      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {entidadesPlaceholder.map((entidad) => (
          <article key={entidad.nombre} className="border border-slate-200 rounded-xl p-4 bg-white">
            <h3 className="font-bold text-slate-900">{entidad.nombre}</h3>
            <p className="text-sm text-slate-600 mt-1">{entidad.descripcion}</p>
          </article>
        ))}
      </div>
    </section>
  );
};

export default AdministrarEntidadesPage;
