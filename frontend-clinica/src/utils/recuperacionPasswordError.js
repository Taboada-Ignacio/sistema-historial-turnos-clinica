import { RECUPERACION_PASSWORD_ERROR_PATH } from './portalPaths';

export function mensajeErrorRecuperacion(err) {
  return err?.response?.data?.mensaje || err?.response?.data?.message || '';
}

export function esErrorEnlaceRecuperacion(mensaje) {
  if (!mensaje) return false;
  const m = mensaje.toLowerCase();
  return (
    m.includes('expir') ||
    m.includes('utilizado') ||
    m.includes('inválido') ||
    m.includes('invalido')
  );
}

export function motivoDesdeMensaje(mensaje) {
  const m = (mensaje || '').toLowerCase();
  return m.includes('expir') ? 'expirado' : 'invalido';
}

export function rutaErrorRecuperacionPassword(tipo, mensaje) {
  const motivo = motivoDesdeMensaje(mensaje);
  return `${RECUPERACION_PASSWORD_ERROR_PATH}?tipo=${encodeURIComponent(tipo)}&motivo=${motivo}`;
}

/**
 * Maneja errores del POST cambiar-password-con-token (evita pantalla de error si ya hubo éxito).
 * @returns {boolean} true si el error quedó manejado
 */
export function manejarErrorCambioPasswordConToken(err, { yaCompletado, navigate, tipo, setError, setSuccess, loginPath }) {
  if (yaCompletado) {
    setSuccess('Contraseña actualizada correctamente.');
    setTimeout(() => navigate(loginPath), 1500);
    return true;
  }
  const msg = mensajeErrorRecuperacion(err);
  if (esErrorEnlaceRecuperacion(msg)) {
    navigate(rutaErrorRecuperacionPassword(tipo, msg));
    return true;
  }
  setError(msg || 'No se pudo cambiar la contraseña.');
  return true;
}
