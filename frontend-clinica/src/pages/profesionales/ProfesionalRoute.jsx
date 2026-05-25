import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import {
  hasActiveSession,
  getEffectiveSessionPortal,
  getDashboardRouteByPortal,
  hasJwtRole,
  clearSession,
} from '../../utils/auth';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';
import { ProfesionalSessionProvider } from '../../context/ProfesionalSessionContext';

const ProfesionalRoute = () => {
  if (!hasActiveSession()) {
    return <Navigate to={PROFESIONAL_PATHS.login} replace />;
  }

  const portal = getEffectiveSessionPortal();
  if (portal !== 'profesional') {
    const destino = portal ? getDashboardRouteByPortal(portal) : PROFESIONAL_PATHS.login;
    return <Navigate to={destino} replace />;
  }

  if (!hasJwtRole('ROLE_PROFESIONAL')) {
    clearSession();
    return <Navigate to={PROFESIONAL_PATHS.login} replace />;
  }

  return (
    <ProfesionalSessionProvider>
      <Outlet />
    </ProfesionalSessionProvider>
  );
};

export default ProfesionalRoute;