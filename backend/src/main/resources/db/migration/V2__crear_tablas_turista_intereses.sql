-- =====================================================
-- V2: Tablas del registro de turistas (RF-01)
-- =====================================================

-- 1. Turistas
CREATE TABLE turista (
    id VARCHAR(36) NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    correo VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NULL,
    proveedor VARCHAR(10) NOT NULL DEFAULT 'LOCAL',
    proveedor_id VARCHAR(255) NULL,
    acepta_tratamiento BOOLEAN NOT NULL,
    fecha_consentimiento DATETIME NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uq_turista_correo (correo),

    CONSTRAINT chk_turista_proveedor
        CHECK (proveedor IN ('LOCAL', 'GOOGLE', 'APPLE')),

    CONSTRAINT chk_turista_estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO')),

    CONSTRAINT chk_turista_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre_completo)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- 2. Catálogo de intereses
CREATE TABLE interes (
    id INT NOT NULL AUTO_INCREMENT,
    nombre VARCHAR(100) NOT NULL,
    activo BOOLEAN NOT NULL DEFAULT TRUE,

    PRIMARY KEY (id),

    UNIQUE KEY uq_interes_nombre (nombre),

    CONSTRAINT chk_interes_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- 3. Relación entre turistas e intereses
CREATE TABLE turista_interes (
    turista_id VARCHAR(36) NOT NULL,
    interes_id INT NOT NULL,
    fecha_asignacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (turista_id, interes_id),

    CONSTRAINT fk_turista_interes_turista
        FOREIGN KEY (turista_id)
        REFERENCES turista(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_turista_interes_interes
        FOREIGN KEY (interes_id)
        REFERENCES interes(id)
        ON DELETE RESTRICT,

    INDEX idx_turista_interes_interes (interes_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


-- 4. Datos iniciales del catálogo de intereses
INSERT INTO interes (nombre, activo) VALUES
('Naturaleza', TRUE),
('Aventura', TRUE),
('Cultura', TRUE),
('Gastronomía', TRUE),
('Historia', TRUE);