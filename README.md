# Descripción de los Microservicios

Este repositorio contiene dos microservicios principales: `Account Service` y `Customer Service`. A continuación, se
proporciona una descripción breve
de cada uno de ellos.

## 1. Customer Service

### Descripción

El `Customer Service` es un microservicio diseñado para gestionar la información de los clientes del banco. Este
servicio maneja las operaciones CRUD
para los datos de los clientes, permitiendo almacenar y mantener actualizada la información personal y de contacto de
los mismos.

### Funcionalidades Clave

- **Gestión de Clientes:** Proporciona operaciones para crear, consultar, actualizar y eliminar registros de clientes.
- **Validación de Datos:** Incluye validaciones para asegurar la integridad y consistencia de la información de los
  clientes.

### Endpoints Principales

- `/customers`: Gestión de clientes.

## 2. Account Service

### Descripción

El `Account Service` es un microservicio encargado de gestionar las cuentas bancarias y los movimientos financieras
asociadas a dichas cuentas.
Proporciona operaciones CRUD (Crear, Leer, Actualizar, Eliminar) para cuentas y movimientos, así como la generación de
reportes de estado de cuenta.

### Funcionalidades Clave

- **Gestión de Cuentas:** Permite crear, consultar, actualizar y eliminar cuentas bancarias.
- **Gestión de Movimientos:** Facilita la creación, consulta, actualización y eliminación de movimientos financieras.
- **Reportes de Estado de Cuenta:** Genera reportes detallados de los movimientos y balances de una cuenta en un rango
  de fechas.

### Endpoints Principales

- `/accounts`: Gestión de cuentas.
- `/movements`: Gestión de movimientos.
- `/reports/v1`: Generación de reportes de estado de cuenta.
- `/reports/v2`: Generación de reportes de estado de cuenta como el solicitado.

## Integración y Uso

Ambos microservicios están diseñados para funcionar de manera independiente, pero pueden integrarse dentro de una
arquitectura más amplia para ofrecer
un sistema bancario completo. El `Account Service` y el `Customer Service` interactúan mediante identificadores de
clientes, lo que permite vincular
cuentas y movimientos a los datos de los clientes.

## Requisitos Previos

- **Java:** Ambos microservicios están desarrollados en Java, por lo que se requiere un entorno de ejecución de Java.
- **Spring Boot:** Utilizan Spring Boot como framework principal.
- **Base de Datos:** Se requiere una base de datos compatible con JPA/Hibernate para persistir los datos de cuentas,
  movimientos y clientes.

## Ejecución

Para ejecutar cada microservicio, siga los siguientes pasos:
Tener docker - docker compose instalador y ejecutar el comando

- **chmod a+x build-and-run.sh** (permiso para ejecutar archivo)
- **./build-and-run.sh o sh build-and-run.sh** (build de jar files y run de docker compose)


1. **Clonar el repositorio:**
   ```bash
   https://github.com/LeandriT/spring-nttdata.git
