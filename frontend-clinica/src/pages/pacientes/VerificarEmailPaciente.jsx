import React from 'react';
import { useLocation } from 'react-router-dom';
import VerificarEmailForm from '../../components/VerificarEmailForm';
import { PACIENTE_PATHS } from '../../utils/portalPaths';

const VerificarEmailPaciente = () => {
  const location = useLocation();
  const email = location.state?.email || '';

  return (
    <VerificarEmailForm
      email={email}
      apiRole="pacientes"
      successPath={PACIENTE_PATHS.registroExitoso}
      variant="paciente"
    />
  );
};

export default VerificarEmailPaciente;
