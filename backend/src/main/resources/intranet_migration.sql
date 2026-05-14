CREATE TABLE IF NOT EXISTS fichaje (
    id               SERIAL       PRIMARY KEY,
    empleado_email   VARCHAR(255) NOT NULL,
    empleado_nombre  VARCHAR(255) NOT NULL,
    tipo             VARCHAR(10)  NOT NULL CHECK (tipo IN ('ENTRADA','SALIDA')),
    fecha_hora       TIMESTAMP    NOT NULL DEFAULT NOW()
);
CREATE INDEX IF NOT EXISTS idx_fichaje_fecha ON fichaje (fecha_hora::date);
CREATE INDEX IF NOT EXISTS idx_fichaje_email ON fichaje (empleado_email);

CREATE TABLE IF NOT EXISTS anuncio (
    id        SERIAL       PRIMARY KEY,
    titulo    VARCHAR(500) NOT NULL,
    tag       VARCHAR(50)  NOT NULL,
    tag_label VARCHAR(100) NOT NULL,
    mensaje   TEXT,
    autor     VARCHAR(255) NOT NULL,
    fecha     TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS reunion (
    id         SERIAL       PRIMARY KEY,
    titulo     VARCHAR(500) NOT NULL,
    fecha      DATE         NOT NULL,
    hora       VARCHAR(5)   NOT NULL,
    plataforma VARCHAR(50)  NOT NULL,
    asistentes TEXT,
    color      VARCHAR(50)  DEFAULT 'rose',
    creado_por VARCHAR(255),
    creado_en  TIMESTAMP    DEFAULT NOW()
);
