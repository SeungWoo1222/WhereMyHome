CREATE TABLE apartment_complexes (
    id            BIGSERIAL PRIMARY KEY,
    region_id     BIGINT       NOT NULL REFERENCES regions (id),
    complex_name  VARCHAR(100) NOT NULL,  -- 아파트명
    dong_name     VARCHAR(50),            -- 법정동명
    built_year    SMALLINT,               -- 건축년도
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW()
);

CREATE INDEX idx_complexes_region ON apartment_complexes (region_id);
CREATE INDEX idx_complexes_name   ON apartment_complexes (complex_name);
