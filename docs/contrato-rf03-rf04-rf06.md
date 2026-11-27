# Contrato — RF-03, RF-04 y RF-06

Diseño acordado antes de implementar. Base: V4 (rutas), V5 (retención), V6 (estado de ruta guardada y sincronización).

## RF-03 · Generar, personalizar y guardar rutas

| Método | Ruta | Rol | Qué hace |
|---|---|---|---|
| POST | `/api/v1/rutas/propuesta` | Turista | Genera una propuesta de ruta según intereses y tiempo disponible. **No persiste**. |
| POST | `/api/v1/rutas` | Turista | Guarda una ruta con las paradas en el orden elegido. |
| GET | `/api/v1/rutas` | Turista | Lista sus rutas guardadas activas. |
| GET | `/api/v1/rutas/{id}` | Turista (dueño) | Detalle con paradas ordenadas. |
| PUT | `/api/v1/rutas/{id}` | Turista (dueño) | Reemplaza paradas: añadir, quitar y reordenar. Recalcula métricas. |

### Propuesta

```json
POST /api/v1/rutas/propuesta
{
  "municipio": "Salento",
  "intereses": ["CULTURA", "NATURALEZA"],
  "tiempoDisponibleMin": 240,
  "origen": { "lat": 4.637, "lng": -75.571 }
}
```

`origen` es opcional: si no llega, se parte del lugar más cercano al centroide del municipio. La ubicación **no se almacena** (RN-02: solo se pide para calcular o ajustar).

Respuesta 200:

```json
{
  "municipio": "Salento",
  "distanciaKm": 6.42,
  "duracionMin": 215,
  "paradas": [
    { "orden": 1, "lugarId": "...", "nombre": "Calle Real", "categoria": "CULTURA",
      "lat": 4.63, "lng": -75.57, "minutosDesdeAnterior": 0 }
  ]
}
```

### Reglas de cálculo

- Solo lugares `ACTIVO` (RN-01) y del municipio solicitado.
- Filtro por intereses; sin intereses, se usan todos.
- Distancia: haversine entre paradas.
- Velocidad de caminata: 4.5 km/h. Visita por parada: 30 min.
- Se agregan paradas mientras quepan en `tiempoDisponibleMin`; máximo 12.
- Orden: vecino más cercano desde el origen y mejora 2-opt. Se usa esta heurística y no Dijkstra porque no hay red vial modelada; Dijkstra sobre un grafo completo de lugares devolvería la misma distancia directa.
- Si no hay lugares que cumplan: 404 `SIN_LUGARES`.
- Si el tiempo no alcanza ni para una parada: 400 `TIEMPO_INSUFICIENTE`.

### Guardar

```json
POST /api/v1/rutas
{ "nombre": "Café y cultura en Salento", "municipio": "Salento",
  "paradas": ["lugar-1", "lugar-2", "lugar-3"] }
```

Valida que cada lugar exista, esté activo y pertenezca al municipio; recalcula distancia y duración (no confía en el cliente). 201 con la ruta creada.

## RF-06 · Inactivar y recuperar rutas guardadas

| Método | Ruta | Efecto |
|---|---|---|
| DELETE | `/api/v1/rutas/{id}` | Marca la ruta como `INACTIVA` (CA-03: no borra) |
| PATCH | `/api/v1/rutas/{id}/inactivar` | Igual que DELETE, idempotente |
| PATCH | `/api/v1/rutas/{id}/reactivar` | Devuelve a `ACTIVA` |
| GET | `/api/v1/rutas/inactivas` | Papelera del perfil |

El estado vive en `ruta_guardada.estado` (V6). Una ruta inactiva no aparece en `GET /rutas` pero sigue existiendo.

## RF-04 · Consulta sin conexión y sincronización

El almacenamiento local es responsabilidad del cliente (RN-04). El backend expone lo necesario para sincronizar:

```
GET /api/v1/rutas/sincronizar?desde=2026-11-10T08:00:00
→ { "desde": "...", "servidor": "2026-11-10T09:12:03",
    "rutas": [ ... ],            // creadas o modificadas después de `desde`
    "eliminadas": [ "rutaId" ] } // inactivadas en ese intervalo
```

`ruta_guardada.fecha_actualizacion` (V6, `ON UPDATE CURRENT_TIMESTAMP`) es la marca que hace posible el delta. Sin ella habría que reenviar todo el perfil en cada sincronización.

**Zona horaria:** las columnas son `DATETIME` sin zona, así que `desde` se interpreta en la hora local del servidor (America/Bogota). El cliente debe enviar ese formato, no UTC, o el delta saldrá vacío. Si el proyecto se despliega en varias zonas, habría que migrar estas columnas a `TIMESTAMP`/UTC.

## Códigos de error

| Código | HTTP | Cuándo |
|---|---|---|
| `SIN_LUGARES` | 404 | Ningún lugar cumple municipio/intereses |
| `TIEMPO_INSUFICIENTE` | 400 | El tiempo no alcanza para una parada |
| `RUTA_NO_ENCONTRADA` | 404 | La ruta no existe o no es del turista |
| `LUGAR_INACTIVO` | 409 | Se intenta guardar un lugar inactivo |
| `SIN_PERMISO` | 403 | Otro turista u otro rol |
