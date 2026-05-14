-- ══════════════════════════════════════════════════════════════
-- SHINE — Seed de subcategorías + asignación automática
-- Ejecutar en orden contra la BD PostgreSQL (AWS RDS)
-- ══════════════════════════════════════════════════════════════

-- ── PASO 1: Vaciar y reinsertar subcategorías ─────────────────
-- (usa ON CONFLICT para no duplicar si ya existen)

INSERT INTO subcategoria (nombre, id_categoria) VALUES
  -- Skincare (1)
  ('Essential Oils',        1),
  ('Face Care',             1),
  ('Body Care',             1),
  ('Aloe Vera Line',        1),
  -- Fragrance (2)
  ('Classic',               2),
  ('New Generation',        2),
  ('Limited Edition',       2),
  ('Special Edition',       2),
  -- Supplements (3)
  ('Vitamins & Health',     3),
  ('Colostrum Health',      3),
  ('Dental Care',           3),
  -- Gift Sets (4) → sin subcategorías
  -- Makeup (5)
  ('Face & Foundation',     5),
  ('Lips',                  5),
  ('Eyes',                  5),
  -- Accessories (6)
  ('Bags & Boxes',          6),
  ('Beauty Tools',          6),
  -- Hair Care (7)
  ('Shampoo & Conditioner', 7),
  ('Styling',               7)
ON CONFLICT DO NOTHING;

-- ── PASO 2: Ver los IDs asignados (necesarios para el paso 3) ─
SELECT id_subcategoria, nombre, id_categoria
FROM subcategoria
ORDER BY id_categoria, id_subcategoria;


-- ══════════════════════════════════════════════════════════════
-- PASO 3: Asignación automática por palabras clave
-- Ajusta los IDs con los que te salieron en el SELECT anterior
-- ══════════════════════════════════════════════════════════════

-- ── SKINCARE ─────────────────────────────────────────────────

-- Essential Oils
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Essential Oils' AND id_categoria = 1)
WHERE id_categoria = 1 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%oil%' OR nombre ILIKE '%aceite%' OR nombre ILIKE '%argan%'
    OR nombre ILIKE '%jojoba%' OR nombre ILIKE '%rosehip%' OR nombre ILIKE '%essential%'
    OR descripcion ILIKE '%essential oil%' OR descripcion ILIKE '%aceite esencial%');

-- Aloe Vera Line (antes que Face Care para no solapar)
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Aloe Vera Line' AND id_categoria = 1)
WHERE id_categoria = 1 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%aloe%' OR descripcion ILIKE '%aloe vera%');

-- Body Care
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Body Care' AND id_categoria = 1)
WHERE id_categoria = 1 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%body%' OR nombre ILIKE '%corporal%' OR nombre ILIKE '%lotion%'
    OR nombre ILIKE '%butter%' OR nombre ILIKE '%manteca%' OR nombre ILIKE '%scrub%'
    OR nombre ILIKE '%cuerpo%' OR descripcion ILIKE '%body care%' OR descripcion ILIKE '%cuidado corporal%');

-- Face Care (catch-all para el resto de Skincare)
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Face Care' AND id_categoria = 1)
WHERE id_categoria = 1 AND id_subcategoria IS NULL;

-- ── FRAGRANCE ─────────────────────────────────────────────────

-- Limited Edition → en BD: "Edicion Limitada" / "Edición Limitada"
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Limited Edition' AND id_categoria = 2)
WHERE id_categoria = 2 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%edicion limitada%' OR nombre ILIKE '%edición limitada%'
    OR nombre ILIKE '%limited%' OR nombre ILIKE '%limitad%' OR nombre ILIKE '%edition%'
    OR nombre ILIKE '%collector%' OR nombre ILIKE '%coleccion%');

-- Special Edition → en BD: "Especial"
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Special Edition' AND id_categoria = 2)
WHERE id_categoria = 2 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%especial%' OR nombre ILIKE '%special%' OR nombre ILIKE '%exclusive%'
    OR nombre ILIKE '%exclusiv%' OR nombre ILIKE '%premium%' OR nombre ILIKE '%vip%');

-- New Generation
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'New Generation' AND id_categoria = 2)
WHERE id_categoria = 2 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%new%' OR nombre ILIKE '%nuevo%' OR nombre ILIKE '%modern%'
    OR nombre ILIKE '%fresh%' OR nombre ILIKE '%fresco%' OR nombre ILIKE '%generation%'
    OR nombre ILIKE '%neo%' OR tipo_fragancia ILIKE '%edt%');

