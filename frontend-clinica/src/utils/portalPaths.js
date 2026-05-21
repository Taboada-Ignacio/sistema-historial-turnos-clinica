// Ruta principal / Landing
export const HOME_PATH = '/';

/** Pantalla de error al abrir o usar un enlace de recuperación de contraseña (?tipo= & ?motivo=). */
export const RECUPERACION_PASSWORD_ERROR_PATH = '/recuperacion-password-error';

// Rutas del Portal Paciente
export const PACIENTE_PATHS = {
  login: '/login',
  registro: '/registro',
  verificarEmail: '/verificar-email-paciente',
  registroExitoso: '/registro-exitoso-paciente',
  confirmacionError: '/confirmacion-error',
  recuperarPassword: '/recuperar-password/paciente',
  cambiarPassword: '/cambiar-password/paciente',
  dashboard: '/dashboard-paciente',
  turnos: '/dashboard-paciente/turnos',
  perfil: '/dashboard-paciente/perfil',
  historial: '/dashboard-paciente/historial',
};

// Rutas del Portal Profesional
export const PROFESIONAL_PATHS = {
  login: '/login-profesional',
  registro: '/registro-profesional',
  verificarEmail: '/verificar-email-profesional',
  aprobacionPendiente: '/aprobacion-pendiente',
  recuperarPassword: '/recuperar-password/profesional',
  cambiarPassword: '/cambiar-password/profesional',
  dashboard: '/dashboard-profesional',
  turnos: '/dashboard-profesional/turnos',
  pacientes: '/dashboard-profesional/pacientes',
  perfil: '/dashboard-profesional/perfil',
};

// Rutas del Panel de Administración
export const ADMIN_PATHS = {
  setup: '/internal/admin/bootstrap-setup',
  recuperarPassword: '/recuperar-password/admin',
  cambiarPassword: '/cambiar-password/admin',
  verificarEmail: '/verificar-email-admin',
  registroExitoso: '/registro-exitoso-admin',
  login: '/internal/admin/auth',
  dashboard: '/internal/admin/panel',
  profesionalesPendientes: '/internal/admin/panel/profesionales-pendientes',
  profesionalPendienteRechazar: (id) => `/internal/admin/panel/profesionales-pendientes/${id}/rechazar`,
  entidades: '/internal/admin/panel/entidades',
  pacientesBuscar: '/internal/admin/panel/entidades/pacientes',
  pacienteDetalle: (id) => `/internal/admin/panel/entidades/pacientes/${id}`,
  pacienteEditar: (id) => `/internal/admin/panel/entidades/pacientes/${id}/editar`,
  profesionalesBuscar: '/internal/admin/panel/entidades/profesionales',
  profesionalDetalle: (id) => `/internal/admin/panel/entidades/profesionales/${id}`,
  profesionalEditar: (id) => `/internal/admin/panel/entidades/profesionales/${id}/editar`,
  administradoresLista: '/internal/admin/panel/entidades/administradores',
  administradorDetalle: (id) => `/internal/admin/panel/entidades/administradores/${id}`,
  administradorEditar: (id) => `/internal/admin/panel/entidades/administradores/${id}/editar`,
  turnos: '/internal/admin/panel/turnos',
  historiales: '/internal/admin/panel/historiales-clinicos',
};