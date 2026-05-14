-- Step 2: Drop old constraint and add new one with 'procesando' included
ALTER TABLE pedido DROP CONSTRAINT IF EXISTS pedido_estado_check;
