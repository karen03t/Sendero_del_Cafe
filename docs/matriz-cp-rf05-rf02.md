# Matriz de casos de prueba — RF-05 y RF-02

**Sprint 2 · Calidad e Integración**
Evidencia ejecutable: `postman/PDSW-RF05.postman_collection.json` (14 requests, 28 aserciones) y `LugarFlowTest` (4 pruebas de integración).

## RF-05 · Módulo administrativo del operador

| CP | CA / RN | Caso | Request | Esperado |
|---|---|---|---|---|
| CP-05 | CA-05 | Registrar un lugar con operador autenticado | `02 · CA-05 Crear lugar` | 201, `estado=ACTIVO`, devuelve `id` |
| CP-06 | CA-06 | Editar un lugar existente | `07 · CA-06 Editar lugar` | 200, nombre actualizado |
| CP-07 | CA-08 | Inactivar un lugar | `08 · CA-08 Inactivar lugar` | 200, código `LUGAR_INACTIVADO` |
| CP-08 | CA-07 / RN-07 | Un turista intenta crear un lugar | `11 · Crear como turista` | 403 |
| CP-09 | RN-07 | Crear un lugar sin token | `10 · Crear sin token` | 401 o 403 |
| CP-10 | D-04 | Consultar la auditoría de un lugar | `09 · Auditoría` | 200 con `INACTIVAR`, `EDITAR`, `CREAR` |
| CP-11 | RN-07 | Registro público de operadores | `12 · Registro público de operador` | 403, no se crea la cuenta |
| CP-12 | — | Login con contraseña incorrecta | `13 · Login inválido` | 401, `CREDENCIALES_INVALIDAS` |

## RF-02 · Explorar y consultar sitios de interés

| CP | CA | Caso | Request | Esperado |
|---|---|---|---|---|
| CP-13 | RF-02 | Listar lugares activos | `03 · Listar lugares activos` | 200, incluye el lugar creado |
| CP-14 | RF-02 | Consultar el detalle de un lugar activo | `04 · Consultar detalle` | 200, mismo `id` |
| CP-15 | RF-02 | Filtrar por categoría | `05 · Filtrar por categoría` | 200, solo de esa categoría |
| CP-16 | RF-02 | Filtrar por municipio | `06 · Filtrar por municipio` | 200, solo de ese municipio |
| CP-17 | RN-01 | Un lugar inactivo no aparece en el listado | cubierto en `LugarFlowTest` | lista vacía |
| CP-18 | RN-01 | Detalle de un lugar inactivo | cubierto en `LugarFlowTest` | 404 `LUGAR_NO_DISPONIBLE` |

## Reglas de negocio cubiertas

| Regla | Cómo se verifica |
|---|---|
| RN-01 | El listado público filtra `estado=ACTIVO`; el detalle de inactivos da 404 |
| RN-07 | Solo `OPERADOR` puede crear, editar e inactivar; el turista recibe 403 |
| RN-08 | Validaciones de nombre, descripción, categoría, dirección, municipio y coordenadas |
| D-04 | Cada mutación escribe una fila en `auditoria_lugar` dentro de la misma transacción |

## Ejecución

```bash
# 1. Base y aplicación
./dev-up.sh            # o el arranque habitual del equipo
# 2. Colección
npx newman run postman/PDSW-RF05.postman_collection.json \
  -e postman/PDSW-Local.postman_environment.json
# 3. Pruebas de integración
cd backend && mvn test
```

Resultado esperado: **14 requests, 28 aserciones, 0 fallos** y **8 pruebas** (4 unitarias + 4 de integración).

## Observaciones de rendimiento

Con 2.000 lugares de prueba, la consulta de RF-02 pasó de recorrer 1.715 filas usando `idx_lugar_estado` a leer 402 usando `idx_lugar_categoria`, tras cambiar los filtros de categoría y municipio de `LIKE '%texto%'` a igualdad.

Pendiente para una migración futura: un índice compuesto `(estado, categoria, municipio)` para cuando el catálogo crezca. No se creó en V3 para no chocar con la numeración de la V4 de rutas.
