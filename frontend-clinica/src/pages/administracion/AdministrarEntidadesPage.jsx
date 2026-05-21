import React from 'react';
import { Link } from 'react-router-dom';
import { ADMIN_PATHS } from '../../utils/portalPaths';
import { ADMIN_CATALOG_CONFIG } from './adminCatalogConfig';

const catalogoTipos = ['roles', 'obras-sociales', 'especialidades', 'provincias', 'localidades', 'estados'];

const AdministrarEntidadesPage = () => {
  const base = ADMIN_PATHS.dashboard;

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <h2 className="text-xl font-black text-slate-900 mb-2">Administrar entidades</h2>
      <p className="text-slate-600 mb-6">
        Catálogos del microservicio de usuarios. Los <strong>roles</strong> y los <strong>estados de cuenta</strong>{' '}
        solo se consultan; el resto permite editar y eliminar (con contraseña al confirmar cambios). Existe un
        registro reservado <strong>SIN ESPECIFICAR</strong> para mantener integridad al borrar.
      </p>

      <h3 className="text-sm font-bold text-slate-500 uppercase tracking-wide mb-3">Catálogos</h3>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8">
        {catalogoTipos.map((tipo) => {
          const cfg = ADMIN_CATALOG_CONFIG[tipo];
          if (!cfg) return null;
          return (
            <Link
              key={tipo}
              to={`${base}/catalogo/${tipo}`}
              className="block border border-slate-200 rounded-xl p-4 bg-slate-50 hover:border-emerald-300 hover:bg-emerald-50/30 transition-colors"
            >
              <h3 className="font-bold text-slate-900">{cfg.label}</h3>
              <p className="text-sm text-slate-600 mt-1">
                {cfg.readOnly ? 'Solo consulta (sin modificar ni eliminar).' : 'Listar, modificar y eliminar.'}
              </p>
              <p className="text-xs text-slate-500 mt-2 font-mono">GET /api/{cfg.apiSegment}</p>
            </Link>
          );
        })}
      </div>

      <h3 className="text-sm font-bold text-slate-500 uppercase tracking-wide mb-3">
        Pacientes, profesionales y administradores
      </h3>
      <div className="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-4 mb-8 max-w-5xl">
        <Link
          to={`${base}/entidades/pacientes`}
          className="block border border-slate-200 rounded-xl p-4 bg-slate-50 hover:border-emerald-300 hover:bg-emerald-50/30 transition-colors"
        >
          <h3 className="font-bold text-slate-900">Consultar pacientes</h3>
          <p className="text-sm text-slate-600 mt-1">
            Buscar por apellido/nombre y/o ubicación. Ver detalle, modificar o eliminar (con contraseña).
          </p>
          <p className="text-xs text-slate-500 mt-2 font-mono">GET /api/pacientes/buscar</p>
        </Link>
        <Link
          to={`${base}/entidades/profesionales`}
          className="block border border-slate-200 rounded-xl p-4 bg-slate-50 hover:border-emerald-300 hover:bg-emerald-50/30 transition-colors"
        >
          <h3 className="font-bold text-slate-900">Consultar profesionales</h3>
          <p className="text-sm text-slate-600 mt-1">
            Buscar por apellido/nombre, especialidad y/o ubicación. Listado con foto. Ver detalle, modificar o eliminar.
          </p>
          <p className="text-xs text-slate-500 mt-2 font-mono">GET /api/profesionales/buscar</p>
        </Link>
        <Link
          to={`${base}/entidades/administradores`}
          className="block border border-slate-200 rounded-xl p-4 bg-slate-50 hover:border-emerald-300 hover:bg-emerald-50/30 transition-colors"
        >
          <h3 className="font-bold text-slate-900">Consultar administradores</h3>
          <p className="text-sm text-slate-600 mt-1">
            Listado completo. Ver detalle de cualquiera; modificar o eliminar solo tu propia cuenta.
          </p>
          <p className="text-xs text-slate-500 mt-2 font-mono">GET /api/administradores</p>
        </Link>
      </div>

      <h3 className="text-sm font-bold text-slate-500 uppercase tracking-wide mb-3">Direcciones</h3>
      <Link
        to={`${base}/direcciones`}
        className="block border border-slate-200 rounded-xl p-4 bg-slate-50 hover:border-emerald-300 hover:bg-emerald-50/30 transition-colors mb-8 max-w-xl"
      >
        <h3 className="font-bold text-slate-900">Direcciones por localidad</h3>
        <p className="text-sm text-slate-600 mt-1">Alta, edición de nombre y baja (con contraseña).</p>
        <p className="text-xs text-slate-500 mt-2 font-mono">/api/direcciones</p>
      </Link>
    </section>
  );
};

export default AdministrarEntidadesPage;
