# Barbershop Manager API

Backend de Barbershop Manager construido con Java 17 y Spring Boot. Expone la API REST para autenticacion, clientes, turnos, visitas, estadisticas, empleados, sucursales y configuraciones de disponibilidad.

## Stack

- Java 17
- Spring Boot
- Spring Security
- JWT firmado
- Spring Data JPA
- Hibernate
- H2 Database para desarrollo
- PostgreSQL para demo
- Maven Wrapper
- Docker

## Requisitos

- Java 17
- Maven Wrapper incluido en el proyecto
- Docker opcional para deploy/container local

## Perfiles

El backend usa perfiles de Spring:

- `dev`: desarrollo local con H2 en memoria y consola H2.
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

`APP_FRONTEND_URL` puede recibir mas de un origen separado por comas, por ejemplo:

```bash
APP_FRONTEND_URL=http://localhost:4200,https://tu-frontend.vercel.app
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

Ejecutar contenedor:

```bash
docker run -p 8080:8080 --env SPRING_PROFILES_ACTIVE=dev barbershop-api
```

Para demo se deben pasar las variables de PostgreSQL, JWT y frontend.

## Datos demo

El proyecto incluye `src/main/resources/data.sql` con datos precargados e idempotentes:

- Barberia demo.
- Sucursales.
- Usuario administrador.
- Clientes.
- Turnos.
- Visitas.
- Pagos.

En `dev` y `demo`, `spring.sql.init.mode=always` permite cargar esos datos al iniciar.

## Modulos principales

```text
src/main/java/com/barbershop/shifts/
|-- configurations/      Seguridad, CORS y configuracion general
|-- controllers/         Endpoints REST
|-- dtos/                Requests y responses
|-- entities/            Modelo de dominio JPA
|-- repositories/        Repositorios Spring Data
`-- services/            Reglas de negocio
```

## Seguridad

- Login y registro mediante endpoints de autenticacion.
- Tokens JWT firmados.
- Endpoints protegidos por Spring Security.
- CORS configurable por `APP_FRONTEND_URL`.
- Contexto de sucursal recibido desde el header `X-Branch-Id`.

## Deploy sugerido

Render:

- Runtime: Docker.
- Root directory: raiz del repo backend.
- Branch: `main`.
- Variables:
  - `SPRING_PROFILES_ACTIVE=demo`
  - `SPRING_DATASOURCE_URL`
  - `SPRING_DATASOURCE_USERNAME`
  - `SPRING_DATASOURCE_PASSWORD`
  - `APP_JWT_SECRET`
  - `APP_FRONTEND_URL`

Base de datos:

- Neon PostgreSQL.
- Usar URL JDBC en `SPRING_DATASOURCE_URL`.
- Mantener credenciales fuera del repositorio.
