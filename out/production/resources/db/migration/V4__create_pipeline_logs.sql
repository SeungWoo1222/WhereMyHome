CREATE TABLE pipeline_logs (
    id            BIGSERIAL PRIMARY KEY,
    job_name      VARCHAR(50)  NOT NULL,
    sigungu_code  VARCHAR(5)   NOT NULL,  -- 수집한 시군구 코드
    trade_year    SMALLINT     NOT NULL,
    trade_month   SMALLINT     NOT NULL,
    status        VARCHAR(10)  NOT NULL,  -- SUCCESS | FAILED
    record_count  INT,
    error_message TEXT,
    started_at    TIMESTAMP    NOT NULL,
    finished_at   TIMESTAMP
);

CREATE INDEX idx_pipeline_logs_sigungu ON pipeline_logs (sigungu_code, trade_year, trade_month);
