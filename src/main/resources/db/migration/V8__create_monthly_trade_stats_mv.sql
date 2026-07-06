CREATE MATERIALIZED VIEW monthly_trade_stats AS
SELECT complex_id,
       TO_CHAR(DATE_TRUNC('month', trade_date), 'YYYY-MM') AS month,
       area,
       ROUND(AVG(price)) AS avg_price,
       MIN(price)        AS min_price,
       MAX(price)        AS max_price,
       COUNT(*)          AS cnt
FROM trade_records
GROUP BY complex_id, DATE_TRUNC('month', trade_date), area;

-- CONCURRENTLY 갱신을 위한 UNIQUE INDEX
CREATE UNIQUE INDEX idx_monthly_trade_stats_unique
    ON monthly_trade_stats (complex_id, month, area);

-- complex_id 조회용 인덱스
CREATE INDEX idx_monthly_trade_stats_complex_id
    ON monthly_trade_stats (complex_id);
