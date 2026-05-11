/** Edad mínima para registrarse en portales públicos. */
export const MIN_REGISTRATION_AGE = 18;

/**
 * Fecha máxima de nacimiento (YYYY-MM-DD) para cumplir `minAge` años al día de hoy (hora local).
 */
export function getMaxBirthDateString(minAge = MIN_REGISTRATION_AGE) {
  const d = new Date();
  d.setFullYear(d.getFullYear() - minAge);
  const y = d.getFullYear();
  const m = String(d.getMonth() + 1).padStart(2, '0');
  const day = String(d.getDate()).padStart(2, '0');
  return `${y}-${m}-${day}`;
}

/**
 * @param {string} birthDateStr valor del input type="date" (YYYY-MM-DD)
 */
export function isAtLeastAge(birthDateStr, minAge = MIN_REGISTRATION_AGE) {
  if (!birthDateStr) return false;
  const parts = birthDateStr.split('-').map(Number);
  if (parts.length !== 3 || parts.some(Number.isNaN)) return false;
  const [y, mo, d] = parts;
  const birth = new Date(y, mo - 1, d);
  const today = new Date();
  let age = today.getFullYear() - birth.getFullYear();
  const monthDiff = today.getMonth() - birth.getMonth();
  if (monthDiff < 0 || (monthDiff === 0 && today.getDate() < birth.getDate())) {
    age -= 1;
  }
  return age >= minAge;
}
