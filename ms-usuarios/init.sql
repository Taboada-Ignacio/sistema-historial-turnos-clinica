-- 1. Crear tablas solo si no existen
CREATE TABLE IF NOT EXISTS roles (
    id_rol SERIAL PRIMARY KEY,
    descripcion VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS especialidades (
    id_especialidad SERIAL PRIMARY KEY,
    descripcion VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS obras_sociales (
    id_obra_social SERIAL PRIMARY KEY,
    descripcion VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS provincias (
    id_provincia SERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS localidades (
    id_localidad SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_provincia INT REFERENCES provincias(id_provincia),
    UNIQUE(nombre, id_provincia) -- Evita duplicar la misma ciudad en la misma provincia
);

---

-- 2. Carga de datos con validación de existencia (ON CONFLICT)
-- Usamos 'descripcion' o 'nombre' como llave para saber si ya están

-- Roles
INSERT INTO roles (descripcion) VALUES ('PROFESIONAL'), ('PACIENTE'), ('ADMINISTRADOR')
ON CONFLICT (descripcion) DO NOTHING;

-- Especialidades
INSERT INTO especialidades (descripcion) VALUES ('MÉDICO'), ('COSMIATRA'), ('ODÓNTOLOGO'), ('PSICÓLOGO')
ON CONFLICT (descripcion) DO NOTHING;

-- Obras Sociales
INSERT INTO obras_sociales (descripcion) VALUES ('LA CAJA'), ('OSDE'), ('NO POSEE')
ON CONFLICT (descripcion) DO NOTHING;

-- Provincias
INSERT INTO provincias (nombre) VALUES ('CORDOBA'), ('SANTA CRUZ')
ON CONFLICT (nombre) DO NOTHING;

-- Localidades (Usamos subconsultas para no hardcodear IDs y ON CONFLICT para el par nombre/provincia)
INSERT INTO localidades (nombre, id_provincia) 
VALUES 
    ('CAPILLA DEL MONTE', (SELECT id_provincia FROM provincias WHERE nombre = 'CORDOBA')),
    ('CORDOBA CAPITAL', (SELECT id_provincia FROM provincias WHERE nombre = 'CORDOBA')),
    ('JESUS MARIA', (SELECT id_provincia FROM provincias WHERE nombre = 'CORDOBA')),
    ('PUERTO SAN JULIAN', (SELECT id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ')),
    ('RIO GALLEGOS', (SELECT id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ')),
    ('CALAFATE', (SELECT id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ'))
ON CONFLICT (nombre, id_provincia) DO NOTHING;