-- Este script inserta datos en la tabla 'customers',
-- utilizando comillas dobles para que los nombres de tablas y columnas
-- se respeten en minúsculas.

-- Insertar datos en la tabla de clientes
INSERT INTO "customers" ("id", "customer_id", "name", "gender", "age", "dni", "address", "phone", "password", "is_active", "created_at", "updated_at")
VALUES (1, '0f4c515a-7b23-4197-bbb9-f9823a676729', 'Gandhy Cuasapas', 'Hombre', 30, '0401590039', 'Otavalo sn y principal', '098254785', '827ccb0eea8a706c4c34a16891f84e7b', true, NOW(), NULL );

-- Sincronizar el contador de autoincremento para la tabla de clientes
-- Esto asegura que el próximo ID generado sea mayor que el último ID insertado.
ALTER TABLE "customers" ALTER COLUMN "id" RESTART WITH (SELECT MAX("id") + 1 FROM "customers");
