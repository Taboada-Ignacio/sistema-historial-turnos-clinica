/** Rutas API públicas de onboarding administrador (separadas del CRUD /api/administradores). */
export const ADMIN_ONBOARDING_API = {
  registro: '/usuarios/api/onboarding/admin/registro',
  confirmarCodigo: '/usuarios/api/onboarding/admin/confirmar-codigo',
  reenviarConfirmacion: (email) =>
    `/usuarios/api/onboarding/admin/reenviar-confirmacion?email=${encodeURIComponent(email)}`,
};

const STORAGE_KEY = 'admin_onboarding_email';

export function persistAdminOnboardingEmail(email) {
  if (email?.trim()) {
    sessionStorage.setItem(STORAGE_KEY, email.trim());
  }
}

export function resolveAdminOnboardingEmail(locationState, searchParams) {
  const fromState = locationState?.email?.trim();
  if (fromState) return fromState;
  const fromQuery = searchParams?.get('email')?.trim();
  if (fromQuery) return fromQuery;
  return sessionStorage.getItem(STORAGE_KEY)?.trim() || '';
}

export function clearAdminOnboardingEmail() {
  sessionStorage.removeItem(STORAGE_KEY);
}
