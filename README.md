# Barbershop Manager API

Backend de Barbershop Manager construido con Java 17 y Spring Boot 4. Expone una API REST para autenticación, clientes, responsables, turnos, agenda, visitas, pagos, estadísticas, sucursales, empleados, horarios laborales y configuraciones de disponibilidad.

## Stack

- Java 17
- Spring Boot 4
- Spring Web MVC
- Spring Security
- OAuth2 Resource Server / JOSE
- JWT firmado
- Spring Data JPA
- Hibernate
- Bean Validation
- Flyway
- H2 Database para desarrollo
- PostgreSQL para demo/deploy
- Maven Wrapper
- Docker

## Requisitos

- Java 17
- Maven Wrapper incluido en el proyecto
- Docker opcional para ejecución containerizada
- PostgreSQL opcional para correr el perfil `demo` localmente

## Perfiles

El backend usa perfiles de Spring:

- `dev`: desarrollo local con H2 en memoria, consola H2 y SQL visible.
- `demo`: despliegue demo con PostgreSQL/Neon.

El perfil activo se define con:

```bash
SPRING_PROFILES_ACTIVE=dev
```

Si no se define, el backend usa `dev` por defecto.

## Variables de entorno

Variables comunes:

```bash
SPRING_PROFILES_ACTIVE=demo
PORT=8080
APP_JWT_SECRET=clave_larga_y_privada_de_minimo_32_caracteres
APP_FRONTEND_URL=http://localhost:4200
```

Variables para `demo` con PostgreSQL:

```bash
SPRING_DATASOURCE_URL=jdbc:postgresql://host/database?sslmode=require
SPRING_DATASOURCE_USERNAME=usuario
SPRING_DATASOURCE_PASSWORD=password
```

`APP_FRONTEND_URL` puede recibir más de un origen separado por comas:

```bash
APP_FRONTEND_URL=http://localhost:4200,https://barbershop-project-frontend.vercel.app
```

En la demo publicada, el frontend permitido es:

```bash
APP_FRONTEND_URL=https://barbershop-project-frontend.vercel.app
```

## Ejecutar en local

Windows:

```bash
mvnw.cmd spring-boot:run
```

Linux/macOS:

```bash
./mvnw spring-boot:run
```

API local:

```text
http://localhost:8080
```

H2 Console en perfil `dev`:

```text
http://localhost:8080/h2-console
```

Datos H2 por defecto:

```text
JDBC URL: jdbc:h2:mem:shiftsdb
User: sa
Password:
```

## Compilar

Windows:

```bash
mvnw.cmd -DskipTests compile
```

Linux/macOS:

```bash
./mvnw -DskipTests compile
```

## Docker

Construir imagen:

```bash
docker build -t barbershop-api .
```

Ejecutar con perfil `dev`:

```bash
docker run -p 8080:8080 --env SPRING_PROFILES_ACTIVE=dev barbershop-api
```

Ejecutar con perfil `demo` requiere pasar datasource, JWT y CORS:

```bash
docker run -p 8080:8080 \
  --env SPRING_PROFILES_ACTIVE=demo \
  --env SPRING_DATASOURCE_URL=jdbc:postgresql://host/database?sslmode=require \
  --env SPRING_DATASOURCE_USERNAME=usuario \
  --env SPRING_DATASOURCE_PASSWORD=password \
  --env APP_JWT_SECRET=clave_larga_y_privada \
  --env APP_FRONTEND_URL=https://barbershop-project-frontend.vercel.app \
  barbershop-api
```

## Datos demo

El proyecto incluye `src/main/resources/data.sql` con datos precargados e idempotentes:

- Barbería demo.
- Sucursales.
- Usuario administrador.
- Clientes y contactos responsables.
- Turnos con barbero asignado.
- Visitas atendidas.
- Movimientos de pago.
- Configuración inicial.

En `dev` y `demo`, `spring.sql.init.mode=always` permite cargar esos datos al iniciar.

## Migraciones

Las migraciones viven en `src/main/resources/db/migration`:

```text
V1__initial_schema.sql
V2__responsible_contacts.sql
V3__client_self_responsible.sql
V4__default_currency_settings.sql
V5__visit_payment_movements.sql
V6__assigned_employee_shifts.sql
V7__employee_schedules.sql
```

Flyway se ejecuta antes del seed y Hibernate está configurado con `ddl-auto=validate`, por lo que el esquema debe coincidir con las entidades.

## Módulos principales

