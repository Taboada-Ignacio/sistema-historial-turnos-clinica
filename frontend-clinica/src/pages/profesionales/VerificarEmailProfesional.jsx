import React from 'react';
import { useLocation } from 'react-router-dom';
import VerificarEmailForm from '../../components/VerificarEmailForm';
import { PROFESIONAL_PATHS } from '../../utils/portalPaths';

const VerificarEmailProfesional = () => {
  const location = useLocation();
  const email = location.state?.email || '';

  return (
    <VerificarEmailForm
      email={email}
      apiRole="profesionales"
      successPath={PROFESIONAL_PATHS.aprobacionPendiente}
      variant="profesional"
    />
  );
};

export default VerificarEmailProfesional;
