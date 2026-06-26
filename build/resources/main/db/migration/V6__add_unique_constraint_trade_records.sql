-- 1단계: 중복 데이터 제거 (같은 단지+날짜+면적+층+가격 중 id가 큰 것 삭제)
-- 파티션별로 실행해야 성능이 좋음
DO $$
DECLARE
    y INT;
BEGIN
    FOR y IN 2006..2026 LOOP
        EXECUTE format(
            'DELETE FROM trade_records_%s a USING trade_records_%s b
             WHERE a.complex_id = b.complex_id
               AND a.trade_date = b.trade_date
               AND a.area = b.area
               AND a.floor IS NOT DISTINCT FROM b.floor
               AND a.price = b.price
               AND a.id > b.id', y, y
        );
        RAISE NOTICE 'Deduped partition %', y;
    END LOOP;
END $$;

-- 2단계: UNIQUE 제약 추가 (중복 제거 후이므로 성공)
ALTER TABLE trade_records
ADD CONSTRAINT uq_trade_record UNIQUE (complex_id, trade_date, area, floor, price);
