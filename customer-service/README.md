# Customer Service

Este microservicio gestiona los datos de clientes (customers) dentro de un sistema financiero.

## Tecnologías utilizadas

- Java 21
- Spring Boot 3.5.4
- Spring Data JPA
- H2 Database (en memoria)
- Spring Kafka
- Spring Security
- MapStruct
- Lombok
- JUnit 5 + Jacoco

## Endpoints

### `GET /customers`

Lista paginada de clientes.

### `GET /customers/{id}`

Obtiene la información de un cliente por ID.

### `POST /customers`

Crea un nuevo cliente.

### `PUT /customers/{id}`

Actualiza la información de un cliente.

### `DELETE /customers/{id}`

Elimina un cliente.

### `POST /customers/by-ids`

Recupera una lista de clientes según los IDs proporcionados.

## Estructura de DTOs

### `CustomerRequest`

```json
{
  "name": "Juan Pérez",
  "gender": "M",
  "age": 30,
  "dni": "1234567890",
  "address": "Calle Falsa 123",
  "phone": "0999999999",
  "password": "secreto",
  "is_active": true
}
```

### `CustomerResponse`

```json
{
  "id": 1,
  "name": "Juan Pérez",
  "gender": "M",
  "age": 30,
  "dni": "1234567890",
  "address": "Calle Falsa 123",
  "phone": "0999999999",
  "is_active": true,
  "createdAt": "2025-08-01T12:00:00",
  "updatedAt": "2025-08-02T10:00:00"
}
```

## Base de datos

Los scripts `schema.sql` y `data.sql` están configurados en `application.yml` para inicializar la base de datos H2 en
memoria.

La consola H2 está disponible en: `http://localhost:8080/h2-console`

- JDBC URL: `jdbc:h2:mem:customers`
- Usuario: `sa`
- Contraseña: *(vacía)*

## Cobertura de pruebas

Jacoco está configurado para verificar una cobertura mínima del 85% en líneas de código de clases clave, excluyendo
DTOs, modelos, configuraciones y excepciones.
## Docker Compose
Para correr kafka localmente ejecutar comando:
- docker-compose up -d
---

Desarrollado por **Gandhy**