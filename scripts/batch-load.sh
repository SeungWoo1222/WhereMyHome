#!/bin/bash
# 실거래가 데이터 배치 적재 스크립트
# Usage: ./scripts/batch-load.sh [start_year] [end_year]

BASE_URL="http://localhost:8081/api/batch/trade-collect"
START_YEAR=${1:-2025}
END_YEAR=${2:-2022}

for year in $(seq $START_YEAR -1 $END_YEAR); do
  for month in $(seq 1 12); do
    # 미래 월은 건너뛰기
    if [ "$year" -eq 2025 ] && [ "$month" -gt 6 ]; then
      continue
    fi

    echo "$(date '+%H:%M:%S') Loading $year-$(printf '%02d' $month)..."
    result=$(curl -s -X POST "$BASE_URL?year=$year&month=$month")
    echo "  $result"
    sleep 1
  done
done

echo "$(date '+%H:%M:%S') All done!"
