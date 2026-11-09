-- V5 · Política de retención, eliminación y reactivación (Ley 1581)
-- Trazabilidad: SCRUM-36, RNF-02, D-08

-- 1. Columnas para gestión del ciclo de vida del turista
ALTER TABLE turista
  ADD COLUMN fecha_ultima_actividad DATETIME NULL AFTER fecha_creacion,
  ADD COLUMN fecha_eliminacion DATETIME NULL AFTER fecha_ultima_actividad;

-- 2. Índice para identificar cuentas inactivas (sin actividad > 12 meses)
CREATE INDEX idx_turista_ultima_actividad ON turista (fecha_ultima_actividad);

-- 3. Índice para procesar eliminaciones pendientes
CREATE INDEX idx_turista_eliminacion ON turista (fecha_eliminacion);
