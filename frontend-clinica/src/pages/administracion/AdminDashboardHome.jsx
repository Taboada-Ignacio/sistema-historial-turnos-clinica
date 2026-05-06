import React from 'react';

const modules = [
  '1) Administrar profesionales pendientes de revisión',
  '2) Administrar entidades del microservicio de usuarios',
  '3) Administración de turnos (microservicio en construcción)',
  '4) Administración de historiales clínicos (microservicio en construcción)',
];

const AdminDashboardHome = () => {
  return (
    <section className="bg-white border border-slate-200 rounded-2xl p-6">
      <h2 className="text-2xl font-black text-slate-900 mb-2">Dashboard de Administración</h2>
      <p className="text-slate-600 mb-6">Seleccioná uno de los cuatro módulos para comenzar.</p>
      <div className="grid gap-3">
        {modules.map((module) => (
          <div key={module} className="border border-slate-200 rounded-xl p-4 text-slate-700 bg-slate-50">
            {module}
          </div>
        ))}
      </div>
    </section>
  );
};

export default AdminDashboardHome;
