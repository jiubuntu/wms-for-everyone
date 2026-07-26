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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboundAllocationPlannerTest {

    @Mock
    private InventoryService inventoryService;

    @Mock
    private LocationService locationService;

    @Mock
    private OutboundValidator outboundValidator;

    @InjectMocks
    private OutboundAllocationPlanner outboundAllocationPlanner;

    private Location locationWithId(Long id) {
        Location location = Location.builder().zone("A").row("01").col("01").level("1").code("LOC-" + id).build();
        ReflectionTestUtils.setField(location, "id", id);
        return location;
    }

    private ProductUnit unitWithId(Long id) {
        ProductUnit unit = new ProductUnit(null, "박스");
        ReflectionTestUtils.setField(unit, "id", id);
        return unit;
    }

    private Product productWithId(Long id, ProductUnit baseUnit, ProductUnit subUnit, BigDecimal conversionRate) {
        Product product = new Product(null, "SKU-1", "테스트상품", null, null, baseUnit, subUnit, conversionRate, false, null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Inventory inventoryOf(Location location, Product product, String lotNumber, LocalDate expiryDate, int quantity) {
        return new Inventory(location, product, lotNumber, null, expiryDate, quantity);
    }

    @Test
    @DisplayName("FEFO는 유효기간이 임박한 재고부터 필요한 수량만큼 순서대로 소진한다")
    void plan_fefo_consumesEarliestExpiryFirst() {
        ProductUnit baseUnit = unitWithId(1L);
        Product product = productWithId(5L, baseUnit, null, null);
        Location earlyLocation = locationWithId(10L);
        Location lateLocation = locationWithId(20L);
        Inventory early = inventoryOf(earlyLocation, product, "LOT-EARLY", LocalDate.of(2026, 1, 1), 20);
        Inventory late = inventoryOf(lateLocation, product, "LOT-LATE", LocalDate.of(2026, 6, 1), 20);

        when(inventoryService.findAvailableForAllocation(100L, 5L)).thenReturn(List.of(early, late));

        List<OutboundAllocationPlan> plans = outboundAllocationPlanner.plan(
                100L, product, baseUnit, 30, AllocationType.FEFO, List.of());

        assertThat(plans).hasSize(2);
        assertThat(plans.get(0).getInventory()).isEqualTo(early);
        assertThat(plans.get(0).getQuantity()).isEqualTo(20);
        assertThat(plans.get(1).getInventory()).isEqualTo(late);
        assertThat(plans.get(1).getQuantity()).isEqualTo(10);
    }

    @Test
    @DisplayName("가용재고 합계가 요청 수량보다 적으면 INSUFFICIENT_AVAILABLE_QUANTITY 예외가 발생한다")
    void plan_fefo_insufficientQuantity_throws() {
        ProductUnit baseUnit = unitWithId(1L);
        Product product = productWithId(5L, baseUnit, null, null);
        Location location = locationWithId(10L);
        Inventory candidate = inventoryOf(location, product, null, null, 10);

        when(inventoryService.findAvailableForAllocation(100L, 5L)).thenReturn(List.of(candidate));

        assertThatThrownBy(() -> outboundAllocationPlanner.plan(100L, product, baseUnit, 30, AllocationType.FEFO, List.of()))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
    }

    @Test
    @DisplayName("MANUAL은 지정된 위치·수량 그대로 계획을 만들고 합계를 검증한다")
    void plan_manual_usesGivenAllocations() {
        ProductUnit baseUnit = unitWithId(1L);
        Product product = productWithId(5L, baseUnit, null, null);
        Location location = locationWithId(10L);
        Inventory inventory = inventoryOf(location, product, "LOT-1", null, 50);

        when(locationService.getActiveInWarehouse(10L, 100L)).thenReturn(location);
        when(inventoryService.getActiveByLocationProductLot(10L, 5L, "LOT-1")).thenReturn(inventory);

        List<OutboundAllocationCommand> allocations = List.of(new OutboundAllocationCommand(10L, "LOT-1", 15));
        List<OutboundAllocationPlan> plans = outboundAllocationPlanner.plan(
                100L, product, baseUnit, 15, AllocationType.MANUAL, allocations);

        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getInventory()).isEqualTo(inventory);
        assertThat(plans.get(0).getQuantity()).isEqualTo(15);
    }

    @Test
    @DisplayName("MANUAL 할당 합계가 요청 수량과 다르면 Validator가 던진 예외가 그대로 전파된다")
    void plan_manual_sumMismatch_throws() {
        ProductUnit baseUnit = unitWithId(1L);
        Product product = productWithId(5L, baseUnit, null, null);
        Location location = locationWithId(10L);
        Inventory inventory = inventoryOf(location, product, "LOT-1", null, 50);

        when(locationService.getActiveInWarehouse(10L, 100L)).thenReturn(location);
        when(inventoryService.getActiveByLocationProductLot(10L, 5L, "LOT-1")).thenReturn(inventory);
        doThrow(new CommonException(ErrorCode.OUTBOUND_ALLOCATION_MISMATCH))
                .when(outboundValidator).validateManualAllocationSum(15, 10);

        List<OutboundAllocationCommand> allocations = List.of(new OutboundAllocationCommand(10L, "LOT-1", 10));

        assertThatThrownBy(() -> outboundAllocationPlanner.plan(100L, product, baseUnit, 15, AllocationType.MANUAL, allocations))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUTBOUND_ALLOCATION_MISMATCH);
    }

    @Test
    @DisplayName("보조 단위로 요청하면 변환율만큼 기본 단위 수량으로 환산해 할당한다")
    void plan_subUnit_convertsToBaseQuantity() {
        ProductUnit baseUnit = unitWithId(1L);
        ProductUnit subUnit = unitWithId(2L);
        Product product = productWithId(5L, baseUnit, subUnit, BigDecimal.valueOf(24));
        Location location = locationWithId(10L);
        Inventory candidate = inventoryOf(location, product, null, null, 150);

        when(inventoryService.findAvailableForAllocation(100L, 5L)).thenReturn(List.of(candidate));

        // 보조 단위(subUnit) 5박스 x 변환율 24 = 기본 단위 120개
        List<OutboundAllocationPlan> plans = outboundAllocationPlanner.plan(
                100L, product, subUnit, 5, AllocationType.FEFO, List.of());

        assertThat(plans).hasSize(1);
        assertThat(plans.get(0).getQuantity()).isEqualTo(120);
    }

}
