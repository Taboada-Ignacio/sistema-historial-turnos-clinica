import React from 'react';
import { BrowserRouter, Routes, Route } from 'react-router-dom';
import Login from './pages/Login';
import Dashboard from './pages/Dashboard';
import Register from './pages/Register';
import AdminRegisterSecret from './pages/AdminRegisterSecret'; // Importación nueva

function App() {
  return (
    <BrowserRouter>
      <Routes>
        <Route path="/" element={<Login />} />
        <Route path="/registro" element={<Register />} />
        <Route path="/dashboard" element={<Dashboard />} />
        
        {/* Ruta oculta (sin links apuntando aquí) */}
        <Route path="/secret-admin-setup" element={<AdminRegisterSecret />} />
      </Routes>
    </BrowserRouter>
  );
}

export default App;