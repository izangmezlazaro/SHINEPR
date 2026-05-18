-- ══════════════════════════════════════════════════════════════════════════
-- Migración: añadir estados 'PENDIENTE_BIZUM' y 'PAGADO' al pedido
-- Necesario para el flujo del webhook de bunq.
-- Ejecutar UNA sola vez en producción / desarrollo.
-- ══════════════════════════════════════════════════════════════════════════

-- 1. Eliminar el CHECK constraint antiguo
ALTER TABLE pedido DROP CONSTRAINT IF EXISTS pedido_estado_check;

-- 2. Añadir el nuevo CHECK con todos los estados válidos
ALTER TABLE pedido ADD CONSTRAINT pedido_estado_check
  CHECK (estado IN (
    'pendiente',
    'PENDIENTE_BIZUM',   -- Pedido creado, esperando confirmación de bunq
    'PAGADO',            -- Confirmado por webhook de bunq
    'procesando',
    'enviado',
    'entregado',
    'cancelado'
  ));

-- 3. (Opcional) Actualizar pedidos existentes que vengan de bizum
-- UPDATE pedido SET estado = 'PENDIENTE_BIZUM' WHERE estado = 'pendiente' AND id_pedido IN (
--   SELECT id_pedido FROM pago WHERE metodo_pago = 'bizum' AND estado = 'pendiente'
-- );
