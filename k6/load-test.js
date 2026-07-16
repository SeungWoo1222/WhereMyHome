import http from 'k6/http';
import { sleep } from 'k6';

const BASE_URL = 'http://localhost:8080';

export const options = {
    stages: [
        { duration: '10s', target: 10 },
        { duration: '30s', target: 50 },
        { duration: '10s', target: 0 },
    ],
    thresholds: {
        'http_req_duration{endpoint:search}': ['p(95)<3000'],
        'http_req_duration{endpoint:trades}': ['p(95)<3000'],
        'http_req_duration{endpoint:regions}': ['p(95)<3000'],
    },
};

export default function () {
    http.get(`${BASE_URL}/api/apartments?name=${encodeURIComponent('현대')}`, {
        tags: { endpoint: 'search' },
    });
    sleep(0.5);

    http.get(`${BASE_URL}/api/apartments/243/trades`, {
        tags: { endpoint: 'trades' },
    });
    sleep(0.5);

    http.get(`${BASE_URL}/api/regions`, {
        tags: { endpoint: 'regions' },
    });
    sleep(0.5);
}
