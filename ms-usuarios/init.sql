-- ==========================================
-- init.sql — Esquema + semilla completa (ms-usuarios)
-- Ejecutar en volumen nuevo de PostgreSQL (docker-entrypoint-initdb.d).
-- Incluye: tablas, catálogos, sentinel SIN ESPECIFICAR, geo Argentina, direcciones por localidad.
-- ==========================================

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

CREATE TABLE IF NOT EXISTS refresh_tokens (
    id BIGSERIAL PRIMARY KEY,
    token VARCHAR(255) UNIQUE NOT NULL,
    id_usuario BIGINT NOT NULL REFERENCES usuarios(id_usuario),
    expiry_date TIMESTAMP NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    created_at TIMESTAMP NOT NULL
);

-- Historial de membresías profesionales
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

INSERT INTO especialidades (descripcion) VALUES ('MEDICINA GENERAL'), ('COSMIATRA'), ('ODONTOLOGO'), ('PSICOLOGO') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO obras_sociales (descripcion) VALUES ('LA CAJA'), ('OSDE'), ('NO POSEE') ON CONFLICT (descripcion) DO NOTHING;

-- CARGA INICIAL DE MEMBRESÍAS
INSERT INTO membresias (nombre) VALUES ('SIN_VERIFICAR'), ('INACTIVA'), ('ACTIVA'), ('ACCESO_INDEFINIDO') ON CONFLICT (nombre) DO NOTHING;

-- Catálogo reservado SIN ESPECIFICAR (integridad al borrar / reasignar FK).
-- Idempotente (ON CONFLICT). Orden: provincia sentinel → localidad sentinel en sección GEO.
INSERT INTO especialidades (descripcion) VALUES ('SIN ESPECIFICAR') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO obras_sociales (descripcion) VALUES ('SIN ESPECIFICAR') ON CONFLICT (descripcion) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SIN ESPECIFICAR') ON CONFLICT (nombre) DO NOTHING;

-- ==========================================
-- GEO: Provincias y localidades de Argentina
-- ==========================================

