ALTER TABLE clients ADD COLUMN IF NOT EXISTS self_responsible BOOLEAN;

UPDATE clients
SET self_responsible = TRUE
WHERE self_responsible IS NULL;
