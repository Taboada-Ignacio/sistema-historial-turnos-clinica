-- ==========================================
-- NIVEL 1: Tablas Maestras (Sin dependencias)
-- ==========================================

CREATE TABLE IF NOT EXISTS roles (
    id_rol BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(50) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS especialidades (
    id_especialidad BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS obras_sociales (
    id_obra_social BIGSERIAL PRIMARY KEY,
    descripcion VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS provincias (
    id_provincia BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) UNIQUE NOT NULL
);

CREATE TABLE IF NOT EXISTS estados (
    id_estado BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(50) UNIQUE NOT NULL
);

-- ==========================================
-- NIVEL 2: Tablas con dependencias simples
-- ==========================================

CREATE TABLE IF NOT EXISTS localidades (
    id_localidad BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    id_provincia BIGINT REFERENCES provincias(id_provincia),
    UNIQUE(nombre, id_provincia)
);

-- ==========================================
-- NIVEL 3: Tabla Padre (Herencia JOINED)
-- ==========================================

CREATE TABLE IF NOT EXISTS usuarios (
    id_usuario BIGSERIAL PRIMARY KEY,
    email VARCHAR(100) UNIQUE NOT NULL,
    password VARCHAR(255) NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    apellido VARCHAR(100) NOT NULL,
    dni INTEGER UNIQUE NOT NULL, -- INTEGER según tu entidad Java
    telefono VARCHAR(20) NOT NULL,
    fecha_nacimiento DATE,
    id_localidad BIGINT NOT NULL REFERENCES localidades(id_localidad),
    id_estado_actual BIGINT NOT NULL REFERENCES estados(id_estado)
);

-- ==========================================
-- NIVEL 4: Tablas Hijas (Específicas de la Herencia)
-- ==========================================

CREATE TABLE IF NOT EXISTS administradores (
    id_usuario BIGINT PRIMARY KEY REFERENCES usuarios(id_usuario)
);

CREATE TABLE IF NOT EXISTS pacientes (
    id_paciente BIGINT PRIMARY KEY REFERENCES usuarios(id_usuario),
    numero_afiliado VARCHAR(50),
    id_obra_social BIGINT NOT NULL REFERENCES obras_sociales(id_obra_social)
);

CREATE TABLE IF NOT EXISTS profesionales (
    id_profesional BIGINT PRIMARY KEY REFERENCES usuarios(id_usuario),
    nro_matricula VARCHAR(50) UNIQUE NOT NULL,
    id_especialidad BIGINT NOT NULL REFERENCES especialidades(id_especialidad)
);

-- ==========================================
-- NIVEL 5: Tablas de Asociación y Soporte
-- ==========================================

-- Tabla intermedia para ManyToMany
CREATE TABLE IF NOT EXISTS usuario_roles (
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    id_rol BIGINT NOT NULL REFERENCES roles(id_rol),
    PRIMARY KEY (id_usuario, id_rol)
);

CREATE TABLE IF NOT EXISTS cambios_estado (
    id_cambio_estado BIGSERIAL PRIMARY KEY,
    fecha TIMESTAMP NOT NULL,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    id_estado BIGINT NOT NULL REFERENCES estados(id_estado)
);

CREATE TABLE IF NOT EXISTS tokens_confirmacion (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    fecha_expiracion TIMESTAMP NOT NULL,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario)
);

-- ==========================================
-- CARGA DE DATOS INICIALES
-- ==========================================

INSERT INTO estados (nombre) VALUES ('PENDIENTE'), ('ACTIVO'), ('BLOQUEADO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (descripcion) VALUES ('ROLE_PROFESIONAL'), ('ROLE_PACIENTE'), ('ROLE_ADMINISTRADOR') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO especialidades (descripcion) VALUES ('MÉDICO'), ('COSMIATRA'), ('ODÓNTOLOGO'), ('PSICÓLOGO') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO obras_sociales (descripcion) VALUES ('LA CAJA'), ('OSDE'), ('NO POSEE') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('CORDOBA'), ('SANTA CRUZ') ON CONFLICT (nombre) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) 
VALUES 
    ('CAPILLA DEL MONTE', (SELECT id_provincia FROM provincias WHERE nombre = 'CORDOBA')),
    ('CORDOBA CAPITAL', (SELECT id_provincia FROM provincias WHERE nombre = 'CORDOBA')),
    ('RIO GALLEGOS', (SELECT id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ')),
    ('PUERTO SAN JULIÁN', (SELECT id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ'))
ON CONFLICT (nombre, id_provincia) DO NOTHING;