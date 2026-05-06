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

export const getSessionPortal = () => {
  return localStorage.getItem(STORAGE_KEYS.portal) || sessionStorage.getItem(STORAGE_KEYS.portal);
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
  if (portal === 'admin') return ADMIN_PATHS.profesionalesPendientes;
  
  return HOME_PATH; // Fallback centralizado
};

export const clearSession = () => {
  // Aseguramos que se borren de ambos storages sin importar cómo se guardaron
  Object.values(STORAGE_KEYS).forEach((key) => {
    localStorage.removeItem(key);
    sessionStorage.removeItem(key);
  });
};