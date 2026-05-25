/**
 * Ruta relativa al gateway para descargar foto con JWT (admin o profesional).
 * fotoPerfil en BD: /fotosPerfilProfesionales/{uuid}.webp
 */
export function profesionalFotoRequestPath(fotoPerfil) {
  if (fotoPerfil == null || fotoPerfil === '') return null;
  const match = String(fotoPerfil).match(/\/([^/]+\.webp)$/i);
  if (!match) return null;
  return `/usuarios/api/profesionales/fotos/${encodeURIComponent(match[1])}`;
}

/** Ruta pública (catálogo) sin JWT; solo fotos de profesionales ACTIVOS listables. */
export function profesionalFotoPublicRequestPath(fotoPerfil) {
  if (fotoPerfil == null || fotoPerfil === '') return null;
  const match = String(fotoPerfil).match(/\/([^/]+\.webp)$/i);
  if (!match) return null;
  return `/usuarios/api/profesionales/fotos/public/${encodeURIComponent(match[1])}`;
}

/** @deprecated Usar ProfesionalFoto (blob autenticado). */
export function profesionalFotoAbsoluteUrl(fotoPerfil) {
  return profesionalFotoRequestPath(fotoPerfil);
}
