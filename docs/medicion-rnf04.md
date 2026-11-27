# Medición RNF-04 — tiempo de respuesta menor a 3 s

**Fecha:** 27/11/2026
**Entorno:** local, i7-11800H, 15 GiB RAM, MySQL 8.4 userland, JVM 21 (Temurin), Spring Boot 4.1.2
**Base de datos:** `turismo_gaps` (3 lugares en Salento, migraciones V1→V6)
**Script:** `~/.hermes/cache/scratch/medir_rnf04.py` (urllib + ThreadPoolExecutor)

## Método

- `GET /api/v1/lugares?tamano=20`: 300 peticiones, 20 concurrentes.
- `POST /api/v1/rutas/propuesta` (municipio Salento, interés ecológico, 240 min):
  120 peticiones, 10 concurrentes, con token de turista.
- Se registran todos los códigos HTTP y los percentiles de latencia.

## Resultados

| Endpoint | Peticiones | Concurrentes | Códigos | p50 | p95 | p99 | Máx |
|---|---|---|---|---|---|---|---|
| `GET /api/v1/lugares` | 300 | 20 | 200 × 300 | 19 ms | 32 ms | 40 ms | 46 ms |
| `POST /api/v1/rutas/propuesta` | 120 | 10 | 200 × 120 | 14 ms | 25 ms | 28 ms | 28 ms |

## Conclusión

Ambos endpoints quedan muy por debajo del límite de 3 s (RNF-04), con un p95 de 32 ms
y 25 ms respectivamente y sin errores HTTP. La medición es local y sirve como línea
base; en un entorno desplegado habrá que repetirla con la red y el hardware definitivos.
