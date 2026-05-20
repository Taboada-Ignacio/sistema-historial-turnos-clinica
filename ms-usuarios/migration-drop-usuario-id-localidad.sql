-- Ejecutar en bases existentes que aún tengan usuarios.id_localidad.
-- Requiere que cada usuario tenga id_direccion coherente con su localidad previa.

ALTER TABLE usuarios DROP COLUMN IF EXISTS id_localidad;
