ALTER TABLE pedido DROP CONSTRAINT IF EXISTS pedido_estado_check;

ALTER TABLE pedido ADD CONSTRAINT pedido_estado_check
    CHECK (estado IN ('pendiente','pendiente_bizum','procesando','enviado','entregado','cancelado'));
