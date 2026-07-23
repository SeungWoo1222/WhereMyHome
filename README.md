# 내 집은 어디에 (WhereMyHome)

전국 아파트 실거래가 1,055만 건을 수집·최적화해 단지·지역 단위로 조회하는 개인 백엔드 프로젝트입니다.
"기능 구현"에서 멈추지 않고, 부하 테스트로 병목을 실측한 뒤 인덱스·캐시·Materialized View로 개선하고,
그 전후 수치를 직접 측정해 근거로 남겼습니다.

🔗 **[프로젝트 링크](https://wheremyhome-production.up.railway.app)** ・ 📄 **[기술 블로그(velog)](https://velog.io/@ilginam24/%EC%95%84%ED%8C%8C%ED%8A%B8-%EC%8B%A4%EA%B1%B0%EB%9E%98-%EB%A7%A4%EB%A7%A4%EA%B0%80-%EC%A1%B0%ED%9A%8C-%EC%84%9C%EB%B9%84%EC%8A%A4-%EC%BF%BC%EB%A6%AC-%EC%B5%9C%EC%A0%81%ED%99%94)** ・ 📝 [포트폴리오](#)

---

## 소개

| | |
|---|---|
| 데이터 | 전국 실거래가 **1,055만 건** (2006~2026, 20년치) |
| 단지 | **43,360개** 아파트 단지 |
| 지역 | **249개** 시군구 |
| 배포 | Railway (Docker, Spring Boot + React 통합 서빙) |

국토교통부 공공데이터 API에서 매월 실거래가를 자동 수집하고, 대용량 데이터에서도 빠르게 검색·조회할 수 있도록
DB 레벨 최적화(인덱스, 파티셔닝, Materialized View, 캐시)를 적용했습니다.

## 핵심 기능
- **단지·지역 통합검색** — 단지명 또는 지역명(시도/시군구)으로 검색
- **지역별 조회** — 시도/시군구별 아파트 목록 탐색
- **단지 상세 & 시세 추이 차트** — 기간(6개월~전체)·면적별 월별 평균/최고/최저가
- **시세 비교** — 두 단지의 가격 흐름을 한 차트에서 비교

## 기술적 하이라이트

DB 최적화 과정을 실측치와 함께 정리했습니다. 자세한 배경·트레이드오프는 기술 블로그를 참고 해주시기 바랍니다.

| 항목 | Before | After | 개선 | 측정 방법 |
|---|---|---|---|---|
| 단지명 검색 — DB 실행시간 | Seq Scan, 13.183ms | Trigram + GIN 인덱스, 2.088ms | **84.2%** | EXPLAIN ANALYZE |
| 단지명 검색 — HTTP 응답(p95) | 29.38ms | 13.58ms | **53.7%** | k6 부하테스트 |
| 월별 시세 집계 | 파티션 실시간 집계, 3.344ms | Materialized View, 0.373ms | **88.9%** | EXPLAIN ANALYZE |
| 지역 목록 조회 | DB 직접 조회, 7.13ms | Caffeine 로컬 캐시, 1.71ms | **76%** (Redis는 24%에 그쳐 폐기) | k6 부하테스트 |

## 아키텍처

<img width="1415" height="434" alt="image" src="https://github.com/user-attachments/assets/8ef89e24-fedd-49ac-b1ae-e88652efbd02" />


- `domain` — JPA 엔티티 (ApartmentComplex, TradeRecord(연도별 파티셔닝), Region, PipelineLog)
- `repository` — Spring Data JPA + JdbcTemplate(대량 처리·집계)
- `service` / `api` — 비즈니스 로직 / REST 컨트롤러
- `batch` — Chunk 기반 ETL (Reader → Processor → Writer)
- `infra/molit` — 국토부 공공데이터 API 연동
- `global` — 캐시·CORS·예외 처리 공통 설정

## 기술 스택

| 구분 | 기술 |
|---|---|
| 백엔드 | Java 21, Spring Boot, Spring Data JPA, Spring Batch, JdbcTemplate |
| 데이터 | PostgreSQL 16 (파티셔닝 · Trigram + GIN · Materialized View), Caffeine |
| 프론트엔드 | React, TypeScript |
| 인프라 | Docker, Railway |
| 측정 | k6, EXPLAIN ANALYZE |

## 주요 API

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/api/regions` | 전국 시도/시군구 목록 (캐시 적용) |
| GET | `/api/apartments?name=&regionId=&sort=` | 단지명·지역 통합검색 (페이지네이션) |
| GET | `/api/apartments/{id}` | 단지 상세 정보 |
| GET | `/api/apartments/{id}/trades` | 거래 이력 목록 |
| GET | `/api/apartments/{id}/trades/monthly?all=&area=` | 월별 시세 집계 (차트용) |
| POST | `/api/batch/trade-collect/chunk?year=&month=` | 특정 연월 실거래 수집 배치 실행 |

## 트러블슈팅

배치 재시작 문제, 공공 API 연동 이슈 → 구조 개선 방식으로 해결했습니다.
자세한 내용은 [포트폴리오](#)를 참고 해주시기 바랍니다.
