-- Migración: alinear tabla anuncio con el modelo del código
-- Ejecutar una sola vez en la BD

ALTER TABLE anuncio RENAME COLUMN contenido TO mensaje;
ALTER TABLE anuncio RENAME COLUMN etiqueta  TO tag;
ALTER TABLE anuncio RENAME COLUMN fecha_publicacion TO fecha;

-- Cambiar id_autor (int) por autor (varchar)
ALTER TABLE anuncio ADD COLUMN autor VARCHAR(255);
ALTER TABLE anuncio DROP COLUMN id_autor;

-- Añadir tag_label que usa el código
ALTER TABLE anuncio ADD COLUMN tag_label VARCHAR(100);

-- Hacer mensaje y tag opcionales (ya lo son en el código)
ALTER TABLE anuncio ALTER COLUMN mensaje DROP NOT NULL;
ALTER TABLE anuncio ALTER COLUMN tag     DROP NOT NULL;
