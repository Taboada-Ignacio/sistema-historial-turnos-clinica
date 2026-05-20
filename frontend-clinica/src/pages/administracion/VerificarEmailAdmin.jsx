import React from 'react';
import { useLocation } from 'react-router-dom';
import VerificarEmailForm from '../../components/VerificarEmailForm';
import { ADMIN_PATHS } from '../../utils/portalPaths';

const VerificarEmailAdmin = () => {
  const location = useLocation();
  const email = location.state?.email || '';

  return (
    <VerificarEmailForm
      email={email}
      apiRole="administradores"
      successPath={ADMIN_PATHS.registroExitoso}
      variant="admin"
    />
  );
};

export default VerificarEmailAdmin;
