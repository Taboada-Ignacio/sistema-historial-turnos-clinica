-- Ejecutar una vez en bases ya creadas antes de agregar el campo en Java (si ddl-auto no aplica).
ALTER TABLE usuarios ADD COLUMN IF NOT EXISTS direccion VARCHAR(255);
