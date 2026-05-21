/** Mensaje de error legible desde respuestas del API de usuarios (campo {@code mensaje} del backend). */
export function apiErrorMessage(err, fallback = 'Ocurrió un error.') {
  const data = err?.response?.data;
  if (!data) return fallback;
  if (data.mensaje) return data.mensaje;
  if (data.message) return data.message;
  if (typeof data === 'object') {
    const first = Object.values(data).find((v) => typeof v === 'string');
    if (first) return first;
  }
  return fallback;
}

/** @deprecated Usar {@link apiErrorMessage}. */
export const adminApiErrorMessage = apiErrorMessage;
