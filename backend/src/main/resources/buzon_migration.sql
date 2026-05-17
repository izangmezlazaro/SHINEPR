CREATE TABLE IF NOT EXISTS buzon_mensajes (
    id_mensaje SERIAL PRIMARY KEY,
    nombre     VARCHAR(50)  NOT NULL,
    apellidos  VARCHAR(100) NOT NULL,
    asunto     VARCHAR(150) NOT NULL,
    texto      TEXT,
    fecha      TIMESTAMP    DEFAULT CURRENT_TIMESTAMP
)
