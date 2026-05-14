-- Step 3: Add new constraint that includes 'procesando'
ALTER TABLE pedido ADD CONSTRAINT pedido_estado_check
  CHECK (estado IN ('pendiente', 'procesando', 'enviado', 'entregado', 'cancelado'));
