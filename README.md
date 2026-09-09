<div align="center">

# StaffSync — Vacation Service

**Solicitudes de vacaciones con notificaciones vía Kafka y RabbitMQ**

![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot%203.3-6DB33F?style=for-the-badge&logo=spring-boot&logoColor=white)
![Kafka](https://img.shields.io/badge/Apache%20Kafka-231F20?style=for-the-badge&logo=apache-kafka&logoColor=white)
![RabbitMQ](https://img.shields.io/badge/RabbitMQ-FF6600?style=for-the-badge&logo=rabbitmq&logoColor=white)

</div>

---

Microservicio que gestiona las solicitudes de vacaciones de los empleados. Cuando un manager aprueba o rechaza una solicitud, el servicio publica el evento en dos canales: el topic Kafka `vacation-events` (para trazabilidad) y el exchange RabbitMQ `staffsync.notifications` (para que el notification-service entregue la notificación al empleado).

---

## Estados de una solicitud

| Estado | Descripción |
|---|---|
| `PENDING` | Pendiente de revisión |
| `APPROVED` | Aprobada por el manager |
| `REJECTED` | Rechazada |

---

## API endpoints

| Método | Ruta | Auth | Descripción |
|---|---|---|---|
| GET | `/vacations` | Sí | Listar todas las solicitudes |
| POST | `/vacations` | EMPLOYEE | Enviar solicitud |
| GET | `/vacations/{id}` | Sí | Obtener por ID |
| GET | `/vacations/employee/{employeeId}` | Sí | Solicitudes de un empleado |
| GET | `/vacations/pending` | MANAGER/ADMIN | Solicitudes pendientes |
| PUT | `/vacations/{id}/approve` | MANAGER/ADMIN | Aprobar |
| PUT | `/vacations/{id}/reject` | MANAGER/ADMIN | Rechazar |

---

## Mensajería

**Kafka** — topic `vacation-events`:
```json
{
  "eventType": "VACATION_APPROVED | VACATION_REJECTED",
  "vacationId": "uuid",
  "employeeId": "uuid",
  "startDate": "2024-01-15",
  "endDate": "2024-01-20",
  "timestamp": "2024-01-01T10:00:00Z"
}
```

**RabbitMQ** — exchange `staffsync.notifications`:
- Routing key `vacation.approved` o `vacation.rejected`
- Consumido por notification-service

---

## Tecnologías

| Capa | Tecnología |
|---|---|
| Runtime | Java 21 |
| Framework | Spring Boot 3.3.4 |
| Mensajería | Apache Kafka + RabbitMQ (Spring AMQP) |
| ORM | Spring Data JPA |
| Base de datos | PostgreSQL / H2 |
| Mapeo | MapStruct 1.5.5 |
| API spec | OpenAPI Generator 7.7.0 |

---

## Variables de entorno

| Variable | Valor por defecto | Descripción |
|---|---|---|
| `DATABASE_URL` | `jdbc:postgresql://localhost:5432/staffsync_vacation` | Conexión PostgreSQL |
| `DB_USER` / `DB_PASS` | `staffsync` | Credenciales |
| `KAFKA_BOOTSTRAP` | `localhost:9092` | Bootstrap servers |
| `RABBITMQ_HOST` | `localhost` | Host de RabbitMQ |
| `RABBITMQ_USER` / `RABBITMQ_PASS` | `guest` | Credenciales RabbitMQ |
| `EUREKA_URL` | `http://admin:admin@localhost:8761/eureka/` | URL de Eureka |

---

## Tests

```bash
mvn test
```

3 tests unitarios (crear solicitud, aprobar con evento, rechazar con evento).

---

## Ejecución local

```bash
mvn spring-boot:run
```

Servicio disponible en: `http://localhost:8084`

---

## Parte de StaffSync

Ver [staffsync](https://github.com/DarioSanchez99/staffsync) para el índice completo del proyecto.