-- Provincias de la República Argentina (+ sentinel)
INSERT INTO provincias (nombre) VALUES ('BUENOS AIRES') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('CIUDAD AUTONOMA DE BUENOS AIRES') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('CATAMARCA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('CHACO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('CHUBUT') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('CORDOBA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('CORRIENTES') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('ENTRE RIOS') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('FORMOSA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('JUJUY') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('LA PAMPA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('LA RIOJA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('MENDOZA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('MISIONES') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('NEUQUEN') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('RIO NEGRO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SALTA') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SAN JUAN') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SAN LUIS') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SANTA CRUZ') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SANTA FE') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SANTIAGO DEL ESTERO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('TIERRA DEL FUEGO') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('TUCUMAN') ON CONFLICT (nombre) DO NOTHING;
INSERT INTO provincias (nombre) VALUES ('SIN ESPECIFICAR') ON CONFLICT (nombre) DO NOTHING;

-- Localidades principales por provincia
INSERT INTO localidades (nombre, id_provincia) SELECT 'LA PLATA', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'MAR DEL PLATA', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'BAHIA BLANCA', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TANDIL', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'AZUL', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'NECOCHEA', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'OLAVARRIA', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'QUILMES', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'LANUS', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'MORON', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN ISIDRO', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TIGRE', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PILAR', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'LOMAS DE ZAMORA', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN NICOLAS', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PERGAMINO', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'JUNIN', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CHIVILCOY', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'MERCEDES', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TRES ARROYOS', id_provincia FROM provincias WHERE nombre = 'BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'CIUDAD AUTONOMA DE BUENOS AIRES', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PALERMO', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'RECOLETA', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'BELGRANO', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CABALLITO', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'FLORES', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'BOEDO', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN TELMO', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'LA BOCA', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'NUÑEZ', id_provincia FROM provincias WHERE nombre = 'CIUDAD AUTONOMA DE BUENOS AIRES' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN FERNANDO DEL VALLE DE CATAMARCA', id_provincia FROM provincias WHERE nombre = 'CATAMARCA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'ANDALGALA', id_provincia FROM provincias WHERE nombre = 'CATAMARCA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'BELEN', id_provincia FROM provincias WHERE nombre = 'CATAMARCA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TINOGASTA', id_provincia FROM provincias WHERE nombre = 'CATAMARCA' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'RESISTENCIA', id_provincia FROM provincias WHERE nombre = 'CHACO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PRESIDENCIA ROQUE SAENZ PEÑA', id_provincia FROM provincias WHERE nombre = 'CHACO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'BARRANQUERAS', id_provincia FROM provincias WHERE nombre = 'CHACO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CHARATA', id_provincia FROM provincias WHERE nombre = 'CHACO' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'RAWSON', id_provincia FROM provincias WHERE nombre = 'CHUBUT' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'COMODORO RIVADAVIA', id_provincia FROM provincias WHERE nombre = 'CHUBUT' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TRELEW', id_provincia FROM provincias WHERE nombre = 'CHUBUT' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PUERTO MADRYN', id_provincia FROM provincias WHERE nombre = 'CHUBUT' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'ESQUEL', id_provincia FROM provincias WHERE nombre = 'CHUBUT' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'CORDOBA CAPITAL', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'VILLA MARIA', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'RIO CUARTO', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN FRANCISCO', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CARLOS PAZ', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CAPILLA DEL MONTE', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'VILLA DOLORES', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'ALTA GRACIA', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'RAFAELA', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'JESUS MARIA', id_provincia FROM provincias WHERE nombre = 'CORDOBA' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'CORRIENTES', id_provincia FROM provincias WHERE nombre = 'CORRIENTES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'GOYA', id_provincia FROM provincias WHERE nombre = 'CORRIENTES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PASO DE LOS LIBRES', id_provincia FROM provincias WHERE nombre = 'CORRIENTES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CURUZU CUATIA', id_provincia FROM provincias WHERE nombre = 'CORRIENTES' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'PARANA', id_provincia FROM provincias WHERE nombre = 'ENTRE RIOS' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CONCORDIA', id_provincia FROM provincias WHERE nombre = 'ENTRE RIOS' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'GUALEGUAYCHU', id_provincia FROM provincias WHERE nombre = 'ENTRE RIOS' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CONCEPCION DEL URUGUAY', id_provincia FROM provincias WHERE nombre = 'ENTRE RIOS' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'VICTORIA', id_provincia FROM provincias WHERE nombre = 'ENTRE RIOS' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'FORMOSA', id_provincia FROM provincias WHERE nombre = 'FORMOSA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CLORINDA', id_provincia FROM provincias WHERE nombre = 'FORMOSA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PIRANE', id_provincia FROM provincias WHERE nombre = 'FORMOSA' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN SALVADOR DE JUJUY', id_provincia FROM provincias WHERE nombre = 'JUJUY' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN PEDRO DE JUJUY', id_provincia FROM provincias WHERE nombre = 'JUJUY' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'LIBERTADOR GENERAL SAN MARTIN', id_provincia FROM provincias WHERE nombre = 'JUJUY' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PALPALA', id_provincia FROM provincias WHERE nombre = 'JUJUY' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SANTA ROSA', id_provincia FROM provincias WHERE nombre = 'LA PAMPA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'GENERAL PICO', id_provincia FROM provincias WHERE nombre = 'LA PAMPA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TOAY', id_provincia FROM provincias WHERE nombre = 'LA PAMPA' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'LA RIOJA', id_provincia FROM provincias WHERE nombre = 'LA RIOJA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CHILECITO', id_provincia FROM provincias WHERE nombre = 'LA RIOJA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'AIMOGASTA', id_provincia FROM provincias WHERE nombre = 'LA RIOJA' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'MENDOZA', id_provincia FROM provincias WHERE nombre = 'MENDOZA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN RAFAEL', id_provincia FROM provincias WHERE nombre = 'MENDOZA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'GODOY CRUZ', id_provincia FROM provincias WHERE nombre = 'MENDOZA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'LUJAN DE CUYO', id_provincia FROM provincias WHERE nombre = 'MENDOZA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'MAIPU', id_provincia FROM provincias WHERE nombre = 'MENDOZA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'MALARGUE', id_provincia FROM provincias WHERE nombre = 'MENDOZA' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'POSADAS', id_provincia FROM provincias WHERE nombre = 'MISIONES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'OBERA', id_provincia FROM provincias WHERE nombre = 'MISIONES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'ELDORADO', id_provincia FROM provincias WHERE nombre = 'MISIONES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PUERTO IGUAZU', id_provincia FROM provincias WHERE nombre = 'MISIONES' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'APOSTOLES', id_provincia FROM provincias WHERE nombre = 'MISIONES' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'NEUQUEN', id_provincia FROM provincias WHERE nombre = 'NEUQUEN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CUTRAL CO', id_provincia FROM provincias WHERE nombre = 'NEUQUEN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN MARTIN DE LOS ANDES', id_provincia FROM provincias WHERE nombre = 'NEUQUEN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'VILLA LA ANGOSTURA', id_provincia FROM provincias WHERE nombre = 'NEUQUEN' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'VIEDMA', id_provincia FROM provincias WHERE nombre = 'RIO NEGRO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN CARLOS DE BARILOCHE', id_provincia FROM provincias WHERE nombre = 'RIO NEGRO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'GENERAL ROCA', id_provincia FROM provincias WHERE nombre = 'RIO NEGRO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CIPOLLETTI', id_provincia FROM provincias WHERE nombre = 'RIO NEGRO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'VILLA REGINA', id_provincia FROM provincias WHERE nombre = 'RIO NEGRO' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SALTA', id_provincia FROM provincias WHERE nombre = 'SALTA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'ORAN', id_provincia FROM provincias WHERE nombre = 'SALTA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TARTAGAL', id_provincia FROM provincias WHERE nombre = 'SALTA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CAFAYATE', id_provincia FROM provincias WHERE nombre = 'SALTA' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'METAN', id_provincia FROM provincias WHERE nombre = 'SALTA' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN JUAN', id_provincia FROM provincias WHERE nombre = 'SAN JUAN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'RAWSON', id_provincia FROM provincias WHERE nombre = 'SAN JUAN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CHIMBAS', id_provincia FROM provincias WHERE nombre = 'SAN JUAN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'RIVADAVIA', id_provincia FROM provincias WHERE nombre = 'SAN JUAN' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN LUIS', id_provincia FROM provincias WHERE nombre = 'SAN LUIS' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'VILLA MERCEDES', id_provincia FROM provincias WHERE nombre = 'SAN LUIS' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'MERLO', id_provincia FROM provincias WHERE nombre = 'SAN LUIS' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'LA PUNTA', id_provincia FROM provincias WHERE nombre = 'SAN LUIS' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'RIO GALLEGOS', id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CALETA OLIVIA', id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'EL CALAFATE', id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PUERTO SAN JULIAN', id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PUERTO DESEADO', id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'PERITO MORENO', id_provincia FROM provincias WHERE nombre = 'SANTA CRUZ' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SANTA FE', id_provincia FROM provincias WHERE nombre = 'SANTA FE' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'ROSARIO', id_provincia FROM provincias WHERE nombre = 'SANTA FE' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'RAFAELA', id_provincia FROM provincias WHERE nombre = 'SANTA FE' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'VENADO TUERTO', id_provincia FROM provincias WHERE nombre = 'SANTA FE' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'RECONQUISTA', id_provincia FROM provincias WHERE nombre = 'SANTA FE' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN LORENZO', id_provincia FROM provincias WHERE nombre = 'SANTA FE' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SANTIAGO DEL ESTERO', id_provincia FROM provincias WHERE nombre = 'SANTIAGO DEL ESTERO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'LA BANDA', id_provincia FROM provincias WHERE nombre = 'SANTIAGO DEL ESTERO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TERMAS DE RIO HONDO', id_provincia FROM provincias WHERE nombre = 'SANTIAGO DEL ESTERO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'FRIAS', id_provincia FROM provincias WHERE nombre = 'SANTIAGO DEL ESTERO' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'USHUAIA', id_provincia FROM provincias WHERE nombre = 'TIERRA DEL FUEGO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'RIO GRANDE', id_provincia FROM provincias WHERE nombre = 'TIERRA DEL FUEGO' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TOLHUIN', id_provincia FROM provincias WHERE nombre = 'TIERRA DEL FUEGO' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SAN MIGUEL DE TUCUMAN', id_provincia FROM provincias WHERE nombre = 'TUCUMAN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'YERBA BUENA', id_provincia FROM provincias WHERE nombre = 'TUCUMAN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'TAFI DEL VALLE', id_provincia FROM provincias WHERE nombre = 'TUCUMAN' ON CONFLICT (nombre, id_provincia) DO NOTHING;
INSERT INTO localidades (nombre, id_provincia) SELECT 'CONCEPCION', id_provincia FROM provincias WHERE nombre = 'TUCUMAN' ON CONFLICT (nombre, id_provincia) DO NOTHING;

INSERT INTO localidades (nombre, id_provincia) SELECT 'SIN ESPECIFICAR', id_provincia FROM provincias WHERE nombre = 'SIN ESPECIFICAR' ON CONFLICT (nombre, id_provincia) DO NOTHING;

-- ==========================================
-- Direccion SIN ESPECIFICAR por localidad
-- ==========================================

-- Dirección SIN ESPECIFICAR por cada localidad (reasignación al borrar otras direcciones)
INSERT INTO direcciones (nombre, id_localidad)
SELECT 'SIN ESPECIFICAR', l.id_localidad
FROM localidades l
WHERE NOT EXISTS (
    SELECT 1 FROM direcciones d
    WHERE d.id_localidad = l.id_localidad AND d.nombre = 'SIN ESPECIFICAR'
);

-- Fin init.sql unificado (esquema + semilla + geo Argentina + direcciones sentinel)
