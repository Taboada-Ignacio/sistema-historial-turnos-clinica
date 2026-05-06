import React from 'react';

const entidadesUsuarios = [
  { nombre: 'Administradores', endpoint: '/usuarios/api/administradores' },
  { nombre: 'Pacientes', endpoint: '/usuarios/api/pacientes' },
  { nombre: 'Profesionales', endpoint: '/usuarios/api/profesionales' },
  { nombre: 'Roles', endpoint: '/usuarios/api/roles' },
  { nombre: 'Estados', endpoint: '/usuarios/api/estados' },
  { nombre: 'Obras sociales', endpoint: '/usuarios/api/obras-sociales' },
  { nombre: 'Especialidades', endpoint: '/usuarios/api/especialidades' },
  { nombre: 'Provincias', endpoint: '/usuarios/api/provincias' },
  { nombre: 'Localidades', endpoint: '/usuarios/api/localidades' },
];

const AdministrarEntidadesPage = () => {
  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <h2 className="text-xl font-black text-slate-900 mb-2">Administrar entidades</h2>
      <p className="text-slate-600 mb-6">
        Este módulo centraliza los CRUD del microservicio de usuarios. Podés implementarlos por entidad usando los endpoints listados.
      </p>

      <div className="grid grid-cols-1 md:grid-cols-2 gap-4">
        {entidadesUsuarios.map((entidad) => (
          <article key={entidad.nombre} className="border border-slate-200 rounded-xl p-4 bg-slate-50">
            <h3 className="font-bold text-slate-900">{entidad.nombre}</h3>
            <p className="text-sm text-slate-600 mt-1">Endpoint base: {entidad.endpoint}</p>
            <div className="flex gap-2 mt-3">
              <span className="text-xs px-2 py-1 rounded bg-slate-200 text-slate-700">Crear</span>
              <span className="text-xs px-2 py-1 rounded bg-slate-200 text-slate-700">Listar</span>
              <span className="text-xs px-2 py-1 rounded bg-slate-200 text-slate-700">Editar</span>
              <span className="text-xs px-2 py-1 rounded bg-slate-200 text-slate-700">Eliminar</span>
            </div>
          </article>
        ))}
      </div>
    </section>
  );
};

export default AdministrarEntidadesPage;
