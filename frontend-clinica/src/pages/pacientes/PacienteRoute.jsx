import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { hasActiveSession, getSessionPortal, getDashboardRouteByPortal } from '../../utils/auth';
import { PACIENTE_PATHS } from '../../utils/portalPaths';

const PacienteRoute = () => {
  // 1. Verificamos si hay una sesión activa en general
  if (!hasActiveSession()) {
    return <Navigate to={PACIENTE_PATHS.login} replace />;
  }

  // 2. Verificamos que el portal correspondiente sea el de paciente
  const portal = getSessionPortal();
  if (portal !== 'paciente') {
    return <Navigate to={getDashboardRouteByPortal(portal)} replace />;
  }

  // 3. Si todo está ok, renderizamos las rutas hijas
  return <Outlet />;
};

export default PacienteRoute;