-- Classic → en BD: "Clasico" / "Clásico" + catch-all para el resto
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Classic' AND id_categoria = 2)
WHERE id_categoria = 2 AND id_subcategoria IS NULL;

-- ── SUPPLEMENTS ──────────────────────────────────────────────

-- Dental Care
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Dental Care' AND id_categoria = 3)
WHERE id_categoria = 3 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%dental%' OR nombre ILIKE '%teeth%' OR nombre ILIKE '%dientes%'
    OR nombre ILIKE '%oral%' OR nombre ILIKE '%boca%' OR nombre ILIKE '%blanqueo%'
    OR nombre ILIKE '%white%' OR descripcion ILIKE '%dental%');

-- Colostrum Health
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Colostrum Health' AND id_categoria = 3)
WHERE id_categoria = 3 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%colostrum%' OR nombre ILIKE '%calostro%' OR nombre ILIKE '%colostro%'
    OR descripcion ILIKE '%colostrum%' OR descripcion ILIKE '%calostro%');

-- Vitamins & Health (catch-all para el resto de Supplements)
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Vitamins & Health' AND id_categoria = 3)
WHERE id_categoria = 3 AND id_subcategoria IS NULL;

-- ── MAKEUP ───────────────────────────────────────────────────

-- Lips
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Lips' AND id_categoria = 5)
WHERE id_categoria = 5 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%lip%' OR nombre ILIKE '%labial%' OR nombre ILIKE '%lipstick%'
    OR nombre ILIKE '%gloss%' OR nombre ILIKE '%balm%' OR nombre ILIKE '%balsamo%');

-- Eyes
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Eyes' AND id_categoria = 5)
WHERE id_categoria = 5 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%eye%' OR nombre ILIKE '%ojo%' OR nombre ILIKE '%mascara%'
    OR nombre ILIKE '%eyeliner%' OR nombre ILIKE '%shadow%' OR nombre ILIKE '%sombra%'
    OR nombre ILIKE '%brow%' OR nombre ILIKE '%cejas%' OR nombre ILIKE '%lash%');

-- Face & Foundation (catch-all para el resto de Makeup)
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Face & Foundation' AND id_categoria = 5)
WHERE id_categoria = 5 AND id_subcategoria IS NULL;

-- ── ACCESSORIES ──────────────────────────────────────────────

-- Beauty Tools
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Beauty Tools' AND id_categoria = 6)
WHERE id_categoria = 6 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%brush%' OR nombre ILIKE '%brocha%' OR nombre ILIKE '%pincel%'
    OR nombre ILIKE '%tool%' OR nombre ILIKE '%roller%' OR nombre ILIKE '%gua sha%'
    OR nombre ILIKE '%mirror%' OR nombre ILIKE '%espejo%' OR nombre ILIKE '%tweezer%'
    OR nombre ILIKE '%pinza%' OR nombre ILIKE '%applicator%');

-- Bags & Boxes (catch-all para el resto de Accessories)
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Bags & Boxes' AND id_categoria = 6)
WHERE id_categoria = 6 AND id_subcategoria IS NULL;

-- ── HAIR CARE ────────────────────────────────────────────────

-- Shampoo & Conditioner
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Shampoo & Conditioner' AND id_categoria = 7)
WHERE id_categoria = 7 AND id_subcategoria IS NULL
  AND (nombre ILIKE '%shampoo%' OR nombre ILIKE '%champu%' OR nombre ILIKE '%champú%'
    OR nombre ILIKE '%conditioner%' OR nombre ILIKE '%acondicionador%');

-- Styling (catch-all para el resto de Hair Care)
UPDATE producto SET id_subcategoria = (SELECT id_subcategoria FROM subcategoria WHERE nombre = 'Styling' AND id_categoria = 7)
WHERE id_categoria = 7 AND id_subcategoria IS NULL;


-- ══════════════════════════════════════════════════════════════
-- VERIFICACIÓN FINAL
-- Muestra productos sin subcategoría asignada (deberían ser 0
-- salvo Gift Sets que no tienen subcategorías)
-- ══════════════════════════════════════════════════════════════
SELECT p.id_producto, p.nombre, c.nombre AS categoria
FROM producto p
JOIN categoria c ON p.id_categoria = c.id_categoria
WHERE p.id_subcategoria IS NULL
  AND p.id_categoria != 4  -- Gift Sets no tienen subcategoría
ORDER BY c.nombre, p.nombre;
