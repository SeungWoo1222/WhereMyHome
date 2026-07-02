import http from 'k6/http';
import { sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
    stages: [
        { duration: '10s', target: 10 },   // 10초 동안 10명으로 올리기
        { duration: '30s', target: 50 },   // 30초 동안 50명 유지
        { duration: '10s', target: 0 },    // 10초 동안 내리기
    ],
    thresholds: {
        http_req_duration: ['p(95)<2000'], // 95%가 2초 안에 응답해야 함
    },
};

export default function () {
    // 1. 아파트 검색 (LIKE 쿼리 — 가장 느릴 가능성 높음)
    http.get(`${BASE_URL}/api/apartments?name=래미안`);
    sleep(0.5);

    // 2. 시세 차트 데이터
    http.get(`${BASE_URL}/api/apartments/243/trades`);
    sleep(0.5);

    // 3. 지역 목록
    http.get(`${BASE_URL}/api/regions`);
    sleep(0.5);
}
