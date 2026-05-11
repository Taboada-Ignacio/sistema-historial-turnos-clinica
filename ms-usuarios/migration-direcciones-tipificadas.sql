-- Migración: dirección tipificada (catálogo) en lugar de texto libre en usuarios.
-- Ejecutar en PostgreSQL sobre una base que ya tenga localidades y usuarios con columna direccion VARCHAR.

CREATE TABLE IF NOT EXISTS direcciones (
    id_direccion BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(500) NOT NULL,
    id_localidad BIGINT NOT NULL REFERENCES localidades(id_localidad),
    UNIQUE (nombre, id_localidad)
);

-- Una fila por localidad para poder enlazar usuarios existentes
INSERT INTO direcciones (nombre, id_localidad)
SELECT 'CENTRO - Dirección por defecto (migración)', l.id_localidad
FROM localidades l
WHERE NOT EXISTS (
    SELECT 1 FROM direcciones d
    WHERE d.id_localidad = l.id_localidad
      AND d.nombre = 'CENTRO - Dirección por defecto (migración)'
);

ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS id_direccion BIGINT REFERENCES direcciones(id_direccion);

UPDATE usuarios u
SET id_direccion = d.id_direccion
FROM direcciones d
WHERE u.id_direccion IS NULL
  AND d.id_localidad = u.id_localidad
  AND d.nombre = 'CENTRO - Dirección por defecto (migración)';

ALTER TABLE usuarios ALTER COLUMN id_direccion SET NOT NULL;

ALTER TABLE usuarios DROP COLUMN IF EXISTS direccion;
