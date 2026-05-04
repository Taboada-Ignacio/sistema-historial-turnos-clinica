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

function App() {
  return (
    <BrowserRouter>
      <Routes>
        
        {/* --- GENERALES --- */}
        <Route path="/" element={<Landing />} />
        <Route path="/dashboard" element={<Dashboard />} />
        <Route path="/secret-admin-setup" element={<AdminRegisterSecret />} />
        
        {/* --- PACIENTES --- */}
        <Route path="/login" element={<Login />} />
        <Route path="/registro" element={<Register />} />
        <Route path="/confirmar-cuenta" element={<ConfirmAccount />} />
        
        {/* --- PROFESIONALES --- */}
        <Route path="/login-profesional" element={<LoginProfesional />} />
        <Route path="/registro-profesional" element={<RegistroProfesional />} />
        <Route path="/verificar-email-profesional" element={<VerificarEmailProfesional />} />
        <Route path="/aprobacion-pendiente" element={<AprobacionPendiente />} />

      </Routes>
    </BrowserRouter>
  );
}

export default App;