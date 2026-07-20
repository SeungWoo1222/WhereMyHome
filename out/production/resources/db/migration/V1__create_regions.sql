-- 법정동 코드 기반 지역 테이블
CREATE TABLE regions (
    id            BIGSERIAL PRIMARY KEY,
    sido_name     VARCHAR(20)  NOT NULL,  -- 시도 (서울특별시)
    sigungu_name  VARCHAR(20)  NOT NULL,  -- 시군구 (강남구)
    sigungu_code  VARCHAR(5)   NOT NULL UNIQUE  -- 국토부 시군구 코드
);

CREATE INDEX idx_regions_sido ON regions (sido_name);
