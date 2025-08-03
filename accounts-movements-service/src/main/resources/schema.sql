-- Este script crea las tablas 'accounts' y 'movements',
-- utilizando comillas dobles para que los nombres de tablas y columnas
-- se respeten en minúsculas en H2.

DROP TABLE IF EXISTS "movements";
DROP TABLE IF EXISTS "accounts";

CREATE TABLE "accounts" (
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "actual_balance" NUMERIC(38, 2),
    "customer_id" INT,
    "initial_balance" NUMERIC(38, 2),
    "account_number" VARCHAR(255),
    "status" BOOLEAN,
    "account_type" VARCHAR(255),
    "created_at" TIMESTAMP DEFAULT NOW(),
    "updated_at" TIMESTAMP
);

CREATE TABLE "movements" (
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "amount" NUMERIC(38, 2) NOT NULL,
    "balance" NUMERIC(38, 2) NOT NULL,
    "date" TIMESTAMP NOT NULL,
    "movement_type" VARCHAR(255) NOT NULL,
    "account_id" INT NOT NULL,
    "created_at" TIMESTAMP DEFAULT NOW(),
    "updated_at" TIMESTAMP,
    CONSTRAINT fk_account FOREIGN KEY ("account_id") REFERENCES "accounts"("id")
);
