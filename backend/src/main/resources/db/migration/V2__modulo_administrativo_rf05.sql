-- V2 · RF-05 Módulo administrativo del operador turístico municipal (MySQL 8)
-- Trazabilidad: RN-07 (solo operador gestiona lugares, cambios auditados)
--               RN-08 (datos mínimos obligatorios para publicar)
--               RN-01 (lugar inactivo fuera de listados)
--               CA-05, CA-06, CA-07, CA-08

-- 1. Operador municipal
CREATE TABLE operador_municipal (
  id             VARCHAR(36)  NOT NULL,
  nombre_completo VARCHAR(200) NOT NULL,
  correo         VARCHAR(255) NOT NULL,
  password_hash  VARCHAR(255) NOT NULL,
  estado         VARCHAR(10)  NOT NULL DEFAULT 'ACTIVO',
  fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  UNIQUE KEY uq_operador_correo (correo),
  CONSTRAINT chk_operador_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 2. Lugar turístico
CREATE TABLE lugar (
  id                   VARCHAR(36)    NOT NULL,
  nombre               VARCHAR(200)   NOT NULL,
  descripcion          TEXT           NOT NULL,
  categoria            VARCHAR(100)   NOT NULL,
  direccion            VARCHAR(255)   NOT NULL,
  municipio            VARCHAR(100)   NOT NULL,
  latitud              DECIMAL(10,7)  NOT NULL,
  longitud             DECIMAL(10,7)  NOT NULL,
  estado               VARCHAR(10)    NOT NULL DEFAULT 'ACTIVO',
  fecha_creacion       DATETIME       NOT NULL DEFAULT CURRENT_TIMESTAMP,
  fecha_actualizacion  DATETIME       NULL,
  operador_creacion_id VARCHAR(36)    NOT NULL,
  PRIMARY KEY (id),
  CONSTRAINT fk_lugar_operador FOREIGN KEY (operador_creacion_id)
    REFERENCES operador_municipal (id) ON DELETE RESTRICT,
  CONSTRAINT chk_lugar_estado CHECK (estado IN ('ACTIVO','INACTIVO'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;

-- 3. Auditoría de cambios en lugares (RN-07)
CREATE TABLE auditoria_lugar (
  id              BIGINT       NOT NULL AUTO_INCREMENT,
  lugar_id        VARCHAR(36)  NOT NULL,
  operador_id     VARCHAR(36)  NOT NULL,
  accion          VARCHAR(20)  NOT NULL,
  dato_modificado JSON         NOT NULL,
  fecha           DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (id),
  CONSTRAINT fk_auditoria_lugar FOREIGN KEY (lugar_id)
    REFERENCES lugar (id) ON DELETE RESTRICT,
  CONSTRAINT fk_auditoria_operador FOREIGN KEY (operador_id)
    REFERENCES operador_municipal (id) ON DELETE RESTRICT,
  CONSTRAINT chk_auditoria_accion CHECK (accion IN ('CREAR','EDITAR','INACTIVAR'))
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci;