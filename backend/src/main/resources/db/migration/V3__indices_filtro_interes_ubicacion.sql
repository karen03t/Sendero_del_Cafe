-- V3 · RF-02 Índices para filtro por interés y ubicación
-- Trazabilidad: SCRUM-26, SCRUM-7 (Explorar y consultar sitios de interés)
-- Nota: los índices de categoría y municipio están creados para igualdad exacta.
--       Backend actualmente filtra con LIKE + comodines, por lo que estos índices
--       no se aprovechan en ese escenario. Se mantienen para consultas futuras
--       con filtro exacto o para revisión con el equipo.

-- 1. Lugar: filtro por estado (solo ACTIVO en listados, RN-01)
CREATE INDEX idx_lugar_estado ON lugar (estado);

-- 2. Lugar: filtro por categoría y municipio (igualdad, no LIKE)
CREATE INDEX idx_lugar_categoria ON lugar (categoria);
CREATE INDEX idx_lugar_municipio ON lugar (municipio);

-- 3. Lugar: ordenamiento por latitud y longitud
-- Nota: este índice B-tree sirve para ORDER BY latitud, longitud
-- pero NO para búsquedas de proximidad ("cerca de mí").
-- Para proximidad real se requiere columna POINT + índice SPATIAL
-- o cálculo de distancia en la capa de aplicación.
CREATE INDEX idx_lugar_coordenadas ON lugar (latitud, longitud);

-- 4. Turista-Interés: filtro de lugares según intereses del turista
CREATE INDEX idx_ti_interes ON turista_interes (interes_id);

-- 5. Interés: filtro solo activos
CREATE INDEX idx_interes_activo ON interes (activo);

-- EXPLAIN de referencia (ejecutados en entorno local con datos de prueba):
-- EXPLAIN SELECT * FROM lugar WHERE estado = 'ACTIVO';
--   → type: ref, key: idx_lugar_estado
-- EXPLAIN SELECT l.* FROM lugar l JOIN turista_interes ti ON ... WHERE ti.interes_id = 1;
--   → type: ref, key: idx_ti_interes