import React from 'react';
import { Navigate, Outlet } from 'react-router-dom';
import { isAdminSession } from '../../utils/auth';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const AdminRoute = () => {
  if (!isAdminSession()) {
    return <Navigate to={ADMIN_PATHS.login} replace />;
  }

  return <Outlet />;
};

export default AdminRoute;
