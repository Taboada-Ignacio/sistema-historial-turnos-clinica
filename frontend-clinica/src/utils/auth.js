import { PACIENTE_PATHS, PROFESIONAL_PATHS, ADMIN_PATHS, HOME_PATH } from './portalPaths';

export const ADMIN_ROLE = 'ADMIN';

const STORAGE_KEYS = {
  token: 'token',
  portal: 'app_portal', // paciente | profesional | admin
  role: 'app_role',
  email: 'app_email',
  remember: 'remember_me',
};

// Determina qué storage usar basado en la opción "Recordarme"
const getStorage = (rememberMe) => (rememberMe ? localStorage : sessionStorage);

export const decodeJwtPayload = (token) => {
  try {
    const payloadBase64 = token.split('.')[1];
    if (!payloadBase64) return null;
    
    const normalizedPayload = payloadBase64.replace(/-/g, '+').replace(/_/g, '/');
    const jsonPayload = decodeURIComponent(
      atob(normalizedPayload)
        .split('')
        .map((char) => `%${`00${char.charCodeAt(0).toString(16)}`.slice(-2)}`)
        .join('')
    );
    return JSON.parse(jsonPayload);
  } catch {
    return null;
  }
};

export const getUserEmailFromToken = (token) => {
  return decodeJwtPayload(token)?.sub || null;
};

export const savePortalSession = ({ token, portal, rememberMe, email }) => {
  clearSession(); // Limpiamos cualquier sesión previa por seguridad
  const storage = getStorage(Boolean(rememberMe));
  
  storage.setItem(STORAGE_KEYS.token, token);
  storage.setItem(STORAGE_KEYS.portal, portal);
  storage.setItem(STORAGE_KEYS.remember, String(Boolean(rememberMe)));
  if (email) storage.setItem(STORAGE_KEYS.email, email);
};

export const saveAdminSession = (token, email, rememberMe = true) => {
  clearSession();
  const storage = getStorage(Boolean(rememberMe));
  
  storage.setItem(STORAGE_KEYS.token, token);
  storage.setItem(STORAGE_KEYS.role, ADMIN_ROLE);
  storage.setItem(STORAGE_KEYS.portal, 'admin');
  storage.setItem(STORAGE_KEYS.remember, String(Boolean(rememberMe)));
  if (email) storage.setItem(STORAGE_KEYS.email, email);
};

export const getSessionToken = () => {
  return localStorage.getItem(STORAGE_KEYS.token) || sessionStorage.getItem(STORAGE_KEYS.token);
};

/** Actualiza solo el access token tras un refresh (misma sesión / mismo storage que ya tenía token). */
export const updateSessionToken = (token) => {
  if (localStorage.getItem(STORAGE_KEYS.token) != null) {
    localStorage.setItem(STORAGE_KEYS.token, token);
  } else if (sessionStorage.getItem(STORAGE_KEYS.token) != null) {
    sessionStorage.setItem(STORAGE_KEYS.token, token);
  } else {
    sessionStorage.setItem(STORAGE_KEYS.token, token);
  }
};

export const getSessionPortal = () => {
  return localStorage.getItem(STORAGE_KEYS.portal) || sessionStorage.getItem(STORAGE_KEYS.portal);
};

const VALID_PORTALS = ['paciente', 'profesional', 'admin'];

/** Storage activo (localStorage o sessionStorage) donde está el token. */
const getActiveStorage = () => {
  if (localStorage.getItem(STORAGE_KEYS.token) != null) return localStorage;
  if (sessionStorage.getItem(STORAGE_KEYS.token) != null) return sessionStorage;
  return null;
};

/**
 * Portal efectivo: prioriza el claim `portal` del JWT (emisión del login) y alinea storage si difiere.
 */
export const getEffectiveSessionPortal = () => {
  const storagePortal = getSessionPortal();
  const jwtPortal = getJwtPortal();

  if (jwtPortal && VALID_PORTALS.includes(jwtPortal)) {
    const activeStorage = getActiveStorage();
    if (activeStorage && storagePortal !== jwtPortal) {
      activeStorage.setItem(STORAGE_KEYS.portal, jwtPortal);
    }
    return jwtPortal;
  }

  return storagePortal;
};

export const syncSessionPortalFromJwt = () => {
  getEffectiveSessionPortal();
};

export const getSessionEmail = () => {
  const stored =
    localStorage.getItem(STORAGE_KEYS.email) || sessionStorage.getItem(STORAGE_KEYS.email);
  if (stored) return stored;
  const token = getSessionToken();
  return token ? getUserEmailFromToken(token) : null;
};

/** Roles del JWT (claim {@code authorities} emitido por ms-usuarios). */
export const getJwtAuthorities = () => {
  const payload = decodeJwtPayload(getSessionToken());
  const authorities = payload?.authorities;
  return Array.isArray(authorities) ? authorities : [];
};

export const hasJwtRole = (role) => getJwtAuthorities().includes(role);

export const getJwtPortal = () => {
  const payload = decodeJwtPayload(getSessionToken());
  const portal = payload?.portal;
  return typeof portal === 'string' ? portal : null;
};

export const hasActiveSession = () => {
  return Boolean(getSessionToken());
};

export const isAdminSession = () => {
  const role = localStorage.getItem(STORAGE_KEYS.role) || sessionStorage.getItem(STORAGE_KEYS.role);
  return Boolean(getSessionToken()) && role === ADMIN_ROLE;
};

export const getDashboardRouteByPortal = (portal) => {
  if (portal === 'profesional') return PROFESIONAL_PATHS.dashboard;
  if (portal === 'paciente') return PACIENTE_PATHS.dashboard;
  if (portal === 'admin') return ADMIN_PATHS.dashboard;
  return HOME_PATH;
};

export const clearSession = () => {
  // Aseguramos que se borren de ambos storages sin importar cómo se guardaron
  Object.values(STORAGE_KEYS).forEach((key) => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  });
};