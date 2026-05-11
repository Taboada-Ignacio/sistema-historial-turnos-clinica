import { API_BASE_URL } from '../config/env';

/**
 * URL absoluta para mostrar la foto vía API Gateway: StripPrefix deja /fotosPerfilProfesionales/... en ms-usuarios.
 */
export function profesionalFotoAbsoluteUrl(fotoPerfil) {
  if (fotoPerfil == null || fotoPerfil === '') return null;
  const path = fotoPerfil.startsWith('/') ? fotoPerfil : `/${fotoPerfil}`;
  return `${API_BASE_URL}/usuarios${path}`;
}
