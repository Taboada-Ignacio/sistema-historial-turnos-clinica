-- Ejecutar después de cargar localidades (02-argentina-geo-data.sql)
INSERT INTO direcciones (nombre, id_localidad)
SELECT 'SIN ESPECIFICAR', l.id_localidad
FROM localidades l
WHERE NOT EXISTS (
    SELECT 1 FROM direcciones d
    WHERE d.id_localidad = l.id_localidad AND d.nombre = 'SIN ESPECIFICAR'
);
