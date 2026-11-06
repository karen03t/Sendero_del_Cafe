## Operador municipal que gestiona los lugares turísticos en el sistema de turismo.

CREATE TABLE operador_municipal (
    id VARCHAR(36) NOT NULL,
    nombre_completo VARCHAR(200) NOT NULL,
    correo VARCHAR(255) NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVO',
    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    UNIQUE KEY uq_operador_correo (correo),

    CONSTRAINT chk_operador_estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO')),

    CONSTRAINT chk_operador_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre_completo)) > 0)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


## Tabla para almacenar los lugares turísticos registrados por los operadores municipales

CREATE TABLE lugar (
    id VARCHAR(36) NOT NULL,
    nombre VARCHAR(200) NOT NULL,
    descripcion TEXT NOT NULL,
    categoria VARCHAR(100) NOT NULL,
    direccion VARCHAR(255) NOT NULL,
    municipio VARCHAR(100) NOT NULL,

    latitud DECIMAL(10,7) NOT NULL,
    longitud DECIMAL(10,7) NOT NULL,

    estado VARCHAR(10) NOT NULL DEFAULT 'ACTIVO',

    fecha_creacion DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
    fecha_actualizacion DATETIME NULL,

    operador_creacion_id VARCHAR(36) NOT NULL,

    PRIMARY KEY (id),

    CONSTRAINT fk_lugar_operador
        FOREIGN KEY (operador_creacion_id)
        REFERENCES operador_municipal(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_lugar_estado
        CHECK (estado IN ('ACTIVO', 'INACTIVO')),

    CONSTRAINT chk_lugar_nombre
        CHECK (CHAR_LENGTH(TRIM(nombre)) > 0),

    CONSTRAINT chk_lugar_descripcion
        CHECK (CHAR_LENGTH(TRIM(descripcion)) > 0),

    CONSTRAINT chk_lugar_categoria
        CHECK (CHAR_LENGTH(TRIM(categoria)) > 0),

    CONSTRAINT chk_lugar_direccion
        CHECK (CHAR_LENGTH(TRIM(direccion)) > 0),

    CONSTRAINT chk_lugar_municipio
        CHECK (CHAR_LENGTH(TRIM(municipio)) > 0),

    CONSTRAINT chk_lugar_latitud
        CHECK (latitud BETWEEN -90 AND 90),

    CONSTRAINT chk_lugar_longitud
        CHECK (longitud BETWEEN -180 AND 180)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;


## Auditoría de cambios en la tabla lugar
CREATE TABLE auditoria_lugar (
    id BIGINT NOT NULL AUTO_INCREMENT,

    lugar_id VARCHAR(36) NOT NULL,
    operador_id VARCHAR(36) NOT NULL,

    accion VARCHAR(20) NOT NULL,

    dato_modificado JSON NOT NULL,

    fecha DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

    PRIMARY KEY (id),

    CONSTRAINT fk_auditoria_lugar
        FOREIGN KEY (lugar_id)
        REFERENCES lugar(id)
        ON DELETE RESTRICT,

    CONSTRAINT fk_auditoria_operador
        FOREIGN KEY (operador_id)
        REFERENCES operador_municipal(id)
        ON DELETE RESTRICT,

    CONSTRAINT chk_auditoria_accion
        CHECK (
            accion IN (
                'CREACION',
                'EDICION',
                'INACTIVACION'
            )
        ),

    INDEX idx_auditoria_lugar (lugar_id),
    INDEX idx_auditoria_operador (operador_id),
    INDEX idx_auditoria_fecha (fecha)

) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4
  COLLATE=utf8mb4_unicode_ci;
