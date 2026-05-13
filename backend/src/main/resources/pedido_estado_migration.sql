-- Ampliar el CHECK constraint de pedido.estado para incluir 'procesando'
ALTER TABLE pedido DROP CONSTRAINT IF EXISTS pedido_estado_check;
ALTER TABLE pedido ADD CONSTRAINT pedido_estado_check
  CHECK (estado IN ('pendiente', 'procesando', 'enviado', 'entregado', 'cancelado'));
