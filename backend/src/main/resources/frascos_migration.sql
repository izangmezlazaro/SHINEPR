-- Migration: insert visual bottle frascos if they don't already exist
INSERT INTO frasco (id_frasco, forma, capacidad_ml, material, precio)
VALUES
  (4,  'Impact',     50, 'Vidrio',  20.00),
  (5,  'Granade',    50, 'Vidrio',  18.00),
  (6,  'Diamond',    50, 'Cristal', 30.00),
  (7,  'Pearl',      50, 'Cristal', 22.00),
  (8,  'Gentlemen',  50, 'Vidrio',  15.00),
  (9,  'Champions',  50, 'Vidrio',  25.00),
  (10, 'Heel',       50, 'Cristal', 35.00),
  (11, 'Panther',    50, 'Vidrio',  28.00)
ON CONFLICT (id_frasco) DO NOTHING;
