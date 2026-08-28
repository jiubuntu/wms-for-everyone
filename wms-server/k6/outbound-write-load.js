import http from 'k6/http';
import { check, sleep } from 'k6';
import { BASE_URL, login, authHeaders } from './lib/api.js';
import { seedInventory } from './lib/seed.js';

// 부하테스트 중 재고가 소진되지 않도록 상품당 로케이션 5곳에 각 5000개씩(총 25000개) 시딩
const SEED_QUANTITY_PER_LOCATION = 5000;
const SEED_LOCATIONS_PER_PRODUCT = 5;
const MAX_PRODUCTS = Number(__ENV.MAX_PRODUCTS || 20);
const ITEMS_PER_ORDER = Number(__ENV.ITEMS_PER_ORDER || 3);

export const options = {
    scenarios: {
        outbound_write: {
            executor: 'ramping-vus',
            startVUs: 0,
            // 붕괴 지점을 찾는 게 아니라, 현실적인 동시 사용자 구간(10/20/30)에서 안정적인지 확인하는 목적이라
            // 각 단계를 길게(1분) 유지해 평균값을 통계적으로 안정시킨다.
            stages: [
                { duration: '20s', target: 10 },
                { duration: '1m', target: 10 },
                { duration: '20s', target: 20 },
                { duration: '1m', target: 20 },
                { duration: '20s', target: 30 },
                { duration: '1m', target: 30 },
                { duration: '20s', target: 0 },
            ],
            exec: 'outboundWrite',
        },
    },
    setupTimeout: '120s',
    thresholds: {
        http_req_failed: ['rate<0.01'],
        http_req_duration: ['p(95)<1000'],
    },
};

export function setup() {
    const token = login();

    const warehouseRes = http.get(`${BASE_URL}/api/warehouse/all`, { headers: authHeaders(token) });
    check(warehouseRes, { '창고 목록 조회 성공': (r) => r.status === 200 });
    const warehouseId = warehouseRes.json('data.0.id');

    const productRes = http.get(`${BASE_URL}/api/product/all`, { headers: authHeaders(token) });
    check(productRes, { '상품 목록 조회 성공': (r) => r.status === 200 });
    // lot_tracking 상품은 입고 시 lotNumber/제조일/유효기한이 필수인데 시딩 로직이 이 정보를 안 보내서
    // 항상 재고 시딩이 실패한다 — 재고 부족(비즈니스 실패)이 커넥션 풀 문제와 섞이지 않도록 아예 제외한다.
    const products = productRes.json('data').filter((product) => !product.lotTracking).slice(0, MAX_PRODUCTS);

    const locationRes = http.get(`${BASE_URL}/api/warehouse/${warehouseId}/location/all`, { headers: authHeaders(token) });
    check(locationRes, { '로케이션 목록 조회 성공': (r) => r.status === 200 });
    const locations = locationRes.json('data');

    if (products.length === 0 || locations.length === 0) {
        throw new Error('상품 또는 로케이션이 없습니다. 화면에서 기초 데이터를 먼저 등록하세요.');
    }

    seedInventory(token, warehouseId, products, locations, SEED_QUANTITY_PER_LOCATION, SEED_LOCATIONS_PER_PRODUCT);

    return { token, warehouseId, products };
}

export function outboundWrite(data) {
    const { token, warehouseId, products } = data;
    const itemsCount = Math.min(ITEMS_PER_ORDER, products.length);
    const startIndex = (__VU + __ITER) % products.length;
    const items = [];
    for (let i = 0; i < itemsCount; i++) {
        const product = products[(startIndex + i) % products.length];
        items.push({
            productId: product.id,
            unitId: product.baseUnitId,
            quantity: 1,
            allocationType: 'FEFO',
            allocations: [],
        });
    }
    const idemBase = `k6-outbound-${__VU}-${__ITER}-${Date.now()}`;

    const createRes = http.post(
        `${BASE_URL}/api/warehouse/${warehouseId}/outbound/create`,
        JSON.stringify({
            customerName: `k6-load-vu${__VU}`,
            note: 'k6 부하테스트',
            items,
        }),
        { headers: authHeaders(token, `${idemBase}-create`) },
    );
    const createOk = check(createRes, { '출고 등록 성공': (r) => r.status === 201 });
    if (!createOk) {
        sleep(0.2);
        return;
    }

    const outboundId = createRes.json('data.id');
    const completeRes = http.post(
        `${BASE_URL}/api/warehouse/${warehouseId}/outbound/${outboundId}/complete`,
        null,
        { headers: authHeaders(token, `${idemBase}-complete`) },
    );
    check(completeRes, { '출고 확정 성공': (r) => r.status === 200 });

    sleep(1);
}
