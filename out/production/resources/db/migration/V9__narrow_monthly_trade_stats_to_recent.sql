-- MV를 최근 3년 거래만 집계하도록 좁힘.
-- 이유: 차트는 최근 3년만 사용하는데 전체(20년)를 집계하면
--       REFRESH 시 대량 정렬 임시공간이 필요해 소용량 볼륨(5GB)에서 실패함.
--       데이터(trade_records) 자체는 전량 유지하되 집계 범위만 축소.
DROP MATERIALIZED VIEW IF EXISTS monthly_trade_stats;

CREATE MATERIALIZED VIEW monthly_trade_stats AS
SELECT complex_id,
       TO_CHAR(DATE_TRUNC('month', trade_date), 'YYYY-MM') AS month,
       area,
       ROUND(AVG(price)) AS avg_price,
       MIN(price)        AS min_price,
       MAX(price)        AS max_price,
       COUNT(*)          AS cnt
FROM trade_records
WHERE trade_date >= (CURRENT_DATE - INTERVAL '3 years')
GROUP BY complex_id, DATE_TRUNC('month', trade_date), area;

-- CONCURRENTLY 갱신을 위한 UNIQUE INDEX
CREATE UNIQUE INDEX idx_monthly_trade_stats_unique
    ON monthly_trade_stats (complex_id, month, area);

-- complex_id 조회용 인덱스
CREATE INDEX idx_monthly_trade_stats_complex_id
    ON monthly_trade_stats (complex_id);
