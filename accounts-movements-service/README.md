# Accounts & Movements Service

Microservicio encargado de la gestión de cuentas bancarias y movimientos (depósitos, retiros), así como la generación de reportes de estado de cuenta.

## 🛠️ Tecnologías

- Java 17
- Spring Boot
- Spring Data JPA
- H2 Database (dev/test)
- Feign Client
- JUnit 5 + Mockito
- Gradle
- JaCoCo (cobertura de pruebas)
- OpenAPI / Swagger

## 📦 Estructura del Proyecto

```
accounts-movements-service/
├── controller/
│   ├── AccountController.java
│   └── MovementController.java
├── service/
│   └── impl/
│       ├── AccountServiceImpl.java
│       └── MovementServiceImpl.java
├── repository/
├── model/
├── dto/
├── mapper/
├── exception/
├── config/
└── ...
```

---

## 🔄 Endpoints REST

### ✅ `AccountController`

Ruta base: `/accounts`

| Método | Ruta                            | Descripción                              |
|--------|----------------------------------|------------------------------------------|
| GET    | `/`                              | Listar cuentas paginadas                 |
| GET    | `/{id}`                          | Obtener una cuenta por ID                |
| POST   | `/`                              | Crear una cuenta                         |
| PUT    | `/{id}`                          | Actualizar una cuenta                    |
| DELETE | `/{id}`                          | Eliminar una cuenta                      |
| GET    | `/statement-report`              | Obtener reporte de estado de cuenta      |

**Parámetros para reporte:**
- `customerId` (opcional)
- `startDate`, `endDate` (obligatorios, formato: `yyyy-MM-dd`)
- `page`, `size` (opcional)

---

### 💰 `MovementController`

Ruta base: `/movements`

| Método | Ruta       | Descripción                        |
|--------|------------|------------------------------------|
| GET    | `/`        | Listar movimientos paginados       |
| GET    | `/{id}`    | Obtener movimiento por ID          |
| POST   | `/`        | Registrar nuevo movimiento         |
| PUT    | `/{id}`    | Actualizar movimiento existente    |
| DELETE | `/{id}`    | Eliminar un movimiento             |

---

## 🔒 Validaciones

- No se permite depositar o retirar montos negativos.
- No se permite retirar fondos si el balance no es suficiente.
- Las fechas del reporte deben estar en orden lógico (start ≤ end y start ≤ hoy).

---

## 🧪 Pruebas

Ejecutar pruebas unitarias y verificar cobertura:

```bash
./gradlew test
./gradlew jacocoTestReport
./gradlew jacocoTestCoverageVerification
```

Informe generado en: `build/reports/jacoco/test/html/index.html`

---

## 📡 Comunicación entre servicios

Este microservicio se comunica con:

- `customer-service` vía **FeignClient**
    - Para validar la existencia del cliente y obtener su información

---

## 🧾 Eventos Asíncronos

- Se publican eventos con `ApplicationEventPublisher` y `EventAccountCustomerPublisher` al crear o actualizar movimientos.
- Los eventos incluyen información del balance actualizado.

---

## 🧰 Base de Datos (H2)

La base H2 se inicializa con:

- Tabla `accounts`
- Tabla `movements`

Puedes acceder a la consola en:

```
http://localhost:8081/h2-console
```

---

## 🧑‍💻 Autor

- Desarrollado por **Gandhy**
