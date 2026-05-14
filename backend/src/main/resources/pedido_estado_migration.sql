-- Sanitize any existing rows with unexpected estado values before adding constraint
UPDATE pedido SET estado = 'pendiente' WHERE estado NOT IN ('pendiente', 'procesando', 'enviado', 'entregado', 'cancelado');
