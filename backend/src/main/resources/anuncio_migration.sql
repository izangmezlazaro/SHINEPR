ALTER TABLE anuncio RENAME COLUMN contenido TO mensaje;
ALTER TABLE anuncio RENAME COLUMN etiqueta  TO tag;
ALTER TABLE anuncio RENAME COLUMN fecha_publicacion TO fecha;
ALTER TABLE anuncio ADD COLUMN autor VARCHAR(255);
ALTER TABLE anuncio DROP COLUMN id_autor;
ALTER TABLE anuncio ADD COLUMN tag_label VARCHAR(100);
ALTER TABLE anuncio ALTER COLUMN mensaje DROP NOT NULL;
ALTER TABLE anuncio ALTER COLUMN tag     DROP NOT NULL;
