package com.jiubuntu.wms.biz.inventory.application;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.inventory.application.dto.command.TransferCreateCommand;
import com.jiubuntu.wms.biz.inventory.application.dto.result.TransferResult;
import com.jiubuntu.wms.biz.inventory.application.validator.InventoryValidator;
import com.jiubuntu.wms.biz.inventory.application.validator.TransferValidator;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.domain.Transfer;
import com.jiubuntu.wms.biz.inventory.infrastructure.TransferRepository;
import com.jiubuntu.wms.biz.location.application.LocationService;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.product.application.ProductService;
import com.jiubuntu.wms.biz.product.domain.Product;
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
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.inOrder;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class TransferServiceTest {

    @Mock
    private TransferRepository transferRepository;

    @Mock
    private TransferValidator transferValidator;

    @Mock
    private InventoryValidator inventoryValidator;

    @Mock
    private InventoryService inventoryService;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private LocationService locationService;

    @Mock
    private ProductService productService;

    @Mock
    private CommonCodeService commonCodeService;

    @InjectMocks
    private TransferService transferService;

    private Warehouse warehouseWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", 1L);
        Warehouse warehouse = new Warehouse(company, "테스트창고", null, null);
        ReflectionTestUtils.setField(warehouse, "id", id);
        return warehouse;
    }

    private Location locationWithId(Long id, Warehouse warehouse) {
        Location location = Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code("LOC-" + id)
                .build();
        ReflectionTestUtils.setField(location, "id", id);
        return location;
    }

    private Product productWithId(Long id) {
        Product product = new Product(null, "SKU-1", "테스트상품", null, null, null, null, null, false, null);
        ReflectionTestUtils.setField(product, "id", id);
        return product;
    }

    private Inventory inventoryOf(Location location, Product product, int quantity, int reservedQuantity) {
        Inventory inventory = new Inventory(location, product, null, null, null, quantity);
        ReflectionTestUtils.setField(inventory, "reservedQuantity", reservedQuantity);
        return inventory;
    }

    @Test
    @DisplayName("출발 위치의 id가 더 작으면 출발 위치를 먼저 갱신한다")
    void create_fromLocationLowerId_updatesFromFirst() {
        Warehouse warehouse = warehouseWithId(100L);
        Location fromLocation = locationWithId(1L, warehouse);
        Location toLocation = locationWithId(2L, warehouse);
        Product product = productWithId(5L);
        Inventory sourceInventory = inventoryOf(fromLocation, product, 100, 0);

        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(locationService.getActiveInWarehouse(1L, 100L)).thenReturn(fromLocation);
        when(locationService.getActiveInWarehouse(2L, 100L)).thenReturn(toLocation);
        when(productService.getAccessible(5L, 1L)).thenReturn(product);
        when(inventoryService.getActiveByLocationProductLot(1L, 5L, null)).thenReturn(sourceInventory);
        when(inventoryService.applyOrderedQuantityChange(
                eq(fromLocation), eq(product), isNull(), any(), any(), eq(-30)))
                .thenReturn(inventoryOf(fromLocation, product, 70, 0));
        when(inventoryService.applyOrderedQuantityChange(
                eq(toLocation), eq(product), isNull(), any(), any(), eq(30)))
                .thenReturn(inventoryOf(toLocation, product, 30, 0));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.findResultById(any())).thenReturn(Optional.of(TransferResult.builder().id(1L).build()));

        TransferCreateCommand command = TransferCreateCommand.builder()
                .warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .productId(5L).fromLocationId(1L).toLocationId(2L).lotNumber(null).quantity(30)
                .reasonId(null).note(null).createdBy(999L)
                .build();

        transferService.create(command);

        InOrder inOrder = inOrder(inventoryService);
        inOrder.verify(inventoryService).applyOrderedQuantityChange(
                eq(fromLocation), eq(product), isNull(), any(), any(), eq(-30));
        inOrder.verify(inventoryService).applyOrderedQuantityChange(
                eq(toLocation), eq(product), isNull(), any(), any(), eq(30));
    }

    @Test
    @DisplayName("도착 위치의 id가 더 작으면 도착 위치를 먼저 갱신한다 (데드락 회피용 정렬)")
    void create_toLocationLowerId_updatesToFirst() {
        Warehouse warehouse = warehouseWithId(100L);
        Location fromLocation = locationWithId(2L, warehouse);
        Location toLocation = locationWithId(1L, warehouse);
        Product product = productWithId(5L);
        Inventory sourceInventory = inventoryOf(fromLocation, product, 100, 0);

        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(locationService.getActiveInWarehouse(2L, 100L)).thenReturn(fromLocation);
        when(locationService.getActiveInWarehouse(1L, 100L)).thenReturn(toLocation);
        when(productService.getAccessible(5L, 1L)).thenReturn(product);
        when(inventoryService.getActiveByLocationProductLot(2L, 5L, null)).thenReturn(sourceInventory);
        when(inventoryService.applyOrderedQuantityChange(
                eq(toLocation), eq(product), isNull(), any(), any(), eq(30)))
                .thenReturn(inventoryOf(toLocation, product, 30, 0));
        when(inventoryService.applyOrderedQuantityChange(
                eq(fromLocation), eq(product), isNull(), any(), any(), eq(-30)))
                .thenReturn(inventoryOf(fromLocation, product, 70, 0));
        when(transferRepository.save(any(Transfer.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(transferRepository.findResultById(any())).thenReturn(Optional.of(TransferResult.builder().id(1L).build()));

        TransferCreateCommand command = TransferCreateCommand.builder()
                .warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .productId(5L).fromLocationId(2L).toLocationId(1L).lotNumber(null).quantity(30)
                .reasonId(null).note(null).createdBy(999L)
                .build();

        transferService.create(command);

        InOrder inOrder = inOrder(inventoryService);
        inOrder.verify(inventoryService).applyOrderedQuantityChange(
                eq(toLocation), eq(product), isNull(), any(), any(), eq(30));
        inOrder.verify(inventoryService).applyOrderedQuantityChange(
                eq(fromLocation), eq(product), isNull(), any(), any(), eq(-30));
    }

    @Test
    @DisplayName("가용재고가 부족하면 위치 갱신 없이 예외가 발생한다")
    void create_insufficientQuantity_throws() {
        Warehouse warehouse = warehouseWithId(100L);
        Location fromLocation = locationWithId(1L, warehouse);
        Location toLocation = locationWithId(2L, warehouse);
        Product product = productWithId(5L);
        Inventory sourceInventory = inventoryOf(fromLocation, product, 10, 0);

        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(locationService.getActiveInWarehouse(1L, 100L)).thenReturn(fromLocation);
        when(locationService.getActiveInWarehouse(2L, 100L)).thenReturn(toLocation);
        when(productService.getAccessible(5L, 1L)).thenReturn(product);
        when(inventoryService.getActiveByLocationProductLot(1L, 5L, null)).thenReturn(sourceInventory);
        doThrow(new CommonException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY))
                .when(inventoryValidator).validateSufficientQuantity(sourceInventory, 30);

        TransferCreateCommand command = TransferCreateCommand.builder()
                .warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .productId(5L).fromLocationId(1L).toLocationId(2L).lotNumber(null).quantity(30)
                .reasonId(null).note(null).createdBy(999L)
                .build();

        assertThatThrownBy(() -> transferService.create(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
        verify(inventoryService, never()).applyOrderedQuantityChange(
                any(), any(), any(), any(), any(), anyInt());
        verify(transferRepository, never()).save(any());
    }

    @Test
    @DisplayName("출발 위치와 도착 위치가 같으면 위치 검증에서 예외가 발생하고 이후 단계는 진행되지 않는다")
    void create_sameLocation_throws() {
        Warehouse warehouse = warehouseWithId(100L);
        Location location = locationWithId(1L, warehouse);

        when(warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(locationService.getActiveInWarehouse(1L, 100L)).thenReturn(location);
        doThrow(new CommonException(ErrorCode.TRANSFER_SAME_LOCATION))
                .when(transferValidator).validateLocations(location, location);

        TransferCreateCommand command = TransferCreateCommand.builder()
                .warehouseId(100L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .productId(5L).fromLocationId(1L).toLocationId(1L).lotNumber(null).quantity(10)
                .reasonId(null).note(null).createdBy(999L)
                .build();

        assertThatThrownBy(() -> transferService.create(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.TRANSFER_SAME_LOCATION);
        verifyNoInteractions(productService);
    }

}
