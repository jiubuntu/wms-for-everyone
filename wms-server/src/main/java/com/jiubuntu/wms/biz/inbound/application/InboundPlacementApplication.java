package com.jiubuntu.wms.biz.inbound.application;

import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.product.domain.Product;
import lombok.Getter;

import java.time.LocalDate;

/**
 * 입고 확정 시 실제로 재고에 반영할 한 건 — 위치+상품+LOT+기본 단위로 환산된 수량.
 * 여러 상품 라인·여러 위치에 걸친 반영을 한 리스트로 모아 location_id 오름차순으로 정렬한 뒤 순서대로 적용한다.
 */
@Getter
public class InboundPlacementApplication {

    private final Location location;
    private final Product product;
    private final String lotNumber;
    private final LocalDate manufactureDate;
    private final LocalDate expiryDate;
    private final int baseQuantity;

    public InboundPlacementApplication(Location location, Product product, String lotNumber,
                                        LocalDate manufactureDate, LocalDate expiryDate, int baseQuantity) {
        this.location = location;
        this.product = product;
        this.lotNumber = lotNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.baseQuantity = baseQuantity;
    }

}
