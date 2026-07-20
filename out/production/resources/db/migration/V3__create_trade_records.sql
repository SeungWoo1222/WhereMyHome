-- 아파트 실거래가 이력 (월별 파티셔닝)
CREATE TABLE trade_records (
    id            BIGSERIAL,
    complex_id    BIGINT       NOT NULL REFERENCES apartment_complexes (id),
    trade_date    DATE         NOT NULL,   -- 파티션 키
    area          NUMERIC(6,2) NOT NULL,   -- 전용면적 (㎡)
    floor         SMALLINT,               -- 층
    price         INT          NOT NULL,   -- 거래금액 (만원)
    created_at    TIMESTAMP    NOT NULL DEFAULT NOW(),
    PRIMARY KEY (id, trade_date)
) PARTITION BY RANGE (trade_date);

CREATE INDEX idx_trade_records_complex ON trade_records (complex_id);
CREATE INDEX idx_trade_records_date    ON trade_records (trade_date);
CREATE INDEX idx_trade_records_area    ON trade_records (complex_id, area);

-- 월별 파티션 (2006-01 ~ 2026-12)
CREATE TABLE trade_records_2006 PARTITION OF trade_records FOR VALUES FROM ('2006-01-01') TO ('2007-01-01');
CREATE TABLE trade_records_2007 PARTITION OF trade_records FOR VALUES FROM ('2007-01-01') TO ('2008-01-01');
CREATE TABLE trade_records_2008 PARTITION OF trade_records FOR VALUES FROM ('2008-01-01') TO ('2009-01-01');
CREATE TABLE trade_records_2009 PARTITION OF trade_records FOR VALUES FROM ('2009-01-01') TO ('2010-01-01');
CREATE TABLE trade_records_2010 PARTITION OF trade_records FOR VALUES FROM ('2010-01-01') TO ('2011-01-01');
CREATE TABLE trade_records_2011 PARTITION OF trade_records FOR VALUES FROM ('2011-01-01') TO ('2012-01-01');
CREATE TABLE trade_records_2012 PARTITION OF trade_records FOR VALUES FROM ('2012-01-01') TO ('2013-01-01');
CREATE TABLE trade_records_2013 PARTITION OF trade_records FOR VALUES FROM ('2013-01-01') TO ('2014-01-01');
CREATE TABLE trade_records_2014 PARTITION OF trade_records FOR VALUES FROM ('2014-01-01') TO ('2015-01-01');
CREATE TABLE trade_records_2015 PARTITION OF trade_records FOR VALUES FROM ('2015-01-01') TO ('2016-01-01');
CREATE TABLE trade_records_2016 PARTITION OF trade_records FOR VALUES FROM ('2016-01-01') TO ('2017-01-01');
CREATE TABLE trade_records_2017 PARTITION OF trade_records FOR VALUES FROM ('2017-01-01') TO ('2018-01-01');
CREATE TABLE trade_records_2018 PARTITION OF trade_records FOR VALUES FROM ('2018-01-01') TO ('2019-01-01');
CREATE TABLE trade_records_2019 PARTITION OF trade_records FOR VALUES FROM ('2019-01-01') TO ('2020-01-01');
CREATE TABLE trade_records_2020 PARTITION OF trade_records FOR VALUES FROM ('2020-01-01') TO ('2021-01-01');
CREATE TABLE trade_records_2021 PARTITION OF trade_records FOR VALUES FROM ('2021-01-01') TO ('2022-01-01');
CREATE TABLE trade_records_2022 PARTITION OF trade_records FOR VALUES FROM ('2022-01-01') TO ('2023-01-01');
CREATE TABLE trade_records_2023 PARTITION OF trade_records FOR VALUES FROM ('2023-01-01') TO ('2024-01-01');
CREATE TABLE trade_records_2024 PARTITION OF trade_records FOR VALUES FROM ('2024-01-01') TO ('2025-01-01');
CREATE TABLE trade_records_2025 PARTITION OF trade_records FOR VALUES FROM ('2025-01-01') TO ('2026-01-01');
CREATE TABLE trade_records_2026 PARTITION OF trade_records FOR VALUES FROM ('2026-01-01') TO ('2027-01-01');
