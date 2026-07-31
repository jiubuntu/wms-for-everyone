package com.jiubuntu.wms.biz.warehouse.application.validator;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.biz.warehouse.domain.WarehouseStaffAssignmentPort;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseValidatorTest {

    @Mock
    private WarehouseStaffAssignmentPort warehouseStaffAssignmentPort;

    @InjectMocks
    private WarehouseValidator warehouseValidator;

    private Warehouse warehouseWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        Warehouse warehouse = new Warehouse(company, "본사창고", null);
        ReflectionTestUtils.setField(warehouse, "id", id);
        return warehouse;
    }

    @Test
    @DisplayName("배정된 담당자가 있으면 WAREHOUSE_HAS_ASSIGNED_STAFF 예외가 발생한다")
    void validateDelete_hasAssignedStaff_throws() {
        Warehouse warehouse = warehouseWithId(1L);
        when(warehouseStaffAssignmentPort.hasActiveStaff(1L)).thenReturn(true);

        assertThatThrownBy(() -> warehouseValidator.validateDelete(warehouse))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.WAREHOUSE_HAS_ASSIGNED_STAFF);
    }

    @Test
    @DisplayName("배정된 담당자가 없으면 예외가 발생하지 않는다")
    void validateDelete_noAssignedStaff_success() {
        Warehouse warehouse = warehouseWithId(1L);
        when(warehouseStaffAssignmentPort.hasActiveStaff(1L)).thenReturn(false);

        assertThatCode(() -> warehouseValidator.validateDelete(warehouse)).doesNotThrowAnyException();
    }

}
