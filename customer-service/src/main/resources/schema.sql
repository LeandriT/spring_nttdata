DROP TABLE IF EXISTS "customers";

CREATE TABLE "customers" (
    "id" BIGINT PRIMARY KEY AUTO_INCREMENT,
    "customer_id" VARCHAR(255),
    "name" VARCHAR(255),
    "gender" VARCHAR(255),
    "age" INT,
    "dni" VARCHAR(255),
    "address" VARCHAR(255),
    "phone" VARCHAR(255),
    "password" VARCHAR(255),
    "is_active" BOOLEAN,
    "created_at" TIMESTAMP NULL,
    "updated_at" TIMESTAMP NULL
);
