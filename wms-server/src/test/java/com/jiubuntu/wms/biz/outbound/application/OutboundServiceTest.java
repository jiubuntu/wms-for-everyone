package com.jiubuntu.wms.biz.outbound.application;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.inventory.application.InventoryService;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundActionCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundItemCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundRegisterCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundHeaderResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundItemRow;
import com.jiubuntu.wms.biz.outbound.application.validator.OutboundValidator;
import com.jiubuntu.wms.biz.outbound.domain.AllocationType;
import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.domain.OutboundItem;
import com.jiubuntu.wms.biz.outbound.domain.OutboundItemLocation;
import com.jiubuntu.wms.biz.outbound.domain.OutboundStatus;
import com.jiubuntu.wms.biz.outbound.infrastructure.OutboundItemLocationRepository;
import com.jiubuntu.wms.biz.outbound.infrastructure.OutboundItemRepository;
import com.jiubuntu.wms.biz.outbound.infrastructure.OutboundRepository;
import com.jiubuntu.wms.biz.product.application.ProductService;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.productunit.application.ProductUnitService;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class OutboundServiceTest {

    @Mock
    private OutboundRepository outboundRepository;

    @Mock
    private OutboundItemRepository outboundItemRepository;

    @Mock
    private OutboundItemLocationRepository outboundItemLocationRepository;

    @Mock
    private OutboundValidator outboundValidator;

    @Mock
    private OutboundAllocationPlanner outboundAllocationPlanner;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductUnitService productUnitService;

    @InjectMocks
    private OutboundService outboundService;

    private Warehouse warehouseWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", 1L);
        Warehouse warehouse = new Warehouse(company, "테스트창고", null, null);
        ReflectionTestUtils.setField(warehouse, "id", id);
        return warehouse;
    }

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

    private Product productWithId(Long id, ProductUnit baseUnit) {
        Product product = new Product(null, "SKU-1", "테스트상품", null, null, baseUnit, null, null, false, null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Inventory inventoryOf(Location location, Product product, String lotNumber, LocalDate expiryDate,
                                   int quantity, int reservedQuantity) {
        Inventory inventory = new Inventory(location, product, lotNumber, null, expiryDate, quantity);
        ReflectionTestUtils.setField(inventory, "reservedQuantity", reservedQuantity);
        return inventory;
    }

    private void stubDetailLookup(Long outboundId, Long itemId, Warehouse warehouse) {
        when(outboundRepository.findHeaderResultById(outboundId)).thenReturn(Optional.of(
                new OutboundHeaderResult(outboundId, warehouse.getId(), "고객사", OutboundStatus.PENDING, null, null, null)));
        when(outboundItemRepository.findActiveRowsByOutboundId(outboundId)).thenReturn(List.of(
                new OutboundItemRow(itemId, 5L, "SKU-1", "테스트상품", 1L, "박스", 30, AllocationType.FEFO)));
        when(outboundItemLocationRepository.findActiveRowsByOutboundItemIdIn(List.of(itemId))).thenReturn(List.of());
    }

    @Test
    @DisplayName("플래너가 계획을 어떤 순서로 반환하든, 실제 예약 반영은 location_id 오름차순으로 처리한다")
    void register_ordersReservationsByLocationIdRegardlessOfPlannerOrder() {
        Warehouse warehouse = warehouseWithId(100L);
        ProductUnit baseUnit = unitWithId(1L);
        Product product = productWithId(5L, baseUnit);

        Location higherLocation = locationWithId(20L);
        Location lowerLocation = locationWithId(10L);
        Inventory inventoryAtHigher = inventoryOf(higherLocation, product, "LOT-A", LocalDate.of(2026, 1, 1), 20, 0);
        Inventory inventoryAtLower = inventoryOf(lowerLocation, product, "LOT-B", LocalDate.of(2026, 6, 1), 20, 0);

        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(productService.getAccessible(5L, 1L)).thenReturn(product);
        when(productUnitService.getAccessible(1L, 1L)).thenReturn(baseUnit);
        when(outboundRepository.save(any(Outbound.class))).thenAnswer(invocation -> {
            Outbound saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 500L);
            return saved;
        });
        when(outboundItemRepository.save(any(OutboundItem.class))).thenAnswer(invocation -> {
            OutboundItem saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 700L);
            return saved;
        });
        // 플래너는 FEFO 소진 순서(higher 먼저)대로 반환하지만, 예약 반영은 location_id 순(lower 먼저)이어야 한다
        when(outboundAllocationPlanner.plan(eq(100L), eq(product), eq(baseUnit), eq(30), eq(AllocationType.FEFO), any()))
                .thenReturn(List.of(
                        new OutboundAllocationPlan(higherLocation, "LOT-A", inventoryAtHigher, 20),
                        new OutboundAllocationPlan(lowerLocation, "LOT-B", inventoryAtLower, 10)));
        when(inventoryService.reserve(any(Inventory.class), anyInt(), anyLong()))
                .thenAnswer(invocation -> invocation.getArgument(0));
        when(outboundItemLocationRepository.save(any(OutboundItemLocation.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));
        stubDetailLookup(500L, 700L, warehouse);

        OutboundRegisterCommand command = OutboundRegisterCommand.builder()
                .warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .customerName("고객사").note(null)
                .items(List.of(new OutboundItemCommand(5L, 1L, 30, AllocationType.FEFO, List.of())))
                .createdBy(999L)
                .build();

        outboundService.register(command);

        InOrder inOrder = inOrder(inventoryService);
        inOrder.verify(inventoryService).reserve(eq(inventoryAtLower), eq(10), eq(999L));
        inOrder.verify(inventoryService).reserve(eq(inventoryAtHigher), eq(20), eq(999L));
    }

    @Test
    @DisplayName("플래너가 예외를 던지면 예약이나 위치 저장 없이 그대로 전파된다")
    void register_plannerThrows_rollsBackWithoutReserving() {
        Warehouse warehouse = warehouseWithId(100L);
        ProductUnit baseUnit = unitWithId(1L);
        Product product = productWithId(5L, baseUnit);

        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(productService.getAccessible(5L, 1L)).thenReturn(product);
        when(productUnitService.getAccessible(1L, 1L)).thenReturn(baseUnit);
        when(outboundRepository.save(any(Outbound.class))).thenAnswer(invocation -> {
            Outbound saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 500L);
            return saved;
        });
        when(outboundItemRepository.save(any(OutboundItem.class))).thenAnswer(invocation -> {
            OutboundItem saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 700L);
            return saved;
        });
        when(outboundAllocationPlanner.plan(eq(100L), eq(product), eq(baseUnit), eq(30), eq(AllocationType.FEFO), any()))
                .thenThrow(new CommonException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY));

        OutboundRegisterCommand command = OutboundRegisterCommand.builder()
                .warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .customerName("고객사").note(null)
                .items(List.of(new OutboundItemCommand(5L, 1L, 30, AllocationType.FEFO, List.of())))
                .createdBy(999L)
                .build();

        assertThatThrownBy(() -> outboundService.register(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
        verify(inventoryService, never()).reserve(any(), anyInt(), any());
        verify(outboundItemLocationRepository, never()).save(any());
    }

    @Test
    @DisplayName("이미 확정된 출고를 다시 확정하려 하면 재고에 아무 영향 없이 예외가 발생한다")
    void complete_alreadyCompleted_throwsWithoutConfirmingReservation() {
        Warehouse warehouse = warehouseWithId(100L);
        Outbound outbound = new Outbound(warehouse.getCompany(), warehouse, "고객사", null);
        ReflectionTestUtils.setField(outbound, "id", 500L);
        ReflectionTestUtils.setField(outbound, "status", OutboundStatus.COMPLETED);

        when(outboundRepository.findActiveById(500L)).thenReturn(Optional.of(outbound));
        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        doThrow(new CommonException(ErrorCode.OUTBOUND_ALREADY_COMPLETED))
                .when(outboundValidator).validateComplete(outbound);

        OutboundActionCommand command = OutboundActionCommand.builder()
                .outboundId(500L).warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN)
                .principalWarehouseId(null).actorId(999L)
                .build();

        assertThatThrownBy(() -> outboundService.complete(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.OUTBOUND_ALREADY_COMPLETED);
        verify(inventoryService, never()).confirmReservation(any(), anyInt(), any(), any(), any(), any());
        verify(outboundItemRepository, never()).findByOutboundIdAndActiveTrue(any());
    }

    @Test
    @DisplayName("취소하면 할당된 재고 row를 location_id 오름차순으로 예약 해제한다")
    void cancel_success_releasesInLocationIdAscendingOrder() {
        Warehouse warehouse = warehouseWithId(100L);
        Outbound outbound = new Outbound(warehouse.getCompany(), warehouse, "고객사", null);
        ReflectionTestUtils.setField(outbound, "id", 500L);
        ReflectionTestUtils.setField(outbound, "status", OutboundStatus.PENDING);

        ProductUnit baseUnit = unitWithId(1L);
        Product product = productWithId(5L, baseUnit);
        OutboundItem item = new OutboundItem(outbound, product, baseUnit, 30, AllocationType.MANUAL);
        ReflectionTestUtils.setField(item, "id", 700L);

        Location higherLocation = locationWithId(20L);
        Location lowerLocation = locationWithId(10L);
        OutboundItemLocation allocationAtHigher = new OutboundItemLocation(item, higherLocation, null, 20);
        OutboundItemLocation allocationAtLower = new OutboundItemLocation(item, lowerLocation, null, 10);

        Inventory inventoryAtHigher = inventoryOf(higherLocation, product, null, null, 50, 20);
        Inventory inventoryAtLower = inventoryOf(lowerLocation, product, null, null, 50, 10);

        when(outboundRepository.findActiveById(500L)).thenReturn(Optional.of(outbound));
        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(outboundItemRepository.findByOutboundIdAndActiveTrue(500L)).thenReturn(List.of(item));
        when(outboundItemLocationRepository.findByOutboundItemIdInAndActiveTrue(List.of(700L)))
                .thenReturn(List.of(allocationAtHigher, allocationAtLower));
        when(inventoryService.getActiveByLocationProductLot(20L, 5L, null)).thenReturn(inventoryAtHigher);
        when(inventoryService.getActiveByLocationProductLot(10L, 5L, null)).thenReturn(inventoryAtLower);
        stubDetailLookup(500L, 700L, warehouse);

        OutboundActionCommand command = OutboundActionCommand.builder()
                .outboundId(500L).warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN)
                .principalWarehouseId(null).actorId(999L)
                .build();

        outboundService.cancel(command);

        InOrder inOrder = inOrder(inventoryService);
        inOrder.verify(inventoryService).releaseReservation(inventoryAtLower, 10, 999L);
        inOrder.verify(inventoryService).releaseReservation(inventoryAtHigher, 20, 999L);
    }

}
