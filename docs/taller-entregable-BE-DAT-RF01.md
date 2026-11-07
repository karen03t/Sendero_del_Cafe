# Entregable Backend + Datos — Sprint 1 / RF-01 (SCRUM-15 + SCRUM-16)
Proyecto: Aplicación de turismo para el Eje Cafetero · Grupo 7 · Samuel David López Sarabia

## Actividad 4. Contrato REST inicial (BE — SCRUM-16)

| Elemento | Definición del equipo |
|---|---|
| Método y ruta | `POST /api/v1/auth/registro` (reemplaza `POST /api/sendero_cafe`: sin verbo en ruta, con versión y recurso `auth/registro`) |
| Entrada | JSON: `nombreCompleto` (string, obligatorio), `correo` (string, formato email, obligatorio), `intereses` (array de IDs, mín. 1, obligatorio), `proveedor` (`LOCAL` \| `GOOGLE` \| `APPLE`, default `LOCAL`), `idTokenExterno` (obligatorio solo si proveedor ≠ LOCAL), `password` (obligatorio solo si LOCAL, mín. 8), `aceptaTratamientoDatos` (boolean, debe ser `true` — RNF-02 / Ley 1581). Ejemplo: `{"nombreCompleto":"Ana Ríos","correo":"ana@ejemplo.com","intereses":[1,3],"proveedor":"LOCAL","password":"********","aceptaTratamientoDatos":true}` |
| Éxito | `201 Created`: turista creado y sesión iniciada. Body: `{"id":"uuid","nombreCompleto":"...","correo":"...","intereses":[1,3],"tokenSesion":"jwt..."}`. Trazabilidad: CA-01, CP-01. |
| Error por regla | `409 Conflict`: `{"codigo":"CORREO_DUPLICADO","mensaje":"Ya existe una cuenta con este correo"}` — correo ya registrado (RN-06 propuesta, CP-02). `400 Bad Request`: `{"codigo":"SIN_INTERESES","mensaje":"Elija al menos un interés para continuar"}` — array vacío/ausente (CA-02 original, CP-02 variante). En ambos no se crea ni se modifica ningún registro (CP-04). |
| Error por dato | `400 Bad Request` con `codigo` + `mensaje` que nombra el dato: `NOMBRE_OBLIGATORIO` ("El nombre completo es obligatorio"), `CORREO_OBLIGATORIO` ("El correo es obligatorio"), `CORREO_FORMATO_INVALIDO`, `PASSWORD_MINIMO`, `INTERES_INEXISTENTE` (id no existe en catálogo), `CONSENTIMIENTO_REQUERIDO` ("Debe aceptar el tratamiento de datos — Ley 1581 de 2012"). Trazabilidad: CA-01, CP-03. |
| Error por permiso | No aplica 403: el registro es endpoint público. La restricción aplicable es RNF-02 — sin `aceptaTratamientoDatos=true` se rechaza con 400 `CONSENTIMIENTO_REQUERIDO`. El módulo operador (RF-05) sí exigirá rol operador; queda fuera de este contrato. |
| Trazabilidad | RF-01 · CDU-01 (precondición para consultar/generar ruta) · RN-06 (propuesta: correo único + mín. 1 interés) · CA-01 (éxito) / CA-02 (rechazo sin interés) · RNF-02 (habeas data) · CP-01 a CP-04 |
| Auth del servicio | Público, sin token previo. Respuesta 201 entrega `tokenSesion` (JWT corta duración). Rate-limit básico anti-spam de registros (p. ej. 10 intentos/IP/hora — a definir con Integración). |

**Supuestos explícitos:** (1) RF-01 no define contraseña; se asume LOCAL=password e idTokenExterno=OAuth (sin guardar secreto externo). (2) RN-06 no existe en el backlog anterior (RN-01 a RN-05 no cubren registro); se propone y se deja a Producto para numeración final. (3) El mapa CDU del taller anterior asigna RF-01→CDU-01 por arrastre; RF-01 es en realidad precondición de CDU-01/CDU-02, se declara así para no inventar un CDU nuevo en Sprint 1.

## Actividad 5. Modelo de datos inicial (DAT — SCRUM-15)

