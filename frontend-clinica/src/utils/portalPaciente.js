/** Código API cuando un profesional ingresa al portal paciente y no puede editar perfil vía paciente. */
export const PERFIL_PACIENTE_NO_DISPONIBLE = 'PERFIL_PACIENTE_NO_DISPONIBLE';

export const TIPO_CUENTA_PROFESIONAL_EN_PORTAL_PACIENTE = 'PROFESIONAL_EN_PORTAL_PACIENTE';

export const esProfesionalEnPortalPaciente = (sesion) =>
  sesion?.tipoCuenta === TIPO_CUENTA_PROFESIONAL_EN_PORTAL_PACIENTE || sesion?.perfilEditable === false;

export const mensajePerfilSoloPortalProfesional =
  'Tu perfil se gestiona desde el portal profesional. Cerrá sesión e ingresá en el login de profesional para editar tus datos.';

export const apiErrorCode = (err) => err?.response?.data?.code ?? null;
