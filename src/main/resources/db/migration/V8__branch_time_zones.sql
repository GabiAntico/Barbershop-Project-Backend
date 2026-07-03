ALTER TABLE branches
    ADD COLUMN time_zone VARCHAR(64) DEFAULT 'America/Argentina/Buenos_Aires' NOT NULL;
