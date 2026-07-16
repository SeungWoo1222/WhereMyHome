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
        'http_req_duration{endpoint:trades}': ['p(95)<3000'],
    },
};

export default function () {
    http.get(`${BASE_URL}/api/apartments/9278/trades`, {
        tags: { endpoint: 'trades' },
    });
    sleep(0.5);
}
