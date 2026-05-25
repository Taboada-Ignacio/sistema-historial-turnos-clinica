import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import {
  hasActiveSession,
  getEffectiveSessionPortal,
  getDashboardRouteByPortal,
  hasJwtRole,
  clearSession,
} from '../../utils/auth';
import { PACIENTE_PATHS } from '../../utils/portalPaths';
import { PacienteSessionProvider } from '../../context/PacienteSessionContext';

const PacienteRoute = () => {
  if (!hasActiveSession()) {
    return <Navigate to={PACIENTE_PATHS.login} replace />;
  }

  const portal = getEffectiveSessionPortal();
  if (portal !== 'paciente') {
    const destino = portal ? getDashboardRouteByPortal(portal) : PACIENTE_PATHS.login;
    return <Navigate to={destino} replace />;
  }

  if (!hasJwtRole('ROLE_PACIENTE')) {
    clearSession();
    return <Navigate to={PACIENTE_PATHS.login} replace />;
  }

  return (
    <PacienteSessionProvider>
      <Outlet />
    </PacienteSessionProvider>
  );
};

export default PacienteRoute;