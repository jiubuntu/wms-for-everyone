import http from 'k6/http';
import { check } from 'k6';

export const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';

const TEST_EMAIL = __ENV.TEST_EMAIL || 'company_admin@wms.com';
const TEST_PASSWORD = __ENV.TEST_PASSWORD || 'CompanyAdmin1234!';

export function login() {
    const res = http.post(
        `${BASE_URL}/api/auth/login`,
        JSON.stringify({ email: TEST_EMAIL, password: TEST_PASSWORD }),
        { headers: { 'Content-Type': 'application/json' } },
    );

    check(res, { '로그인 성공': (r) => r.status === 200 });
    return res.json('data.accessToken');
}

export function authHeaders(token, idempotencyKey) {
    const headers = {
        'Content-Type': 'application/json',
        Authorization: `Bearer ${token}`,
    };
    if (idempotencyKey) {
        headers['Idempotency-Key'] = idempotencyKey;
    }
    return headers;
}
