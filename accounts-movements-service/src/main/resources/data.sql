-- Este script inserta datos en las tablas 'accounts' y 'movements',
-- utilizando comillas dobles para que los nombres de tablas y columnas
-- se respeten en minúsculas.

-- Insertar datos en la tabla de cuentas
INSERT INTO "accounts" (
    "id", "actual_balance", "customer_id", "initial_balance",
    "account_number", "status", "account_type",
    "created_at", "updated_at"
)
VALUES (
    1, 100.00, 1, 100.00,
    '1234567890', true, 'SAVINGS',
    NOW(), NULL
);

-- Insertar movimiento inicial tipo DEPÓSITO
INSERT INTO "movements" (
    "id", "amount", "balance", "date",
    "movement_type", "account_id",
    "created_at", "updated_at"
)
VALUES (
    1, 100.00, 100.00, '2025-08-02 19:30:00',
    'DEPOSIT', 1,
    NOW(), NULL
);

-- Sincronizar el contador de autoincremento para la tabla de movimientos
-- Esto asegura que el próximo ID generado sea mayor que el último ID insertado.
ALTER TABLE "movements" ALTER COLUMN "id" RESTART WITH (SELECT MAX("id") + 1 FROM "movements");

-- Sincronizar el contador de autoincremento para la tabla de cuentas (por si acaso)
ALTER TABLE "accounts" ALTER COLUMN "id" RESTART WITH (SELECT MAX("id") + 1 FROM "accounts");