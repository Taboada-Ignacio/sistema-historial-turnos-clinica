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

-- NUEVA TABLA MAESTRA PARA MEMBRESÍAS
CREATE TABLE IF NOT EXISTS membresias (
    id_membresia BIGSERIAL PRIMARY KEY,
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

-- Catálogo de direcciones por localidad (tipificación)
CREATE TABLE IF NOT EXISTS direcciones (
    id_direccion BIGSERIAL PRIMARY KEY,
    nombre VARCHAR(500) NOT NULL,
    id_localidad BIGINT NOT NULL REFERENCES localidades(id_localidad),
    UNIQUE(nombre, id_localidad)
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
    id_direccion BIGINT NOT NULL REFERENCES direcciones(id_direccion),
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
    foto_perfil VARCHAR(255), -- Ruta de la foto (.webp)
    id_especialidad BIGINT NOT NULL REFERENCES especialidades(id_especialidad),
    id_membresia_actual BIGINT NOT NULL REFERENCES membresias(id_membresia)
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
    codigo VARCHAR(6),
    fecha_expiracion TIMESTAMP NOT NULL,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario)
);

-- NUEVA TABLA PARA EL HISTORIAL DE PAGOS / MEMBRESÍAS
CREATE TABLE IF NOT EXISTS cambios_membresia (
    id_cambio_membresia BIGSERIAL PRIMARY KEY,
    fecha_inicio TIMESTAMP NOT NULL,
    fecha_vencimiento TIMESTAMP,
    id_profesional BIGINT NOT NULL REFERENCES profesionales(id_profesional),
    id_membresia BIGINT NOT NULL REFERENCES membresias(id_membresia)
);

-- ==========================================
-- CARGA DE DATOS INICIALES
-- ==========================================

INSERT INTO estados (nombre) VALUES ('PENDIENTE'), ('ACTIVO'), ('BLOQUEADO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO roles (descripcion) VALUES ('ROLE_PROFESIONAL'), ('ROLE_PACIENTE'), ('ROLE_ADMINISTRADOR') ON CONFLICT (descripcion) DO NOTHING;

INSERT INTO especialidades (descripcion) VALUES ('MEDICO'), ('COSMIATRA'), ('ODONTOLOGO'), ('PSICOLOGO') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO obras_sociales (descripcion) VALUES ('LA CAJA'), ('OSDE'), ('NO POSEE') ON CONFLICT (descripcion) DO NOTHING;

-- CARGA INICIAL DE MEMBRESÍAS
INSERT INTO membresias (nombre) VALUES ('SIN_VERIFICAR'), ('INACTIVA'), ('ACTIVA'), ('ACCESO_INDEFINIDO') ON CONFLICT (nombre) DO NOTHING;

-- -------------------------------------------------------------------------
-- Catálogo reservado SIN ESPECIFICAR (integridad al borrar / reasignar FK).
-- Idempotente (ON CONFLICT). Incluye lo que antes estaba en migration-sentinel-sin-especificar.sql.
-- Orden: provincia sentinel → localidad sentinel depende de esa provincia.
-- -------------------------------------------------------------------------
INSERT INTO especialidades (descripcion) VALUES ('SIN ESPECIFICAR') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO obras_sociales (descripcion) VALUES ('SIN ESPECIFICAR') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SIN ESPECIFICAR') ON CONFLICT (nombre) DO NOTHING;

-- Provincias y localidades: docker-entrypoint-initdb.d/02-argentina-geo-data.sql
-- Direcciones sentinel por localidad: docker-entrypoint-initdb.d/03-direcciones-sentinel.sql
-- Documentación: README.md en este directorio