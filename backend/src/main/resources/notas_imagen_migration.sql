-- ============================================================
-- Migración: añadir url_imagen a las notas olfativas existentes
-- Solo actualiza filas donde url_imagen todavía es NULL
-- Ejecutar una vez en la BBDD, o dejar que Spring lo ignore
-- (si usas data.sql con spring.sql.init.mode=never)
-- ============================================================

-- Notas de salida (top notes) → imagen cítrica/fresca
UPDATE nota_olfativa SET url_imagen = 'assets/img/product-toner.png'
WHERE tipo = 'salida' AND url_imagen IS NULL;

-- Notas de corazón (heart notes) → imagen sérum/floral
UPDATE nota_olfativa SET url_imagen = 'assets/img/product-serum.png'
WHERE tipo = 'corazon' AND url_imagen IS NULL;

-- Notas de fondo (base notes) → imagen body oil/amaderado
UPDATE nota_olfativa SET url_imagen = 'assets/img/product-bodyoil.png'
WHERE tipo = 'fondo' AND url_imagen IS NULL;
