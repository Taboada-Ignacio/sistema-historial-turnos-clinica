/** Nombre para catálogo público: apellido, nombre */
export function nombreProfesionalPresentacion(profesional) {
  if (!profesional) return '';
  return [profesional.apellido, profesional.nombre].filter(Boolean).join(', ');
}
