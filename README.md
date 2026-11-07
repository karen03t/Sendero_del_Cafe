# Sendero del Café · Backend

Aplicación de turismo para el Eje Cafetero — Proyecto de Desarrollo de Software (UAM, Grupo 7).

Sprint 1 · **RF-01: registro de turista** con selección de intereses (CDU-01, RN-06, CA-01, CA-02, RNF-02).

## Estructura

```
backend/            API REST en Spring Boot 4 (Java 21) + Flyway
  src/main/java/co/edu/autonoma/turismo/
    api/            controladores y DTOs del contrato
    domain/         entidades JPA (Turista, Interes, TuristaInteres)
    repository/     repositorios Spring Data
    service/        reglas de negocio (RegistroService, TokenService)
    exception/      manejo de errores con código y mensaje
  src/main/resources/db/migration/   migraciones Flyway (esquema)
postman/            colección con CP-01 a CP-04 y entorno local
docs/               entregable de Backend y Datos del taller
docker-compose.yml  MySQL 8.4 para el equipo
```

## Requisitos

- JDK 21
- Maven 3.9+ (o usar `backend/mvnw`)
- Docker (recomendado para la base de datos) o MySQL 8.0+

## Cómo correr

```bash
# 1. Credenciales
cp .env.example .env        # ajusta DB_PASS y JWT_SECRET

# 2. Base de datos
docker compose up -d        # MySQL 8.4 en 127.0.0.1:3306

# 3. Backend
cd backend
set -a && source ../.env && set +a
./mvnw spring-boot:run      # http://127.0.0.1:8080
```

Flyway crea el esquema y el catálogo de intereses en el primer arranque; no hay que ejecutar SQL a mano.

## Endpoints

### `POST /api/v1/auth/registro`

```json
{
  "nombreCompleto": "Ana Ríos",
  "correo": "ana.rios@ejemplo.com",
  "intereses": [1, 2],
  "proveedor": "LOCAL",
  "password": "Secreta123",
  "aceptaTratamientoDatos": true
}
```

`201 Created` → `{id, nombreCompleto, correo, intereses, tokenSesion}`

| Situación | HTTP | `codigo` |
|---|---|---|
| Correo ya registrado | 409 | `CORREO_DUPLICADO` |
| Sin intereses | 400 | `SIN_INTERESES` |
| Falta nombre / correo | 400 | `NOMBRE_OBLIGATORIO` / `CORREO_OBLIGATORIO` |
| Correo mal formado | 400 | `CORREO_FORMATO_INVALIDO` |
| Contraseña ausente o < 8 | 400 | `PASSWORD_OBLIGATORIO` / `PASSWORD_MINIMO` |
| Proveedor externo sin token | 400 | `TOKEN_EXTERNO_OBLIGATORIO` |
| Sin aceptar tratamiento (Ley 1581) | 400 | `CONSENTIMIENTO_REQUERIDO` |
| Interés inexistente o inactivo | 400 | `INTERES_INEXISTENTE` |

### `GET /api/v1/intereses`
Catálogo activo: `[{"id":1,"nombre":"ecológico"}, …]`

## Probar con Postman

Importar `postman/PDSW-RF01.postman_collection.json` y `postman/PDSW-Local.postman_environment.json`,
seleccionar el entorno **PDSW · Local** y ejecutar en orden (el `01` crea el correo que usa el `02`).
Para evidencia completa: *Run collection* → 9 requests, 22 aserciones.

## Ramas y commits

- `master` estable ← `develop` integración ← `feature/SCRUM-XX-descripcion`
- Commits: `feat(SCRUM-16): contrato de registro de turista`

## Documentación

- `docs/taller-entregable-BE-DAT-RF01.md` — contrato REST y modelo de datos del Sprint 1.
