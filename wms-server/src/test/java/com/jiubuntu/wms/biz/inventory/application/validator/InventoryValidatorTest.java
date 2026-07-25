package com.jiubuntu.wms.biz.inventory.application.validator;

import com.jiubuntu.wms.biz.inventory.domain.Inventory;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InventoryValidatorTest {

    private final InventoryValidator inventoryValidator = new InventoryValidator();

    private Inventory inventoryOf(int quantity, int reservedQuantity) {
        Inventory inventory = new Inventory(null, null, null, null, null, quantity);
        ReflectionTestUtils.setField(inventory, "reservedQuantity", reservedQuantity);
        return inventory;
    }

    @Test
    @DisplayName("조정 후 수량이 예약재고보다 적으면 INSUFFICIENT_AVAILABLE_QUANTITY 예외가 발생한다")
    void validateAdjust_belowReserved_throws() {
        Inventory inventory = inventoryOf(100, 30);

        assertThatThrownBy(() -> inventoryValidator.validateAdjust(inventory, 20))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
    }

    @Test
    @DisplayName("조정 후 수량이 예약재고 이상이면 예외가 발생하지 않는다")
    void validateAdjust_aboveReserved_success() {
        Inventory inventory = inventoryOf(100, 30);

        assertThatCode(() -> inventoryValidator.validateAdjust(inventory, 50)).doesNotThrowAnyException();
    }

    @Test
    @DisplayName("가용재고보다 많은 수량을 요구하면 INSUFFICIENT_AVAILABLE_QUANTITY 예외가 발생한다")
    void validateSufficientQuantity_insufficient_throws() {
        Inventory inventory = inventoryOf(100, 30);

        assertThatThrownBy(() -> inventoryValidator.validateSufficientQuantity(inventory, 80))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INSUFFICIENT_AVAILABLE_QUANTITY);
    }

    @Test
    @DisplayName("가용재고 이내의 수량을 요구하면 예외가 발생하지 않는다")
    void validateSufficientQuantity_sufficient_success() {
        Inventory inventory = inventoryOf(100, 30);

        assertThatCode(() -> inventoryValidator.validateSufficientQuantity(inventory, 70)).doesNotThrowAnyException();
    }

}
