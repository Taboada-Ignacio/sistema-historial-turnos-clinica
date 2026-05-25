import React, { useEffect, useState } from 'react';
import { useLocation, useNavigate, useParams } from 'react-router-dom';
import clienteAxios from '../../api/axiosConfig';
import AdminConfirmModal from '../../components/AdminConfirmModal';
import AdminPasswordConfirmModal from '../../components/AdminPasswordConfirmModal';
import { adminApiErrorMessage } from '../../utils/adminApiError';
import { formatSexoLabel } from '../../components/SexoSelectField';
import { ADMIN_PATHS } from '../../utils/portalPaths';

function formatFechaNacimiento(value) {
  if (value == null || value === '') return '—';
  if (typeof value === 'string') {
    const d = value.slice(0, 10);
    if (/^\d{4}-\d{2}-\d{2}$/.test(d)) {
      const [y, m, day] = d.split('-');
      return `${day}/${m}/${y}`;
    }
    return value;
  }
  return String(value);
}

const AdminPacienteDetallePage = () => {
  const { idPaciente } = useParams();
  const navigate = useNavigate();
  const location = useLocation();
  const flashMensaje = location.state?.mensaje;
  const [paciente, setPaciente] = useState(null);
  const [loading, setLoading] = useState(true);
  const [error, setError] = useState('');
  const [showConfirmDelete, setShowConfirmDelete] = useState(false);
  const [showPwd, setShowPwd] = useState(false);
  const [pwd, setPwd] = useState('');
  const [deleting, setDeleting] = useState(false);

  useEffect(() => {
    const fetchPaciente = async () => {
      setLoading(true);
      setError('');
      try {
        const { data } = await clienteAxios.get(`/usuarios/api/pacientes/${idPaciente}`);
        setPaciente(data);
      } catch (err) {
        setError(adminApiErrorMessage(err, 'No se pudo cargar el paciente.'));
        setPaciente(null);
      } finally {
        setLoading(false);
      }
    };
    fetchPaciente();
  }, [idPaciente]);

  const iniciarEliminacion = () => {
    setShowConfirmDelete(true);
  };

  const continuarEliminacionConPassword = () => {
    setShowConfirmDelete(false);
    setPwd('');
    setShowPwd(true);
  };

  const confirmarEliminacion = async () => {
    if (!pwd.trim()) {
      setError('Ingresá tu contraseña para confirmar la eliminación.');
      return;
    }
    setDeleting(true);
    setError('');
    try {
      await clienteAxios.post('/usuarios/api/seguridad/verificar-password-actual', { password: pwd });
      await clienteAxios.delete(`/usuarios/api/pacientes/${idPaciente}`);
      setShowPwd(false);
      navigate(ADMIN_PATHS.pacientesBuscar, {
        state: { mensaje: 'Paciente eliminado correctamente.' },
      });
    } catch (err) {
      setError(adminApiErrorMessage(err, 'No se pudo eliminar el paciente.'));
    } finally {
      setDeleting(false);
    }
  };

  if (loading) {
    return <div className="bg-white rounded-xl border border-slate-200 p-6">Cargando…</div>;
  }

  if (!paciente) {
    return (
      <div className="bg-white rounded-xl border border-slate-200 p-6">
        <p className="text-red-600 mb-4">{error || 'Paciente no encontrado.'}</p>
        <button
          type="button"
          onClick={() => navigate(ADMIN_PATHS.pacientesBuscar)}
          className="px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 font-semibold hover:bg-slate-50"
        >
          Volver a consultar pacientes
        </button>
      </div>
    );
  }

  const rolesList = paciente.roles ? Array.from(paciente.roles).sort() : [];

  return (
    <section className="bg-white rounded-xl border border-slate-200 p-6">
      <button
        type="button"
        onClick={() => navigate(ADMIN_PATHS.pacientesBuscar)}
        className="mb-4 px-5 py-2.5 rounded-lg border border-slate-300 text-slate-800 text-sm font-semibold hover:bg-slate-50"
      >
        ← Volver a consultar pacientes
      </button>

      <h2 className="text-2xl font-black text-slate-900 mb-1">
        {paciente.apellido}, {paciente.nombre}
      </h2>
      <p className="text-slate-500 text-sm mb-6">ID usuario: {paciente.idUsuario}</p>

      {flashMensaje && (
        <div className="mb-4 rounded-lg border border-emerald-200 bg-emerald-50 px-4 py-3 text-sm text-emerald-900">
          {flashMensaje}
        </div>
      )}

      {error && (
        <div className="mb-4 rounded-lg border border-red-200 bg-red-50 px-4 py-3 text-sm text-red-700">
          {error}
        </div>
      )}

      <div className="space-y-8 max-w-3xl">
        <div>
          <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">Contacto y documento</h3>
          <dl className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 text-sm">
            <div>
              <dt className="text-slate-500">Email</dt>
              <dd className="font-medium text-slate-900 break-all">{paciente.email}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Teléfono</dt>
              <dd className="font-medium text-slate-900">{paciente.telefono ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-slate-500">DNI</dt>
              <dd className="font-medium text-slate-900">{paciente.dni ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Fecha de nacimiento</dt>
              <dd className="font-medium text-slate-900">{formatFechaNacimiento(paciente.fechaNacimiento)}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Sexo</dt>
              <dd className="font-medium text-slate-900">{formatSexoLabel(paciente.sexo)}</dd>
            </div>
          </dl>
        </div>

        <div>
          <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">Obra social y estado</h3>
          <dl className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 text-sm">
            <div>
              <dt className="text-slate-500">Obra social</dt>
              <dd className="font-medium text-slate-900">{paciente.nombreObraSocial ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Nº afiliado</dt>
              <dd className="font-medium text-slate-900">{paciente.numeroAfiliado ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Estado de cuenta</dt>
              <dd className="font-medium text-slate-900">{paciente.estadoActual ?? '—'}</dd>
            </div>
          </dl>
        </div>

        <div>
          <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">Ubicación</h3>
          <dl className="grid grid-cols-1 sm:grid-cols-2 gap-x-6 gap-y-2 text-sm">
            <div className="sm:col-span-2">
              <dt className="text-slate-500">Dirección</dt>
              <dd className="font-medium text-slate-900">{paciente.direccion?.trim() || '—'}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Localidad</dt>
              <dd className="font-medium text-slate-900">{paciente.nombreLocalidad ?? '—'}</dd>
            </div>
            <div>
              <dt className="text-slate-500">Provincia</dt>
              <dd className="font-medium text-slate-900">{paciente.nombreProvincia ?? '—'}</dd>
            </div>
          </dl>
        </div>

        <div>
          <h3 className="text-xs font-bold uppercase tracking-wide text-slate-500 mb-3">Roles</h3>
          {rolesList.length > 0 ? (
            <ul className="flex flex-wrap gap-2">
              {rolesList.map((r) => (
                <li key={r} className="px-2.5 py-1 rounded-md bg-slate-100 text-slate-800 text-xs font-mono">
                  {r}
                </li>
              ))}
            </ul>
          ) : (
            <p className="text-sm text-slate-600">—</p>
          )}
        </div>
      </div>

      <div className="mt-10 flex flex-wrap gap-3 border-t border-slate-100 pt-6">
        <button
          type="button"
          onClick={() => navigate(ADMIN_PATHS.pacienteEditar(idPaciente))}
          className="px-5 py-2.5 rounded-lg bg-emerald-700 text-white font-semibold hover:bg-emerald-600"
        >
          Modificar datos
        </button>
        <button
          type="button"
          onClick={iniciarEliminacion}
          className="px-5 py-2.5 rounded-lg border border-red-300 text-red-800 font-semibold hover:bg-red-50"
        >
          Eliminar paciente
        </button>
      </div>

      <AdminConfirmModal
        open={showConfirmDelete}
        title="Eliminar paciente"
        description={`¿Eliminar a ${paciente.apellido}, ${paciente.nombre} de forma permanente?\n\nSe borrará su cuenta y datos asociados. Esta acción no se puede deshacer.`}
        cancelLabel="Cancelar"
        confirmLabel="Sí, eliminar paciente"
        confirmClassName="bg-red-700 hover:bg-red-600"
        onCancel={() => setShowConfirmDelete(false)}
        onConfirm={continuarEliminacionConPassword}
      />

      <AdminPasswordConfirmModal
        open={showPwd}
        title="Confirmar eliminación"
        description="Ingresá tu contraseña de administrador para eliminar este paciente."
        password={pwd}
        onPasswordChange={setPwd}
        onCancel={() => {
          setShowPwd(false);
          setPwd('');
        }}
        onConfirm={confirmarEliminacion}
        submitting={deleting}
        confirmLabel="Confirmar eliminación"
        submittingLabel="Eliminando paciente…"
        confirmClassName="bg-red-700 hover:bg-red-600"
      />
    </section>
  );
};

export default AdminPacienteDetallePage;
