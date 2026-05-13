-- Migration: add loyalty points column to usuario table
ALTER TABLE usuario ADD COLUMN IF NOT EXISTS puntos INTEGER NOT NULL DEFAULT 0;
