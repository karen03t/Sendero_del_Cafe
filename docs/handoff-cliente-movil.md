# Handoff — Cliente móvil Sendero del Café

Documento de traspaso para iniciar el desarrollo de la app móvil en una sesión nueva.
Fecha: 27/11/2026.

## 1. Contexto del proyecto

- Proyecto académico UAM: aplicación de turismo para el **Eje Cafetero** (Quindío, Risaralda, Caldas).
- Backend terminado y mergeado en `develop`; esta fase es solo el **cliente móvil**.
- Repo: `https://github.com/karen03t/Sendero_del_Cafe`
- Stack backend: Spring Boot 4.1.2, Java 21, MySQL 8.4, Flyway V1→V6, JWT HS512.
- El backend local corre en `http://127.0.0.1:8080` como servicio de usuario (`pdsw-backend`).
- Roles del equipo: Karen (Producto), Isabela (Backend), César (Datos), Samuel (Calidad e Integración).
- En Jira y reportes **no se menciona el uso de IA**.

## 2. Decisiones ya tomadas

- Cliente **móvil** (Android e iOS), no web.
- Stack propuesto: **Expo + React Native + TypeScript**.
- Sin Swagger.
- Optimización de rutas por tarifas y horarios: **fuera de alcance**, registrada como deuda en **SCRUM-39**.
- No iniciar el Sprint 4 de Jira sin autorización de Samuel.

## 3. Contrato del backend que consume la app

Base: `http://<host>:8080`. Todas las respuestas de error tienen `{codigo, mensaje}`.

### Autenticación

| Método | Ruta | Cuerpo / notas |
|---|---|---|
| POST | `/api/v1/auth/registro` | `{nombreCompleto, correo, password, intereses:[ids], proveedor:"LOCAL", aceptaTratamientoDatos:true}` → 201 + `tokenSesion` |
| POST | `/api/v1/auth/login` | `{correo, password}` → `{token, tipo, rol}` |
| POST | `/api/v1/auth/login-operador` | solo operadores |
| GET | `/api/v1/intereses` | catálogo `[{id, nombre}]` |

El token va en `Authorization: Bearer <token>`. Límite: 30 intentos/minuto por IP y ruta en login/registro (`429 DEMASIADAS_SOLICITUDES`).

### Lugares (RF-02 / RF-05)

| Método | Ruta | Notas |
|---|---|---|
| GET | `/api/v1/lugares` | Paginado: `?nombre=&categoria=&municipio=&interes=&pagina=0&tamano=20` |
| GET | `/api/v1/lugares/{id}` | detalle activo |
| POST | `/api/v1/lugares` | operador |
| PUT | `/api/v1/lugares/{id}` | operador |
| PATCH | `/api/v1/lugares/{id}/inactivar` | operador |
| GET | `/api/v1/lugares/{id}/auditoria` | operador |

Respuesta paginada:

```json
{ "content": [ ... ], "number": 0, "size": 20, "totalElements": 3, "totalPages": 1 }
```

Solo se devuelven lugares `ACTIVO`. Municipios fuera del Eje Cafetero → `400 MUNICIPIO_FUERA_COBERTURA`.

### Rutas (RF-03, RF-04, RF-06)

| Método | Ruta | Notas |
|---|---|---|
| POST | `/api/v1/rutas/propuesta` | `{municipio, intereses:[nombres], tiempoDisponibleMin, origen?:{lat,lng}}` → paradas ordenadas |
| POST | `/api/v1/rutas` | guardar propuesta |
| GET | `/api/v1/rutas` | rutas guardadas del turista |
| GET | `/api/v1/rutas/{id}` | detalle |
| PUT | `/api/v1/rutas/{id}` | reordenar / editar |
| DELETE | `/api/v1/rutas/{id}` | inactivar (baja lógica) |
| PATCH | `/api/v1/rutas/{id}/reactivar` | recuperar |
| GET | `/api/v1/rutas/inactivas` | papelera del turista |
| GET | `/api/v1/rutas/sincronizar?desde=<ISO>` | delta para RF-04 |

Reglas del servidor: solo lugares activos, del mismo municipio, sin duplicados; máximo 12 paradas; 4.5 km/h + 30 min por parada; orden con Dijkstra sobre grafo disperso + 2-opt.

## 4. Alcance del cliente móvil

1. **Autenticación**: registro con intereses, login, sesión persistente.
2. **Explorar**: listado paginado, búsqueda y filtros, ficha de lugar y **mapa**.
3. **Rutas**: generar propuesta, ajustar orden, guardar, consultar, inactivar y recuperar.
4. **Sin conexión (RF-04 / SCRUM-9, 32, 33)**: caché local de lugares y rutas guardadas, consulta en modo avión y sincronización al recuperar red sin duplicados.
5. **Panel del operador**: registrar, editar e inactivar lugares (puede ser una sección dentro de la misma app).

## 5. Modelo offline sugerido

- `expo-sqlite` con tablas espejo: `lugares`, `rutas`, `rutas_guardadas`, `sync_estado`.
- `expo-secure-store` para el JWT.
- Sincronización **pull** con `GET /rutas/sincronizar?desde=`; guardar el `desde` de la última sync.
- Al guardar una ruta se exige red; la consulta offline es de rutas ya guardadas.
- Resolver conflictos por `fechaActualizacion` del servidor (el servidor manda).

## 6. Restricciones reales del entorno

- La red local está aislada (AP isolation): **Expo Go por LAN falla**. Usar `npx expo start --tunnel`.
- El teléfono tampoco alcanzará `127.0.0.1:8080`: opciones probadas o viables:
  - USB tethering (la laptop comparte red al teléfono),
  - túnel al backend (ngrok/cloudflared),
  - o desplegar el backend en un host accesible.
- SDK de Android en `~/.local/share/android-sdk`; EAS CLI global; build `preview` genera APK instalable.
- Sin `sudo`: todo userland (`mise`, systemd `--user`).
- Fuente preferida en interfaces: JetBrainsMono Nerd Font.

## 7. Calidad esperada

- TypeScript estricto, componentes con estados de carga/error/vacío.
- Pruebas unitarias de la capa de datos y de la sincronización (Jest).
- Manejo de 401 (cerrar sesión), 429 (mensaje de espera) y errores de red.
- Español en la interfaz; código y nombres técnicos en inglés.
- No commitear secretos ni tokens; `.env` fuera de git.

## 8. Primer paso sugerido

1. Crear la app Expo en `mobile/` dentro del repo, rama `feature/movil-base`.
2. Cliente HTTP tipado + almacenamiento del token.
3. Pantallas de login/registro contra el backend real.
4. Listado paginado de lugares y ficha.
5. Mapa con los lugares.
6. Rutas y después offline.

## 9. Referencias en el repo

- `README.md` — endpoints, seguridad, cobertura y retención.
- `docs/contrato-rf03-rf04-rf06.md` — contrato de rutas y sincronización.
- `docs/matriz-cp-rf05-rf02.md` — casos de prueba.
- `docs/plan-regresion-seguridad.md` — riesgos y criterios de salida.
- `postman/` — colecciones RF-01, RF-05 y RF-03.
