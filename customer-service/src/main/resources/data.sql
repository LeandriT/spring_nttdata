-- Este script inserta datos en la tabla 'customers',
-- utilizando comillas dobles para que los nombres de tablas y columnas
-- se respeten en minúsculas.

-- Insertar datos en la tabla de clientes
INSERT INTO "customers" (
    "customer_id", "name", "gender", "age", "dni", "address", "phone", "password", "is_active", "created_at"
) VALUES
('CUST001', 'Jose Lema', 'M', 30, '100000001', 'Otavalo sn y principal', '098254785', '1234', true, NOW()),
('CUST002', 'Marianela Montalvo', 'F', 28, '100000002', 'Amazonas y NNUU', '097548965', '5678', true, NOW()),
('CUST003', 'Juan Osorio', 'M', 32, '100000003', '13 junio y Equinoccial', '098874587', '1245', true, NOW());
-- Sincronizar el contador de autoincremento para la tabla de clientes
-- Esto asegura que el próximo ID generado sea mayor que el último ID insertado.
ALTER TABLE "customers" ALTER COLUMN "id" RESTART WITH (SELECT MAX("id") + 1 FROM "customers");
