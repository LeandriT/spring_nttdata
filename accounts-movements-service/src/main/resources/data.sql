-- Este script inserta datos en las tablas 'accounts' y 'movements',
-- utilizando comillas dobles para que los nombres de tablas y columnas
-- se respeten en minúsculas.

-- Insertar datos en la tabla de cuentas
INSERT INTO "accounts" (
    "id", "actual_balance", "customer_id", "initial_balance", "account_number", "status", "account_type", "created_at"
) VALUES
(1, 2000.00, 1, 1425.00, '478758', true, 'SAVINGS', NOW()), --Jose Lema
(2, 100.00, 2, 700.00, '225487', true, 'CURRENT', NOW()), --Marianela Montalvo
(3, 0.00, 3, 150.00, '495878', true, 'SAVINGS', NOW()), --Juan Osorio
(4, 540.00, 2, 0, '496825', true, 'SAVINGS', NOW()),--Marianela Montalvo
(5, 1000.00, 1, 1000.00, '585545', true, 'CURRENT', NOW());--Jose Lema
-- Insertar movimiento inicial tipo DEPÓSITO
INSERT INTO "movements" (
    "id", "amount", "balance", "date", "movement_type", "account_id", "created_at"
) VALUES
(1, 100.00, 100.00, '2025-08-03 10:05:00', 'DEPOSIT', 1, NOW()),      -- Depósito en cuenta 478758 --Jose Lema
(2, 100.00, 700.00, '2022-02-08 10:05:00', 'DEPOSIT', 2, NOW()),      -- Depósito en cuenta 225487 --Marianela Montalvo
(3, 0.00, 0.00, '2025-08-03 10:05:00', 'DEPOSIT', 3, NOW()),          -- Depósito en cuenta 495878 --Juan Osorio
(4, 540.00, 540.00, '2025-02-10 10:05:00', 'DEPOSIT', 4, NOW()),      -- Depósito en cuenta 496825 --Marianela Montalvo
(5, 1000.00, 1000.00, '2025-08-03 10:05:00', 'DEPOSIT', 5, NOW()),    -- Depósito en cuenta 585545 --Jose Lema

(6, 575.00, 1425.00, '2025-08-03 10:00:00', 'WITHDRAWAL', 1, NOW()), -- Retiro de cuenta 478758
(7, 600.00, 700.00, '2022-02-08 10:05:00', 'DEPOSIT', 2, NOW()),      -- Depósito en cuenta 225487 --Marianela Montalvo
(8, 150.00, 150.00, '2025-08-03 10:10:00', 'DEPOSIT', 3, NOW()),      -- Depósito en cuenta 495878
(9, 540.00, 0.00, '2025-02-10 10:15:00', 'WITHDRAWAL', 4, NOW());    -- Retiro total de cuenta 496825 --Marianela Montalvo

-- Sincronizar el contador de autoincremento para la tabla de movimientos
-- Esto asegura que el próximo ID generado sea mayor que el último ID insertado.
ALTER TABLE "movements" ALTER COLUMN "id" RESTART WITH (SELECT MAX("id") + 1 FROM "movements");

-- Sincronizar el contador de autoincremento para la tabla de cuentas (por si acaso)
ALTER TABLE "accounts" ALTER COLUMN "id" RESTART WITH (SELECT MAX("id") + 1 FROM "accounts");