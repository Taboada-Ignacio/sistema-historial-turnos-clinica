import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

// IMPORTS DE COMPONENTES (Se mantienen igual)
import Landing from './pages/general/Landing';
import AdminRegisterSecret from './pages/general/AdminRegisterSecret';
import Login from './pages/pacientes/Login';
import Register from './pages/pacientes/Register';
import VerificarEmailPaciente from './pages/pacientes/VerificarEmailPaciente';
import RegistroExitosoPaciente from './pages/pacientes/RegistroExitosoPaciente';
import VerificarEmailAdmin from './pages/administracion/VerificarEmailAdmin';
import RegistroExitosoAdmin from './pages/administracion/RegistroExitosoAdmin';
import ConfirmacionError from './pages/general/ConfirmacionError';
import SolicitarCambioPasswordPaciente from './pages/pacientes/SolicitarCambioPasswordPaciente';
import CambiarPasswordPaciente from './pages/pacientes/CambiarPasswordPaciente';
import PacienteRoute from './pages/pacientes/PacienteRoute';
import DashboardPaciente from './pages/pacientes/DashboardPaciente';
import LoginProfesional from './pages/profesionales/LoginProfesional';
import RegistroProfesional from './pages/profesionales/RegistroProfesional';
import VerificarEmailProfesional from './pages/profesionales/VerificarEmailProfesional';
import AprobacionPendiente from './pages/profesionales/AprobacionPendiente';
import SolicitarCambioPasswordProfesional from './pages/profesionales/SolicitarCambioPasswordProfesional';
import CambiarPasswordProfesional from './pages/profesionales/CambiarPasswordProfesional';
import ProfesionalRoute from './pages/profesionales/ProfesionalRoute';
import DashboardProfesional from './pages/profesionales/DashboardProfesional';
import LoginAdministrador from './pages/administracion/LoginAdministrador';
import SolicitarCambioPasswordAdmin from './pages/administracion/SolicitarCambioPasswordAdmin';
import CambiarPasswordAdmin from './pages/administracion/CambiarPasswordAdmin';
import AdminRoute from './pages/administracion/AdminRoute';
import AdminLayout from './pages/administracion/AdminLayout';
import AdminDashboardHome from './pages/administracion/AdminDashboardHome';
import ProfesionalesPendientesPage from './pages/administracion/ProfesionalesPendientesPage';
import ProfesionalPendienteDetallePage from './pages/administracion/ProfesionalPendienteDetallePage';
import AdministrarEntidadesPage from './pages/administracion/AdministrarEntidadesPage';
import AdminCatalogoListaPage from './pages/administracion/AdminCatalogoListaPage';
import AdminCatalogoEditPage from './pages/administracion/AdminCatalogoEditPage';
import AdminTurnosPage from './pages/administracion/AdminTurnosPage';
import AdminHistorialesClinicosPage from './pages/administracion/AdminHistorialesClinicosPage';

// UTILS / PATHS CENTRALIZADOS
import { ADMIN_PATHS, PACIENTE_PATHS, PROFESIONAL_PATHS, HOME_PATH } from './utils/portalPaths';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        
        {/* --- GENERALES --- */}
        <Route path={HOME_PATH} element={<Landing />} />
        <Route path={ADMIN_PATHS.setup} element={<AdminRegisterSecret />} />
        
        {/* --- PACIENTES --- */}
        <Route path={PACIENTE_PATHS.login} element={<Login />} />
        <Route path={PACIENTE_PATHS.registro} element={<Register />} />
        <Route path={PACIENTE_PATHS.verificarEmail} element={<VerificarEmailPaciente />} />
        <Route path={PACIENTE_PATHS.registroExitoso} element={<RegistroExitosoPaciente />} />
        <Route path={PACIENTE_PATHS.confirmacionError} element={<ConfirmacionError />} />
        <Route path={PACIENTE_PATHS.recuperarPassword} element={<SolicitarCambioPasswordPaciente />} />
        <Route path={PACIENTE_PATHS.cambiarPassword} element={<CambiarPasswordPaciente />} />
        
        {/* RUTAS PROTEGIDAS PACIENTE */}
        <Route element={<PacienteRoute />}>
          <Route path={PACIENTE_PATHS.dashboard} element={<DashboardPaciente />} />
        </Route>
        
        {/* --- PROFESIONALES --- */}
        <Route path={PROFESIONAL_PATHS.login} element={<LoginProfesional />} />
        <Route path={PROFESIONAL_PATHS.registro} element={<RegistroProfesional />} />
        <Route path={PROFESIONAL_PATHS.verificarEmail} element={<VerificarEmailProfesional />} />
        <Route path={PROFESIONAL_PATHS.aprobacionPendiente} element={<AprobacionPendiente />} />
        <Route path={PROFESIONAL_PATHS.recuperarPassword} element={<SolicitarCambioPasswordProfesional />} />
        <Route path={PROFESIONAL_PATHS.cambiarPassword} element={<CambiarPasswordProfesional />} />

        {/* RUTAS PROTEGIDAS PROFESIONAL */}
        <Route element={<ProfesionalRoute />}>
          <Route path={PROFESIONAL_PATHS.dashboard} element={<DashboardProfesional />} />
        </Route>

        {/* --- ADMINISTRACIÓN --- */}
        <Route path={ADMIN_PATHS.verificarEmail} element={<VerificarEmailAdmin />} />
        <Route path={ADMIN_PATHS.registroExitoso} element={<RegistroExitosoAdmin />} />
        <Route path={ADMIN_PATHS.login} element={<LoginAdministrador />} />
        <Route path={ADMIN_PATHS.recuperarPassword} element={<SolicitarCambioPasswordAdmin />} />
        <Route path={ADMIN_PATHS.cambiarPassword} element={<CambiarPasswordAdmin />} />
        <Route element={<AdminRoute />}>
          <Route path={ADMIN_PATHS.dashboard} element={<AdminLayout />}>
            <Route index element={<AdminDashboardHome />} />
            <Route path="profesionales-pendientes" element={<ProfesionalesPendientesPage />} />
            <Route path="profesionales-pendientes/:idProfesional" element={<ProfesionalPendienteDetallePage />} />
            <Route path="entidades" element={<AdministrarEntidadesPage />} />
            <Route path="catalogo/:tipo" element={<AdminCatalogoListaPage />} />
            <Route path="catalogo/:tipo/:id" element={<AdminCatalogoEditPage />} />
            <Route path="turnos" element={<AdminTurnosPage />} />
            <Route path="historiales-clinicos" element={<AdminHistorialesClinicosPage />} />
          </Route>
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;