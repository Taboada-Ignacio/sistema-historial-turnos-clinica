export function buildPostPayload(tipo, form) {
  if (tipo === 'obras-sociales' || tipo === 'especialidades') {
    return { descripcion: form.descripcion.trim().toUpperCase() };
  }
  if (tipo === 'provincias') {
    return { nombre: form.nombre.trim().toUpperCase() };
  }
  if (tipo === 'localidades') {
    return {
      nombre: form.nombre.trim().toUpperCase(),
      idProvincia: parseInt(form.idProvincia, 10),
    };
  }
  return {};
}

export function buildPutPayload(tipo, form) {
  return buildPostPayload(tipo, form);
}
