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
