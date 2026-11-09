-- V3 · RF-02 Índices para filtro por interés y ubicación
-- Trazabilidad: SCRUM-26, SCRUM-7 (Explorar y consultar sitios de interés)
-- Justificación: acelerar consultas de búsqueda de lugares por estado,
-- categoría, municipio y coordenadas; y filtro de turistas por interés.

-- 1. Lugar: filtro por estado (solo ACTIVO en listados, RN-01)
CREATE INDEX idx_lugar_estado ON lugar (estado);

-- 2. Lugar: filtro por categoría y municipio
CREATE INDEX idx_lugar_categoria ON lugar (categoria);
CREATE INDEX idx_lugar_municipio ON lugar (municipio);

-- 3. Lugar: búsqueda por coordenadas (latitud, longitud)
CREATE INDEX idx_lugar_coordenadas ON lugar (latitud, longitud);

-- 4. Turista-Interés: filtro de lugares según intereses del turista
CREATE INDEX idx_ti_interes ON turista_interes (interes_id);

-- 5. Interés: filtro solo activos
CREATE INDEX idx_interes_activo ON interes (activo);