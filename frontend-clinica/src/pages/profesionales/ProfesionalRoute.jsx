import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { hasActiveSession, getSessionPortal, getDashboardRouteByPortal } from '../../utils/auth';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';
import { ProfesionalSessionProvider } from '../../context/ProfesionalSessionContext';

const ProfesionalRoute = () => {
  // 1. Verificamos si hay una sesión activa en general
  if (!hasActiveSession()) {
    return <Navigate to={PROFESIONAL_PATHS.login} replace />;
  }

  // 2. Verificamos que el portal correspondiente sea el de profesional
  const portal = getSessionPortal();
  if (portal !== 'profesional') {
    return <Navigate to={getDashboardRouteByPortal(portal)} replace />;
  }

  // 3. Si todo está ok, renderizamos las rutas hijas con sesión cacheada en memoria
  return (
    <ProfesionalSessionProvider>
      <Outlet />
    </ProfesionalSessionProvider>
  );
};

export default ProfesionalRoute;