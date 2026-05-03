import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Register from './pages/Register';
import AdminRegisterSecret from './pages/AdminRegisterSecret';
import ConfirmAccount from './pages/ConfirmAccount'; // Nueva importación

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/registro" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        
        {/* Ruta para la validación de cuenta vía email */}
        <Route path="/confirmar-cuenta" element={<ConfirmAccount />} />
        
        {/* Ruta oculta para el setup inicial de administradores */}
        <Route path="/secret-admin-setup" element={<AdminRegisterSecret />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;