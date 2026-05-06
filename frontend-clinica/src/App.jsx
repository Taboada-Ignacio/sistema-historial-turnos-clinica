import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';

// ==========================================
// 1. PÁGINAS GENERALES / INICIO
// ==========================================
import Landing from './pages/general/Landing';
import Dashboard from './pages/general/Dashboard'; // Más adelante quizás lo dividamos por rol
import AdminRegisterSecret from './pages/general/AdminRegisterSecret';

// ==========================================
// 2. DOMINIO: PACIENTES
// ==========================================
import Login from './pages/pacientes/Login';
import Register from './pages/pacientes/Register'; // <-- ¡Acá estaba el error! Faltaba /pacientes/
import ConfirmAccount from './pages/pacientes/ConfirmAccount';

// ==========================================
// 3. DOMINIO: PROFESIONALES
// ==========================================
import LoginProfesional from './pages/profesionales/LoginProfesional';
import RegistroProfesional from './pages/profesionales/RegistroProfesional';
import VerificarEmailProfesional from './pages/profesionales/VerificarEmailProfesional';
import AprobacionPendiente from './pages/profesionales/AprobacionPendiente';
import LoginAdministrador from './pages/administracion/LoginAdministrador';
import AdminRoute from './pages/administracion/AdminRoute';
import AdminLayout from './pages/administracion/AdminLayout';
import AdminDashboardHome from './pages/administracion/AdminDashboardHome';
import ProfesionalesPendientesPage from './pages/administracion/ProfesionalesPendientesPage';
import ProfesionalPendienteDetallePage from './pages/administracion/ProfesionalPendienteDetallePage';
import AdministrarEntidadesPage from './pages/administracion/AdministrarEntidadesPage';
import AdminTurnosPage from './pages/administracion/AdminTurnosPage';
import AdminHistorialesClinicosPage from './pages/administracion/AdminHistorialesClinicosPage';
import { ADMIN_PATHS } from './utils/adminPaths';

function App() {
  return (
    <BrowserRouter>
      <Routes>
        
        {/* --- GENERALES --- */}
        <Route path="/" element={<Landing />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path={ADMIN_PATHS.setup} element={<AdminRegisterSecret />} />
        
        {/* --- PACIENTES --- */}
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Register />} />
        <Route path="/confirmar-cuenta" element={<ConfirmAccount />} />
        
        {/* --- PROFESIONALES --- */}
        <Route path="/login-profesional" element={<LoginProfesional />} />
        <Route path="/registro-profesional" element={<RegistroProfesional />} />
        <Route path="/verificar-email-profesional" element={<VerificarEmailProfesional />} />
        <Route path="/aprobacion-pendiente" element={<AprobacionPendiente />} />

        {/* --- ADMINISTRACIÓN --- */}
        <Route path={ADMIN_PATHS.login} element={<LoginAdministrador />} />
        <Route element={<AdminRoute />}>
          <Route path={ADMIN_PATHS.dashboard} element={<AdminLayout />}>
            <Route index element={<AdminDashboardHome />} />
            <Route path="profesionales-pendientes" element={<ProfesionalesPendientesPage />} />
            <Route path="profesionales-pendientes/:idProfesional" element={<ProfesionalPendienteDetallePage />} />
            <Route path="entidades" element={<AdministrarEntidadesPage />} />
            <Route path="turnos" element={<AdminTurnosPage />} />
            <Route path="historiales-clinicos" element={<AdminHistorialesClinicosPage />} />
          </Route>
        </Route>

      </Routes>
    </BrowserRouter>
  );
}

export default App;