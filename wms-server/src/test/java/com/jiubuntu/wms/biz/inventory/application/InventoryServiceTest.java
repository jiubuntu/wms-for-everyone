package com.jiubuntu.wms.biz.inventory.application;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.inventory.application.dto.command.InventoryAdjustCommand;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryResult;
import com.jiubuntu.wms.biz.inventory.application.validator.InventoryValidator;
import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistory;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryHistoryRepository;
import com.jiubuntu.wms.biz.inventory.infrastructure.InventoryRepository;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {

    @Mock
    private InventoryRepository inventoryRepository;

    @Mock
    private InventoryHistoryRepository inventoryHistoryRepository;

    @Mock
    private InventoryValidator inventoryValidator;

    @Mock
    private WarehouseService warehouseService;

    @InjectMocks
    private InventoryService inventoryService;

    private Warehouse warehouseWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", 1L);
        Warehouse warehouse = new Warehouse(company, "테스트창고", null, null);
        ReflectionTestUtils.setField(warehouse, "id", id);
        return warehouse;
    }

    private Location locationOf(Warehouse warehouse) {
        Location location = Location.builder()
                .warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1")
                .build();
        ReflectionTestUtils.setField(location, "id", 10L);
        return location;
    }

    private Product productOf() {
        Product product = new Product(null, "SKU-1", "테스트상품", null, null, null, null, null, false, null);
        ReflectionTestUtils.setField(product, "id", 5L);
        return product;
    }

    private Inventory inventoryWithId(Long id, Location location, Product product, int quantity, int reservedQuantity) {
        Inventory inventory = new Inventory(location, product, null, null, null, quantity);
        ReflectionTestUtils.setField(inventory, "id", id);
        ReflectionTestUtils.setField(inventory, "reservedQuantity", reservedQuantity);
        return inventory;
    }

    @Test
    @DisplayName("재고를 조정하면 변동량이 계산되어 이력에 기록되고 실재고가 갱신된다")
    void adjust_success() {
        Warehouse warehouse = warehouseWithId(100L);
        Location location = locationOf(warehouse);
        Product product = productOf();
        Inventory inventory = inventoryWithId(1L, location, product, 50, 10);
        when(inventoryRepository.findActiveById(1L)).thenReturn(Optional.of(inventory));
        when(inventoryRepository.findResultById(1L)).thenReturn(Optional.of(
                InventoryResult.builder().id(1L).quantity(80).reservedQuantity(10).build()));

        InventoryAdjustCommand command = new InventoryAdjustCommand(
                1L, 1L, UserRole.COMPANY_ADMIN, null, 80, "실사 결과 반영", 999L);
        InventoryResult result = inventoryService.adjust(command);

        assertThat(inventory.getQuantity()).isEqualTo(80);
        assertThat(inventory.getUpdatedBy()).isEqualTo(999L);
        assertThat(result.getQuantity()).isEqualTo(80);
        verify(warehouseService).getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null);

        ArgumentCaptor<InventoryHistory> captor = ArgumentCaptor.forClass(InventoryHistory.class);
        verify(inventoryHistoryRepository).save(captor.capture());
        assertThat(captor.getValue().getQuantityChange()).isEqualTo(30);
        assertThat(captor.getValue().getQuantityAfter()).isEqualTo(80);
        assertThat(captor.getValue().getTargetType()).isEqualTo(InventoryHistoryTargetType.ADJUSTMENT);
        assertThat(captor.getValue().getReason()).isEqualTo("실사 결과 반영");
    }

    @Test
    @DisplayName("조정 후 수량이 예약재고보다 적으면 예외가 발생하고 이력이 기록되지 않는다")
    void adjust_belowReserved_throws() {
        Warehouse warehouse = warehouseWithId(100L);
        Location location = locationOf(warehouse);
        Product product = productOf();
        Inventory inventory = inventoryWithId(1L, location, product, 50, 30);
        when(inventoryRepository.findActiveById(1L)).thenReturn(Optional.of(inventory));
        doThrow(new CommonException(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY))
                .when(inventoryValidator).validateAdjust(inventory, 20);

        InventoryAdjustCommand command = new InventoryAdjustCommand(
                1L, 1L, UserRole.COMPANY_ADMIN, null, 20, "실사 결과 반영", 999L);

        assertThatThrownBy(() -> inventoryService.adjust(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
        verify(inventoryHistoryRepository, never()).save(any());
    }

    @Test
    @DisplayName("존재하지 않는 재고를 조정하면 INVENTORY_NOT_FOUND 예외가 발생한다")
    void adjust_notFound_throws() {
        when(inventoryRepository.findActiveById(999L)).thenReturn(Optional.empty());

        InventoryAdjustCommand command = new InventoryAdjustCommand(
                999L, 1L, UserRole.COMPANY_ADMIN, null, 10, "사유", 1L);

        assertThatThrownBy(() -> inventoryService.adjust(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVENTORY_NOT_FOUND);
    }

}
