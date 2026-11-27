-- V6 · RF-06 inactivar/recuperar rutas guardadas + soporte de sincronización RF-04
-- Trazabilidad: SCRUM-11 (RF-06), SCRUM-9 (RF-04), CA-03, CA-04
--
-- La V4 modeló ruta_guardada sin estado ni marca de actualización:
--   * sin estado no se puede cumplir CA-03 (eliminar = marcar inactiva y poder recuperarla)
--   * sin fecha_actualizacion no hay forma de responder "qué cambió desde X" para RF-04

ALTER TABLE ruta_guardada
  ADD COLUMN estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVA' AFTER nombre_custom,
  ADD COLUMN fecha_actualizacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
      ON UPDATE CURRENT_TIMESTAMP AFTER fecha_guardado,
  ADD CONSTRAINT chk_rg_estado CHECK (estado IN ('ACTIVA', 'INACTIVA'));

-- Consulta del perfil: rutas activas o inactivas de un turista
CREATE INDEX idx_rg_turista_estado ON ruta_guardada (turista_id, estado);

-- Sincronización incremental (RF-04): cambios posteriores a una marca de tiempo
CREATE INDEX idx_rg_actualizacion ON ruta_guardada (turista_id, fecha_actualizacion);
