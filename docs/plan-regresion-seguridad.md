# Plan de regresión y revisión de seguridad — API Sendero del Café

**Fecha:** 27/11/2026
**Alcance:** backend (API REST) en `develop`, migraciones V1→V6.
**Fuera de alcance:** cliente móvil (aún no existe) y despliegue en producción.

## 1. Suites automáticas

| Suite | Cobertura | Resultado actual |
|---|---|---|
| `mvn test` | 23 pruebas: TokenService 4, GrafoRutas 2, Retención 2, Límite 2, Lugares 6, Rutas 7 | 23/23 verde |
| Flyway | V1→V6 desde base vacía | 6/6 `success=1` |
| Newman RF-01 | 9 requests / 22 aserciones | verde |
| Newman RF-05 | 15 requests / 33 aserciones | verde |
| Newman RF-03 | 17 requests / 17 aserciones | verde |
| CI GitHub | build + tests + MySQL + Flyway + Gitleaks | verde |

## 2. Checklist antes de cada entrega

```bash
# 1. Base limpia y demo
DEMO_DB=turismo_regresion ~/Projects/PDSW/sprint2-demo.sh

# 2. Pruebas de integración
cd backend && mvn -B test

# 3. Colecciones contra el demo
npx newman run postman/PDSW-RF01.postman_collection.json -e postman/PDSW-Local.postman_environment.json --env-var baseUrl=http://127.0.0.1:8081
npx newman run postman/PDSW-RF05.postman_collection.json -e postman/PDSW-Local.postman_environment.json --env-var baseUrl=http://127.0.0.1:8081
npx newman run postman/PDSW-RF03.postman_collection.json -e postman/PDSW-Local.postman_environment.json --env-var baseUrl=http://127.0.0.1:8081

# 4. Límite de intentos (31 intentos → 429)
for i in $(seq 1 31); do curl -s -o /dev/null -w '%{http_code}\n' -X POST http://127.0.0.1:8081/api/v1/auth/login -H 'Content-Type: application/json' -d '{"correo":"x@x.co","password":"mal"}'; done | sort | uniq -c

# 5. Cabeceras de seguridad
curl -sI http://127.0.0.1:8081/api/v1/lugares | grep -Ei 'x-frame-options|x-content-type-options|access-control-allow-origin'
```

## 3. Controles implementados

- JWT HS512 con roles `TURISTA` / `OPERADOR`; registro público de operadores en `denyAll`.
- Contraseñas BCrypt; el hash nunca sale en las respuestas.
- Límite de 30 intentos/minuto por IP y ruta en login y registro (`429 DEMASIADAS_SOLICITUDES`).
- CORS restringido por `SEGURIDAD_CORS_ORIGENES`; cabeceras `DENY` y `nosniff`.
- Secretos solo por variables de entorno, sin valores por defecto en `application.yml`.
- Gitleaks en CI con `fetch-depth: 0`.

## 4. Riesgos abiertos

| Riesgo | Impacto | Mitigación |
|---|---|---|
| `JWT_SECRET` histórico en el commit `b843a62` (repo público) | Tokens falsificables si se despliega con ese valor | Rotar el secreto en cada entorno; la excepción de Gitleaks es solo para no bloquear el CI |
| Rate limit en memoria por instancia | Con réplicas el límite se multiplica | Redis o balanceador cuando haya más de una instancia |
| Sin pentest ni SAST adicional | Vulnerabilidades no detectadas | Gitleaks + revisión manual; evaluar OWASP Dependency-Check |
| CORS solo para orígenes locales | El cliente móvil no usará CORS, pero la web futura sí | Añadir el dominio real al desplegar |
| Cifrado en reposo depende del disco | Datos personales en claro en el servidor | Cifrado de disco y backups cifrados en producción |
| Cliente offline inexistente (RF-04) | requisito incompleto | Fase del cliente móvil |

## 5. Criterios de salida para el cliente móvil

1. RF-04 probado en modo avión: guardar, cerrar, abrir sin red y sincronizar sin duplicados.
2. Contrato de `GET /lugares` (envelope paginado) y de rutas congelado y documentado.
3. `JWT_SECRET` rotado y `SEGURIDAD_CORS_ORIGENES` con el dominio real.
4. Suites de este plan en verde sobre la base de producción.
