import http from 'k6/http';
import { check } from 'k6';
import { BASE_URL, authHeaders } from './api.js';

/**
 * 입고 등록→로케이션 배치→확정 흐름을 그대로 호출해서 각 상품마다 여러 로케이션에 나눠 재고를 쌓아둔다.
 * 같은 상품이라도 재고 row(location+product+lot)를 여러 개로 분산시켜두면, 부하테스트 중 동시 출고 요청이
 * FEFO로 서로 다른 row에 할당될 확률이 높아져 낙관적 락 충돌을 의도적으로 유도하지 않을 수 있다.
 */
export function seedInventory(token, warehouseId, products, locations, quantityPerLocation, locationsPerProduct) {
    const spread = Math.min(locationsPerProduct, locations.length);

    products.forEach((product, productIndex) => {
        const idemBase = `k6-seed-${warehouseId}-${product.id}-${Date.now()}-${productIndex}`;
        const totalQuantity = quantityPerLocation * spread;

        const createRes = http.post(
            `${BASE_URL}/api/warehouse/${warehouseId}/inbound/create`,
            JSON.stringify({
                supplierName: 'k6-seed-supplier',
                note: 'k6 부하테스트 재고 시딩',
                items: [{ productId: product.id, unitId: product.baseUnitId, quantity: totalQuantity }],
            }),
            { headers: authHeaders(token, `${idemBase}-create`) },
        );
        check(createRes, { '시딩 입고 등록 성공': (r) => r.status === 201 });

        const inboundId = createRes.json('data.id');
        const itemId = createRes.json('data.items.0.id');

        const placedLocations = [];
        for (let i = 0; i < spread; i++) {
            placedLocations.push({
                locationId: locations[(productIndex + i) % locations.length].id,
                quantity: quantityPerLocation,
            });
        }

        const locateRes = http.post(
            `${BASE_URL}/api/warehouse/${warehouseId}/inbound/${inboundId}/items/${itemId}/locations`,
            JSON.stringify({ locations: placedLocations }),
            { headers: authHeaders(token) },
        );
        check(locateRes, { '시딩 로케이션 배치 성공': (r) => r.status === 200 });

        const completeRes = http.post(
            `${BASE_URL}/api/warehouse/${warehouseId}/inbound/${inboundId}/complete`,
            null,
            { headers: authHeaders(token, `${idemBase}-complete`) },
        );
        check(completeRes, { '시딩 입고 확정 성공': (r) => r.status === 200 });
    });
}
