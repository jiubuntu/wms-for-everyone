package com.jiubuntu.wms.biz.warehouse.application;

import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseCreateCommand;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseDeleteCommand;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseUpdateCommand;
import com.jiubuntu.wms.biz.warehouse.application.validator.WarehouseValidator;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.biz.warehouse.infrastructure.WarehouseRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class WarehouseServiceTest {

    @Mock
    private WarehouseRepository warehouseRepository;

    @Mock
    private WarehouseValidator warehouseValidator;

    @Mock
    private CompanyService companyService;

    @InjectMocks
    private WarehouseService warehouseService;

    private Company companyWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", id);
        return company;
    }

    private Warehouse warehouseWithId(Long id, Company company) {
        Warehouse warehouse = new Warehouse(company, "본사창고", "서울시");
        ReflectionTestUtils.setField(warehouse, "id", id);
        return warehouse;
    }

    @Test
    @DisplayName("창고를 등록하면 회사를 조회해 연결한다")
    void create_success() {
        when(companyService.getActiveById(1L)).thenReturn(companyWithId(1L));
        when(warehouseRepository.save(any(Warehouse.class))).thenAnswer(invocation -> invocation.getArgument(0));

        WarehouseCreateCommand command = WarehouseCreateCommand.builder()
                .companyId(1L).name("본사창고").address("서울시")
                .build();
        Warehouse result = warehouseService.create(command);

        assertThat(result.getName()).isEqualTo("본사창고");
    }

    @Test
    @DisplayName("존재하지 않는 창고를 조회하면 WAREHOUSE_NOT_FOUND 예외가 발생한다")
    void getAccessible_notFound() {
        when(warehouseRepository.findActiveById(100L)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> warehouseService.getAccessible(100L, 1L, UserRole.COMPANY_ADMIN, null))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.WAREHOUSE_NOT_FOUND);
    }

    @Test
    @DisplayName("다른 회사의 창고에 접근하면 COMPANY_SCOPE_VIOLATION 예외가 발생한다")
    void getAccessible_otherCompany_violation() {
        Warehouse warehouse = warehouseWithId(100L, companyWithId(1L));
        when(warehouseRepository.findActiveById(100L)).thenReturn(Optional.of(warehouse));

        assertThatThrownBy(() -> warehouseService.getAccessible(100L, 2L, UserRole.COMPANY_ADMIN, null))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.COMPANY_SCOPE_VIOLATION);
    }

    @Test
    @DisplayName("창고관리자가 담당 창고가 아닌 창고에 접근하면 WAREHOUSE_SCOPE_VIOLATION 예외가 발생한다")
    void getAccessible_warehouseManager_otherWarehouse_violation() {
        Warehouse warehouse = warehouseWithId(100L, companyWithId(1L));
        when(warehouseRepository.findActiveById(100L)).thenReturn(Optional.of(warehouse));

        assertThatThrownBy(() -> warehouseService.getAccessible(100L, 1L, UserRole.WAREHOUSE_MANAGER, 999L))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.WAREHOUSE_SCOPE_VIOLATION);
    }

    @Test
    @DisplayName("창고관리자가 담당 창고에 접근하면 예외 없이 조회된다")
    void getAccessible_warehouseManager_ownWarehouse_success() {
        Warehouse warehouse = warehouseWithId(100L, companyWithId(1L));
        when(warehouseRepository.findActiveById(100L)).thenReturn(Optional.of(warehouse));

        Warehouse result = warehouseService.getAccessible(100L, 1L, UserRole.WAREHOUSE_MANAGER, 100L);

        assertThat(result.getId()).isEqualTo(100L);
    }

    @Test
    @DisplayName("창고를 수정하면 이름과 주소가 갱신된다")
    void update_success() {
        Warehouse warehouse = warehouseWithId(100L, companyWithId(1L));
        when(warehouseRepository.findActiveById(100L)).thenReturn(Optional.of(warehouse));

        WarehouseUpdateCommand command = WarehouseUpdateCommand.builder()
                .id(100L).expectedCompanyId(1L).name("제2창고").address("부산시")
                .updatedBy(999L)
                .build();
        Warehouse result = warehouseService.update(command);

        assertThat(result.getName()).isEqualTo("제2창고");
        assertThat(result.getAddress()).isEqualTo("부산시");
        assertThat(result.getUpdatedBy()).isEqualTo(999L);
    }

    @Test
    @DisplayName("창고를 삭제하면 검증기를 거쳐 소프트 삭제된다")
    void delete_success() {
        Warehouse warehouse = warehouseWithId(100L, companyWithId(1L));
        when(warehouseRepository.findActiveById(100L)).thenReturn(Optional.of(warehouse));

        WarehouseDeleteCommand command = new WarehouseDeleteCommand(100L, 1L, 999L);
        warehouseService.delete(command);

        assertThat(warehouse.isActive()).isFalse();
        assertThat(warehouse.getUpdatedBy()).isEqualTo(999L);
    }

}
