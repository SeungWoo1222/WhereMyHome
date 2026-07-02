CREATE EXTENSION IF NOT EXISTS pg_trgm;

CREATE INDEX idx_complexes_name_trgm
    ON apartment_complexes
    USING GIN (complex_name gin_trgm_ops);
