-- Renombra la especialidad legada MEDICO → MEDICINA GENERAL (idempotente).
UPDATE especialidades
SET descripcion = 'MEDICINA GENERAL'
WHERE descripcion = 'MEDICO'
  AND NOT EXISTS (SELECT 1 FROM especialidades WHERE descripcion = 'MEDICINA GENERAL');
