package com.jiubuntu.wms.biz.inbound.application;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundActionCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundLocationCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundLocationReplaceCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundHeaderResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundItemRow;
import com.jiubuntu.wms.biz.inbound.application.validator.InboundValidator;
import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import com.jiubuntu.wms.biz.inbound.domain.InboundItem;
import com.jiubuntu.wms.biz.inbound.domain.InboundItemLocation;
import com.jiubuntu.wms.biz.inbound.domain.InboundStatus;
import com.jiubuntu.wms.biz.inbound.infrastructure.InboundItemLocationRepository;
import com.jiubuntu.wms.biz.inbound.infrastructure.InboundItemRepository;
import com.jiubuntu.wms.biz.inbound.infrastructure.InboundRepository;
import com.jiubuntu.wms.biz.inventory.application.InventoryService;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.location.application.LocationService;
import com.jiubuntu.wms.biz.location.domain.Location;
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
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.PlatformTransactionManager;
import org.springframework.transaction.TransactionDefinition;
import org.springframework.transaction.TransactionStatus;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InboundServiceTest {

    @Mock
    private InboundRepository inboundRepository;

    @Mock
    private InboundItemRepository inboundItemRepository;

    @Mock
    private InboundItemLocationRepository inboundItemLocationRepository;

    @Mock
    private InboundValidator inboundValidator;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private LocationService locationService;

    @Mock
    private ProductService productService;

    @Mock
    private ProductUnitService productUnitService;

    @Mock
    private PlatformTransactionManager transactionManager;

    @InjectMocks
    private InboundService inboundService;

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

    private void stubTransactionManager() {
        TransactionStatus status = mock(TransactionStatus.class);
        when(transactionManager.getTransaction(any(TransactionDefinition.class))).thenReturn(status);
    }

    private void stubDetailLookup(Long inboundId, Warehouse warehouse) {
        when(inboundRepository.findHeaderResultById(inboundId)).thenReturn(Optional.of(
                new InboundHeaderResult(inboundId, warehouse.getId(), "공급업체", InboundStatus.PENDING, null, null, null)));
        when(inboundItemRepository.findActiveRowsByInboundId(inboundId)).thenReturn(List.of());
        when(inboundItemLocationRepository.findActiveRowsByInboundItemIdIn(List.of())).thenReturn(List.of());
    }

    @Test
    @DisplayName("확정 시 여러 위치에 걸친 재고 반영은 location_id 오름차순으로 처리한다")
    void complete_success_appliesInLocationIdOrder() {
        stubTransactionManager();
        Warehouse warehouse = warehouseWithId(100L);
        ProductUnit unit = unitWithId(1L);
        Product product = productWithId(5L, unit);

        Inbound inbound = new Inbound(warehouse.getCompany(), warehouse, "공급업체", null);
        ReflectionTestUtils.setField(inbound, "id", 500L);
        ReflectionTestUtils.setField(inbound, "status", InboundStatus.IN_PROGRESS);

        InboundItem item = new InboundItem(inbound, product, unit, 30, null, null, null);
        ReflectionTestUtils.setField(item, "id", 700L);

        Location higherLocation = locationWithId(20L);
        Location lowerLocation = locationWithId(10L);
        InboundItemLocation placementAtHigher = new InboundItemLocation(item, higherLocation, 20);
        InboundItemLocation placementAtLower = new InboundItemLocation(item, lowerLocation, 10);

        when(inboundRepository.findActiveById(500L)).thenReturn(Optional.of(inbound));
        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(inboundItemRepository.findByInboundIdAndActiveTrue(500L)).thenReturn(List.of(item));
        when(inboundItemLocationRepository.findByInboundItemIdAndActiveTrue(700L))
                .thenReturn(List.of(placementAtHigher, placementAtLower));
        when(inventoryService.applyOrderedQuantityChange(any(), any(), any(), any(), any(), anyInt()))
                .thenReturn(mock(Inventory.class));
        stubDetailLookup(500L, warehouse);

        InboundActionCommand command = InboundActionCommand.builder()
                .inboundId(500L).warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN)
                .principalWarehouseId(null).actorId(999L)
                .build();

        inboundService.complete(command);

        InOrder inOrder = inOrder(inventoryService);
        inOrder.verify(inventoryService).applyOrderedQuantityChange(eq(lowerLocation), eq(product), any(), any(), any(), eq(10));
        inOrder.verify(inventoryService).applyOrderedQuantityChange(eq(higherLocation), eq(product), any(), any(), any(), eq(20));
        verify(inventoryService, times(2)).recordHistory(any(), eq(warehouse), anyInt(), any(), eq(500L), any(), eq(999L));
    }

    @Test
    @DisplayName("배치가 끝나지 않은 상품 라인이 있으면 재고 반영 없이 예외가 발생한다")
    void complete_notFullyPlaced_throwsWithoutApplying() {
        stubTransactionManager();
        Warehouse warehouse = warehouseWithId(100L);
        ProductUnit unit = unitWithId(1L);
        Product product = productWithId(5L, unit);

        Inbound inbound = new Inbound(warehouse.getCompany(), warehouse, "공급업체", null);
        ReflectionTestUtils.setField(inbound, "id", 500L);
        ReflectionTestUtils.setField(inbound, "status", InboundStatus.IN_PROGRESS);

        InboundItem item = new InboundItem(inbound, product, unit, 30, null, null, null);
        ReflectionTestUtils.setField(item, "id", 700L);
        Location location = locationWithId(10L);
        InboundItemLocation partialPlacement = new InboundItemLocation(item, location, 10);

        when(inboundRepository.findActiveById(500L)).thenReturn(Optional.of(inbound));
        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(inboundItemRepository.findByInboundIdAndActiveTrue(500L)).thenReturn(List.of(item));
        when(inboundItemLocationRepository.findByInboundItemIdAndActiveTrue(700L)).thenReturn(List.of(partialPlacement));
        doThrow(new CommonException(ErrorCode.INBOUND_NOT_FULLY_PLACED))
                .when(inboundValidator).validateFullyPlaced(30, 10);

        InboundActionCommand command = InboundActionCommand.builder()
                .inboundId(500L).warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN)
                .principalWarehouseId(null).actorId(999L)
                .build();

        assertThatThrownBy(() -> inboundService.complete(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INBOUND_NOT_FULLY_PLACED);
        verify(inventoryService, never()).applyOrderedQuantityChange(any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("낙관적 락 충돌이 나면 재시도해서 결국 성공한다")
    void complete_optimisticLockConflict_retriesAndSucceeds() {
        stubTransactionManager();
        Warehouse warehouse = warehouseWithId(100L);
        ProductUnit unit = unitWithId(1L);
        Product product = productWithId(5L, unit);

        Inbound inbound = new Inbound(warehouse.getCompany(), warehouse, "공급업체", null);
        ReflectionTestUtils.setField(inbound, "id", 500L);
        ReflectionTestUtils.setField(inbound, "status", InboundStatus.IN_PROGRESS);

        InboundItem item = new InboundItem(inbound, product, unit, 10, null, null, null);
        ReflectionTestUtils.setField(item, "id", 700L);
        Location location = locationWithId(10L);
        InboundItemLocation placement = new InboundItemLocation(item, location, 10);

        when(inboundRepository.findActiveById(500L)).thenReturn(Optional.of(inbound));
        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(inboundItemRepository.findByInboundIdAndActiveTrue(500L)).thenReturn(List.of(item));
        when(inboundItemLocationRepository.findByInboundItemIdAndActiveTrue(700L)).thenReturn(List.of(placement));
        when(inventoryService.applyOrderedQuantityChange(any(), any(), any(), any(), any(), anyInt()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Inventory.class, 1L))
                .thenReturn(mock(Inventory.class));
        stubDetailLookup(500L, warehouse);

        InboundActionCommand command = InboundActionCommand.builder()
                .inboundId(500L).warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN)
                .principalWarehouseId(null).actorId(999L)
                .build();

        inboundService.complete(command);

        verify(inventoryService, times(2)).applyOrderedQuantityChange(any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("낙관적 락 충돌이 재시도 횟수를 초과하면 INBOUND_CONCURRENT_UPDATE_CONFLICT 예외가 발생한다")
    void complete_retryExhausted_throwsConflictError() {
        stubTransactionManager();
        Warehouse warehouse = warehouseWithId(100L);
        ProductUnit unit = unitWithId(1L);
        Product product = productWithId(5L, unit);

        Inbound inbound = new Inbound(warehouse.getCompany(), warehouse, "공급업체", null);
        ReflectionTestUtils.setField(inbound, "id", 500L);
        ReflectionTestUtils.setField(inbound, "status", InboundStatus.IN_PROGRESS);

        InboundItem item = new InboundItem(inbound, product, unit, 10, null, null, null);
        ReflectionTestUtils.setField(item, "id", 700L);
        Location location = locationWithId(10L);
        InboundItemLocation placement = new InboundItemLocation(item, location, 10);

        when(inboundRepository.findActiveById(500L)).thenReturn(Optional.of(inbound));
        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(inboundItemRepository.findByInboundIdAndActiveTrue(500L)).thenReturn(List.of(item));
        when(inboundItemLocationRepository.findByInboundItemIdAndActiveTrue(700L)).thenReturn(List.of(placement));
        when(inventoryService.applyOrderedQuantityChange(any(), any(), any(), any(), any(), anyInt()))
                .thenThrow(new ObjectOptimisticLockingFailureException(Inventory.class, 1L));

        InboundActionCommand command = InboundActionCommand.builder()
                .inboundId(500L).warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN)
                .principalWarehouseId(null).actorId(999L)
                .build();

        assertThatThrownBy(() -> inboundService.complete(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INBOUND_CONCURRENT_UPDATE_CONFLICT);
        verify(inventoryService, times(3)).applyOrderedQuantityChange(any(), any(), any(), any(), any(), anyInt());
    }

    @Test
    @DisplayName("PENDING 상태에서 위치 배치를 저장하면 IN_PROGRESS로 자동 전환된다")
    void replaceItemLocations_pendingAutoTransitionsToInProgress() {
        Warehouse warehouse = warehouseWithId(100L);
        ProductUnit unit = unitWithId(1L);
        Product product = productWithId(5L, unit);

        Inbound inbound = new Inbound(warehouse.getCompany(), warehouse, "공급업체", null);
        ReflectionTestUtils.setField(inbound, "id", 500L);
        assertThat(inbound.getStatus()).isEqualTo(InboundStatus.PENDING);

        InboundItem item = new InboundItem(inbound, product, unit, 30, null, null, null);
        ReflectionTestUtils.setField(item, "id", 700L);
        Location location = locationWithId(10L);

        when(inboundRepository.findActiveById(500L)).thenReturn(Optional.of(inbound));
        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(inboundItemRepository.findById(700L)).thenReturn(Optional.of(item));
        when(inboundItemLocationRepository.findByInboundItemIdAndActiveTrue(700L)).thenReturn(List.of());
        when(locationService.getActiveInWarehouse(10L, 100L)).thenReturn(location);
        stubDetailLookup(500L, warehouse);

        InboundLocationReplaceCommand command = InboundLocationReplaceCommand.builder()
                .inboundId(500L).itemId(700L).warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN)
                .principalWarehouseId(null)
                .locations(List.of(new InboundLocationCommand(10L, 30)))
                .actorId(999L)
                .build();

        inboundService.replaceItemLocations(command);

        assertThat(inbound.getStatus()).isEqualTo(InboundStatus.IN_PROGRESS);
    }

    @Test
    @DisplayName("취소하면 상태만 바뀌고 재고 반영은 발생하지 않는다")
    void cancel_success_doesNotTouchInventory() {
        Warehouse warehouse = warehouseWithId(100L);
        Inbound inbound = new Inbound(warehouse.getCompany(), warehouse, "공급업체", null);
        ReflectionTestUtils.setField(inbound, "id", 500L);
        ReflectionTestUtils.setField(inbound, "status", InboundStatus.PENDING);

        when(inboundRepository.findActiveById(500L)).thenReturn(Optional.of(inbound));
        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        stubDetailLookup(500L, warehouse);

        InboundActionCommand command = InboundActionCommand.builder()
                .inboundId(500L).warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN)
                .principalWarehouseId(null).actorId(999L)
                .build();

        inboundService.cancel(command);

        assertThat(inbound.getStatus()).isEqualTo(InboundStatus.CANCELLED);
        verify(inventoryService, never()).applyOrderedQuantityChange(any(), any(), any(), any(), any(), anyInt());
    }

}