| Entidad | Atributos | Estados | Relaciones | Restricción / RN |
|---|---|---|---|---|
| Turista | `id` (uuid, PK), `nombre_completo`, `correo` (único), `password_hash` (nullable, solo LOCAL), `proveedor` (LOCAL/GOOGLE/APPLE), `proveedor_id` (nullable, id externo), `acepta_tratamiento` (bool), `fecha_consentimiento`, `estado`, `fecha_creacion` | ACTIVO, INACTIVO (baja lógica; sin borrado físico — D-08 / RNF-02) | Tiene N intereses vía Turista_Interes; posee N rutas (a definir en RF-03, fuera de este modelo) | `correo` único (RN-06); si LOCAL exige `password_hash`, si externo exige `proveedor_id` y prohíbe `password_hash`; `acepta_tratamiento=true` obligatorio (RNF-02) |
| Interes | `id` (int, PK), `nombre` (ecológico, gastronómico, local, …), `activo` (bool) | — (catálogo; activo/inactivo por operador en RF-05) | Referenciado por N turistas | `nombre` único; solo `activo=true` ofrecible en registro |
| Turista_Interes | `turista_id` (FK), `interes_id` (FK), `fecha_asignacion` — PK compuesta (turista_id, interes_id) | — | Pertenece a un Turista y a un Interes (N:M) | Mín. 1 fila por turista ACTIVO (CA-02); FK con `ON DELETE RESTRICT`; inactivar turista no borra filas (auditoría/RNF-02) |

**Compatibilidad con Backend:** el modelo soporta todo el contrato: `POST` valida `correo` contra índice único, `intereses[]` contra `Interes.activo=true`, crea 1 fila `Turista` + N filas `Turista_Interes` en una sola transacción (si falla una, no se crea nada — CP-04). `GET /api/v1/intereses` (catálogo, solo lectura, a implementar por BE si Producto lo pide) lee de `Interes`.

### DDL mínimo (PostgreSQL, anexo para Jira)

```sql
CREATE TABLE turista (
  id UUID PRIMARY KEY DEFAULT gen_random_uuid(),
  nombre_completo TEXT NOT NULL,
  correo CITEXT UNIQUE NOT NULL,
  password_hash TEXT NULL,
  proveedor TEXT NOT NULL DEFAULT 'LOCAL' CHECK (proveedor IN ('LOCAL','GOOGLE','APPLE')),
  proveedor_id TEXT NULL,
  acepta_tratamiento BOOLEAN NOT NULL DEFAULT FALSE,
  fecha_consentimiento TIMESTAMPTZ NULL,
  estado TEXT NOT NULL DEFAULT 'ACTIVO' CHECK (estado IN ('ACTIVO','INACTIVO')),
  fecha_creacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  CHECK ((proveedor='LOCAL' AND password_hash IS NOT NULL)
      OR (proveedor<>'LOCAL' AND proveedor_id IS NOT NULL AND password_hash IS NULL)),
  CHECK (acepta_tratamiento = TRUE)
);
CREATE TABLE interes (
  id SERIAL PRIMARY KEY,
  nombre TEXT UNIQUE NOT NULL,
  activo BOOLEAN NOT NULL DEFAULT TRUE
);
CREATE TABLE turista_interes (
  turista_id UUID REFERENCES turista(id) ON DELETE RESTRICT,
  interes_id INT REFERENCES interes(id) ON DELETE RESTRICT,
  fecha_asignacion TIMESTAMPTZ NOT NULL DEFAULT now(),
  PRIMARY KEY (turista_id, interes_id)
);
INSERT INTO interes (nombre) VALUES ('ecológico'),('gastronómico'),('local') ON CONFLICT DO NOTHING;
```

## Actividad 9. Compromisos (filas BE + DAT para pegar)

| Rol | Entrego | Se revisa | Dependo de | Bloqueo | Evidencia |
|---|---|---|---|---|---|
| Backend | Contrato `POST /api/v1/auth/registro` con 201/400/409 y mensajes exactos (SCRUM-16) | Próxima clase | SCRUM-15 (modelo) — ya alineado | Ninguno tras esta entrega; se desbloquea SCRUM-17 | Contrato adjunto a SCRUM-16 en Jira |
| Datos | Modelo Turista–Interes–Turista_Interes + DDL + restricción RN-06 (SCRUM-15) | Próxima clase | — (base, sin dependencias) | Solo pendiente validación Ley 1581 para retención/eliminación (D-08, va a Sprint 2) | Modelo + DDL adjuntos a SCRUM-15 en Jira |

## Efecto sobre la revisión cruzada (Act. 8)

- Fila Backend|Calidad pasa a "Sí, encaja" una vez adjuntados SCRUM-15/16: los códigos que Calidad supuso (201 / 409 "Ya existe una cuenta con este correo" / 400 dato faltante) quedan confirmados como contrato, y CP-04 (no modificación tras 409/400) queda garantizado por transacción única.
- Ajuste pedido por Producto (error 400 sin interés con mensaje exacto del boceto y sin crear cuenta) queda cubierto por `SIN_INTERESES`.
- Alcance nuevo rechazado: recuperación de contraseña, verificación de correo, notificaciones — fuera del MVP, no entran al contrato ni al modelo.
