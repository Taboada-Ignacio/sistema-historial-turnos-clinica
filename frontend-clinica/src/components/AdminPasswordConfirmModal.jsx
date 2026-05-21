import React from 'react';

/**
 * Modal reutilizable: confirmar acción sensible con contraseña del administrador.
 */
const AdminPasswordConfirmModal = ({
  open,
  title = 'Confirmar con contraseña',
  description = 'Ingresá tu contraseña de administrador para continuar.',
  password,
  onPasswordChange,
  onCancel,
  onConfirm,
  submitting = false,
  confirmLabel = 'Confirmar',
  submittingLabel = 'Procesando…',
  confirmClassName = 'bg-emerald-600 hover:bg-emerald-700',
}) => {
  if (!open) return null;

  return (
    <div className="fixed inset-0 z-50 flex items-center justify-center bg-black/40 p-4">
      <div className="w-full max-w-sm rounded-2xl bg-white p-6 shadow-xl border border-slate-200">
        <h3 className="font-bold text-slate-900 mb-2">{title}</h3>
        <p className="text-sm text-slate-600 mb-4">{description}</p>
        <input
          type="password"
          autoFocus
          value={password}
          onChange={(e) => onPasswordChange(e.target.value)}
          className="w-full rounded-lg border border-slate-200 px-3 py-2 mb-4"
          placeholder="Contraseña"
          autoComplete="current-password"
        />
        <div className="flex justify-end gap-2">
          <button
            type="button"
            onClick={onCancel}
            disabled={submitting}
            className="px-4 py-2 rounded-lg border border-slate-200 text-slate-700 font-semibold hover:bg-slate-50"
          >
            Cancelar
          </button>
          <button
            type="button"
            onClick={onConfirm}
            disabled={submitting}
            className={`px-4 py-2 rounded-lg text-white font-semibold disabled:opacity-50 ${confirmClassName}`}
          >
            {submitting ? submittingLabel : confirmLabel}
          </button>
        </div>
      </div>
    </div>
  );
};

export default AdminPasswordConfirmModal;
