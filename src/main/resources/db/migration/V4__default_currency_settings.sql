ALTER TABLE app_settings ADD COLUMN IF NOT EXISTS default_currency VARCHAR(3);

UPDATE app_settings
SET default_currency = 'ARS'
WHERE default_currency IS NULL OR TRIM(default_currency) = '';