```text
src/main/java/com/barbershop/shifts/
|-- configurations/      Seguridad, CORS y configuración general
|-- controllers/         Endpoints REST
|-- dtos/                Requests y responses
|-- entities/            Modelo de dominio JPA
|-- repositories/        Repositorios Spring Data
`-- services/            Reglas de negocio
```

## Modelo de dominio

Entidades principales:

- `Barbershop`: barbería a la que pertenece el usuario.
- `Branch`: sucursal de una barbería.
- `User`: administrador o empleado, con sucursales asignadas.
- `EmployeeSchedule`: horarios laborales de un empleado por sucursal y día.
- `Client`: cliente compartido dentro de una barbería.
- `ResponsibleContact`: contacto responsable opcional para clientes que no gestionan su propio turno.
- `Shift`: turno con cliente, sucursal, estado, monto estimado y barbero asignado.
- `ScheduleOverride`: disponibilidad configurable por fecha o rango.
- `AppSettings`: monto estimado, moneda y slots por defecto.
- `Visit`: atención realizada sobre un turno.
- `VisitPaymentMovement`: movimiento financiero de una visita.

## Reglas de negocio destacadas

- Los datos operativos se filtran por barbería y, cuando corresponde, por `X-Branch-Id`.
- Los clientes se comparten entre sucursales de la misma barbería.
- El teléfono del cliente es obligatorio y no puede repetirse dentro de la misma barbería.
- El email del cliente es opcional y puede repetirse.
- Un cliente puede ser su propio responsable o tener un contacto responsable externo.
- La disponibilidad de turnos se calcula con la configuración de horarios, los turnos ya cargados y los empleados que trabajan en la sucursal.
- Un horario puede aceptar más de un turno si hay más de un barbero disponible.
- Al crear un turno se puede elegir barbero o dejar asignación automática.
- Las visitas registran el empleado que atendió.
- Los pagos se modelan como movimientos: `PAYMENT`, `REFUND` y `BONIFICATION`.
- El estado final de pago se deriva del balance entre total, pagos, reembolsos y bonificaciones.

## Endpoints principales

Autenticación:

```text
POST /auth/login
POST /auth/register
POST /auth/change-password
```

Contexto de trabajo:

```text
GET  /api/work-context
GET  /api/work-context/branches
POST /api/work-context/branches
GET  /api/work-context/employees
POST /api/work-context/employees
PUT  /api/work-context/employees/{id}/branches
GET  /api/work-context/employees/{id}/schedule
PUT  /api/work-context/employees/{id}/schedule
```

Clientes:

```text
GET    /api/clients
GET    /api/clients/{id}
POST   /api/clients
PUT    /api/clients
DELETE /api/clients/{id}
```

Turnos:

```text
GET  /api/shifts
GET  /api/shifts/complete
GET  /api/shifts/availability
GET  /api/shifts/agenda
GET  /api/shifts/{id}
POST /api/shifts
PUT  /api/shifts/{id}
```

Visitas:

```text
GET  /api/visits
GET  /api/visits/{id}
POST /api/visits
PUT  /api/visits/{id}
```

Configuración:

```text
GET /api/settings
PUT /api/settings
GET /api/settings/schedule
GET /api/settings/schedule/range
GET /api/settings/schedule/default
PUT /api/settings/schedule
```

Dashboards:

```text
GET /api/dashboard
GET /api/dashboard/clients/{clientId}
```

## Seguridad

- Login y registro mediante endpoints públicos de autenticación.
- Tokens JWT firmados con `APP_JWT_SECRET`.
- Endpoints privados protegidos por Spring Security.
- CORS configurable por `APP_FRONTEND_URL`.
- Contexto de sucursal recibido desde el header `X-Branch-Id`.
- Los empleados creados por un administrador pueden quedar con contraseña temporal y deben cambiarla al ingresar.

## Deploy demo

La API demo está desplegada en Render y conectada a una base PostgreSQL administrada en Neon.

```text
https://barbershop-project-backend.onrender.com
```

El frontend demo está desplegado en Vercel y consume esta API desde:

```text
https://barbershop-project-backend.onrender.com/api
```

Configuración del servicio en Render:

- Runtime: Docker.
- Branch: `main`.
- Perfil activo: `demo`.
- Base de datos: Neon PostgreSQL.
- Puerto: Render inyecta `PORT` y la aplicación lo toma desde `server.port=${PORT:8080}`.
- CORS: limitado al origen configurado en `APP_FRONTEND_URL`.

Variables configuradas en Render:

```bash
SPRING_PROFILES_ACTIVE=demo
SPRING_DATASOURCE_URL=jdbc:postgresql://.../barbershop_db?sslmode=require
SPRING_DATASOURCE_USERNAME=...
SPRING_DATASOURCE_PASSWORD=...
APP_JWT_SECRET=...
APP_FRONTEND_URL=https://barbershop-project-frontend.vercel.app
```

Las credenciales reales no se versionan en el repositorio. El perfil `demo` ejecuta Flyway y luego carga `data.sql`, que es idempotente para mantener una demo utilizable sin duplicar datos en cada arranque.

## Notas técnicas

- En desarrollo local, la API corre por defecto en `http://localhost:8080`.
- En Render, el puerto lo define la plataforma mediante la variable `PORT`.
- El perfil `demo` usa PostgreSQL/Neon, Flyway y seed idempotente.
- CORS está limitado al frontend demo publicado en Vercel.
- Hibernate corre con `ddl-auto=validate`, por lo que las migraciones son la fuente de verdad del esquema.

Estas decisiones permiten que el mismo backend funcione en local con H2 y en demo con PostgreSQL sin cambiar código.
