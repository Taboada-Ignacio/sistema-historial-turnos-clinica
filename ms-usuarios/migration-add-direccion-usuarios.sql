-- Obsoleto para el esquema actual: la dirección tipificada vive en `direcciones` + `usuarios.id_direccion`.
-- En bases antiguas solo con VARCHAR, aplicar primero `migration-direcciones-tipificadas.sql`.
-- (Historial) Ejecutar una vez en bases ya creadas antes de agregar el campo en Java (si ddl-auto no aplica).
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS direccion VARCHAR(255);
