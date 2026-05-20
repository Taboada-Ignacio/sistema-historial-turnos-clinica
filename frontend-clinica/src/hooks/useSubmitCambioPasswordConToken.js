import { useRef, useState, useCallback } from 'react';
import { useNavigate } from 'react-router-dom';
import { clienteAxiosPublic } from '../api/axiosConfig';
import { manejarErrorCambioPasswordConToken } from '../utils/recuperacionPasswordError';

/** Candado a nivel módulo: evita dos POST paralelos aunque React dispare dos handlers en el mismo ms. */
const globalApiLock = { inFlight: false };

/**
 * @param {'paciente'|'profesional'|'admin'} tipo
 * @param {string} loginPath
 * @param {string} apiSuffix
 */
export function useSubmitCambioPasswordConToken(tipo, loginPath, apiSuffix) {
  const navigate = useNavigate();
  const [isSubmitting, setIsSubmitting] = useState(false);
  const [completado, setCompletado] = useState(false);
  const [error, setError] = useState('');
  const [success, setSuccess] = useState('');
  const completadoRef = useRef(false);

  const submitPasswordChange = useCallback(
    async (lockRef, { token, passwordNueva, confirmPasswordNueva }) => {
      if (completadoRef.current) {
        return { completed: true };
      }

      if (globalApiLock.inFlight) {
        return { completed: false, skipped: true };
      }

      setIsSubmitting(true);
      setError('');
      setSuccess('');

      if (!token) {
        setIsSubmitting(false);
        setError('Token inválido o ausente.');
        return { completed: false };
      }
      if (passwordNueva !== confirmPasswordNueva) {
        setIsSubmitting(false);
        setError('La confirmación de contraseña no coincide.');
        return { completed: false };
      }

      globalApiLock.inFlight = true;

      try {
        const response = await clienteAxiosPublic.post(
          `/usuarios/api/auth/cambiar-password-con-token/${apiSuffix}`,
          { token, passwordNueva }
        );
        completadoRef.current = true;
        setCompletado(true);
        setSuccess(response.data?.message || 'Contraseña actualizada correctamente.');
        setTimeout(() => navigate(loginPath), 1500);
        return { completed: true };
      } catch (err) {
        manejarErrorCambioPasswordConToken(err, {
          yaCompletado: completadoRef.current,
          navigate,
          tipo,
          setError,
          setSuccess,
          loginPath,
        });
        return { completed: false };
      } finally {
        if (!completadoRef.current) {
          globalApiLock.inFlight = false;
          setIsSubmitting(false);
          if (lockRef) {
            lockRef.current = false;
          }
        }
      }
    },
    [apiSuffix, loginPath, navigate, tipo]
  );

  return {
    isSubmitting,
    completado,
    error,
    success,
    submitPasswordChange,
    formDisabled: isSubmitting || completado,
  };
}
