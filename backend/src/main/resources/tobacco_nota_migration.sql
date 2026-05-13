-- ============================================================
-- Migración: añadir fragancia "Tobacco" como nota de base
-- Ejecutar una vez contra la BD PostgreSQL
-- ============================================================

INSERT INTO fragancia (nombre, familia, es_base)
VALUES ('Tobacco', 'Oriental', true)
ON CONFLICT DO NOTHING;
