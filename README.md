# Descripción de los Microservicios

Este repositorio contiene dos microservicios principales: `Account Service` y `Customer Service`. A continuación, se proporciona una descripción breve
de cada uno de ellos.

## 1. Customer Service

### Descripción

El `Customer Service` es un microservicio diseñado para gestionar la información de los clientes del banco. Este servicio maneja las operaciones CRUD
para los datos de los clientes, permitiendo almacenar y mantener actualizada la información personal y de contacto de los mismos.

### Funcionalidades Clave

- **Gestión de Clientes:** Proporciona operaciones para crear, consultar, actualizar y eliminar registros de clientes.
- **Validación de Datos:** Incluye validaciones para asegurar la integridad y consistencia de la información de los clientes.

### Endpoints Principales

- `/customers`: Gestión de clientes.

## 2. Account Service

### Descripción

El `Account Service` es un microservicio encargado de gestionar las cuentas bancarias y las transacciones financieras asociadas a dichas cuentas.
Proporciona operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para cuentas y transacciones, así como la generación de reportes de estado de cuenta.

### Funcionalidades Clave

- **Gestión de Cuentas:** Permite crear, consultar, actualizar y eliminar cuentas bancarias.
- **Gestión de Transacciones:** Facilita la creación, consulta, actualización y eliminación de transacciones financieras.
- **Reportes de Estado de Cuenta:** Genera reportes detallados de las transacciones y balances de una cuenta en un rango de fechas.

### Endpoints Principales

- `/accounts`: Gestión de cuentas.
- `/transactions`: Gestión de transacciones.
- `/accounts/reports`: Generación de reportes de estado de cuenta.

## Integración y Uso

Ambos microservicios están diseñados para funcionar de manera independiente, pero pueden integrarse dentro de una arquitectura más amplia para ofrecer
un sistema bancario completo. El `Account Service` y el `Customer Service` interactúan mediante identificadores de clientes, lo que permite vincular
cuentas y transacciones a los datos de los clientes.

## Requisitos Previos

- **Java:** Ambos microservicios están desarrollados en Java, por lo que se requiere un entorno de ejecución de Java.
- **Spring Boot:** Utilizan Spring Boot como framework principal.
- **Base de Datos:** Se requiere una base de datos compatible con JPA/Hibernate para persistir los datos de cuentas, transacciones y clientes.

## Ejecución

Para ejecutar cada microservicio, siga los siguientes pasos:
Tener docker - docker compose instalador y ejecutar el comando

docker compose up --build

1. **Clonar el repositorio:**
   ```bash
   https://github.com/LeandriT/spring-softka.git
