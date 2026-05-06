export const ADMIN_ROLE = 'ADMIN';

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
  } catch (error) {
    return null;
  }
};

export const getUserEmailFromToken = (token) => {
  const payload = decodeJwtPayload(token);
  return payload?.sub || null;
};

export const saveAdminSession = (token, email) => {
  localStorage.setItem('token', token);
  localStorage.setItem('app_role', ADMIN_ROLE);
  localStorage.setItem('app_email', email);
};

export const clearSession = () => {
  localStorage.removeItem('token');
  localStorage.removeItem('app_role');
  localStorage.removeItem('app_email');
};

export const isAdminSession = () => {
  return Boolean(localStorage.getItem('token')) && localStorage.getItem('app_role') === ADMIN_ROLE;
};
