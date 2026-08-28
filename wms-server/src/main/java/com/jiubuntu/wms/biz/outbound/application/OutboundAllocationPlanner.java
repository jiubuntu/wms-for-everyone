package com.jiubuntu.wms.biz.outbound.application;

import com.jiubuntu.wms.biz.inventory.application.InventoryService;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.location.application.LocationService;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundAllocationCommand;
import com.jiubuntu.wms.biz.outbound.application.validator.OutboundValidator;
import com.jiubuntu.wms.biz.outbound.domain.AllocationType;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;

/**
 * 출고 상품 라인 하나를 어느 위치·LOT에서 얼마나 뺄지 계산하는 책임만 담당한다 (FEFO 자동 할당 / MANUAL 수동 할당 + 단위 환산).
 * 실제 예약 반영(순서·트랜잭션)은 OutboundService가 맡는다.
 */
@Component
@RequiredArgsConstructor
public class OutboundAllocationPlanner {

    private final InventoryService inventoryService;
    private final LocationService locationService;
    private final OutboundValidator outboundValidator;

    /**
     * FEFO 후보(fefoCandidates)는 호출측(OutboundService)이 요청 전체 상품에 대해 미리 배치로 조회해
     * 전달한다 — item마다 이 메서드가 직접 재고를 조회하면 item 개수만큼 쿼리가 반복되기 때문.
     * MANUAL 할당은 로케이션 지정이 있어 배치화 대상이 아니라 그대로 둔다.
     */
    public List<OutboundAllocationPlan> plan(Long warehouseId, Product product, ProductUnit unit, int quantity,
                                              AllocationType allocationType, List<OutboundAllocationCommand> allocations,
                                              List<Inventory> fefoCandidates) {
        int baseQuantity = toBaseQuantity(product, unit, quantity);
        return allocationType == AllocationType.FEFO
                ? allocateFefo(fefoCandidates, baseQuantity)
                : allocateManual(warehouseId, product, allocations, baseQuantity);
    }

    private List<OutboundAllocationPlan> allocateFefo(List<Inventory> candidates, int requiredBaseQuantity) {
        List<OutboundAllocationPlan> plans = new ArrayList<>();
        int remaining = requiredBaseQuantity;
        for (Inventory candidate : candidates) {
            if (remaining <= 0) {
                break;
            }
            int take = Math.min(candidate.getAvailableQuantity(), remaining);
            plans.add(new OutboundAllocationPlan(candidate.getLocation(), candidate.getLotNumber(), candidate, take));
            remaining -= take;
        }
        if (remaining > 0) {
            throw new CommonException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
        }
        return plans;
    }

    private List<OutboundAllocationPlan> allocateManual(Long warehouseId, Product product,
                                                          List<OutboundAllocationCommand> allocationCommands, int requiredBaseQuantity) {
        List<OutboundAllocationPlan> plans = new ArrayList<>();
        int sum = 0;
        for (OutboundAllocationCommand allocationCommand : allocationCommands) {
            Location location = locationService.getActiveInWarehouse(allocationCommand.getLocationId(), warehouseId);
            Inventory inventory = inventoryService.getActiveByLocationProductLot(
                    location.getId(), product.getId(), allocationCommand.getLotNumber());
            plans.add(new OutboundAllocationPlan(location, allocationCommand.getLotNumber(), inventory, allocationCommand.getQuantity()));
            sum += allocationCommand.getQuantity();
        }
        outboundValidator.validateManualAllocationSum(requiredBaseQuantity, sum);
        return plans;
    }

    private int toBaseQuantity(Product product, ProductUnit unit, int quantity) {
        if (product.getSubUnit() != null && product.getSubUnit().getId().equals(unit.getId())) {
            return BigDecimal.valueOf(quantity)
                    .multiply(product.getUnitConversionRate())
                    .setScale(0, RoundingMode.HALF_UP)
                    .intValueExact();
        }
        return quantity;
    }

}
