import React from 'react';
import ProfesionalModuloPlaceholder from './ProfesionalModuloPlaceholder';

const ProfesionalAgendaPage = () => (
  <ProfesionalModuloPlaceholder
    titulo="Agenda de hoy"
    descripcion="Vista de turnos del día, horarios de atención y gestión de la agenda profesional. Se integrará con el microservicio de turnos."
    funcionalidadesPrevistas={[
      'Listado de turnos del día con estado (confirmado, en sala, finalizado, ausente)',
      'Filtros por fecha, especialidad y consultorio',
      'Acceso rápido al detalle del paciente del turno',
      'Bloques de disponibilidad y excepciones de agenda',
    ]}
  />
);

export default ProfesionalAgendaPage;
