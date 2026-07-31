package com.jiubuntu.wms.biz.location.application;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationBulkCreateCommand;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationDeleteCommand;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationUpdateCommand;
import com.jiubuntu.wms.biz.location.application.validator.LocationValidator;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.domain.LocationStatus;
import com.jiubuntu.wms.biz.location.infrastructure.LocationRepository;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class LocationServiceTest {

    @Mock
    private LocationRepository locationRepository;

    @Mock
    private LocationValidator locationValidator;

    @Mock
    private WarehouseService warehouseService;

    @Mock
    private CommonCodeService commonCodeService;

    @InjectMocks
    private LocationService locationService;

    private Warehouse warehouseWithId(Long id) {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        ReflectionTestUtils.setField(company, "id", 1L);
        Warehouse warehouse = new Warehouse(company, "본사창고", null);
        ReflectionTestUtils.setField(warehouse, "id", id);
        return warehouse;
    }

    @Test
    @DisplayName("행 2개 x 열 3개 x 2단을 입력하면 12개 위치가 A-01-01-1 형식 코드로 생성된다")
    void bulkCreate_generatesExpectedCodes() {
        Warehouse warehouse = warehouseWithId(10L);
        when(warehouseService.getAccessible(10L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        when(locationRepository.saveAll(anyList())).thenAnswer(invocation -> invocation.getArgument(0));

        LocationBulkCreateCommand command = LocationBulkCreateCommand.builder()
                .warehouseId(10L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .zone("A").rowFrom(1).rowTo(2).colFrom(1).colTo(3).levelCount(2)
                .createdBy(999L)
                .build();

        List<Location> result = locationService.bulkCreate(command);

        assertThat(result).hasSize(12);
        assertThat(result).extracting(Location::getCode)
                .contains("A-01-01-1", "A-01-01-2", "A-02-03-2");
        verify(locationValidator).validateNoDuplicateCodes(any(), anyList());
    }

    @Test
    @DisplayName("중복된 코드가 있으면 LOCATION_ALREADY_EXISTS 예외가 검증기에서 전파된다")
    void bulkCreate_duplicateCode_throws() {
        Warehouse warehouse = warehouseWithId(10L);
        when(warehouseService.getAccessible(10L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);
        org.mockito.Mockito.doThrow(new CommonException(ErrorCode.LOCATION_ALREADY_EXISTS))
                .when(locationValidator).validateNoDuplicateCodes(any(), anyList());

        LocationBulkCreateCommand command = LocationBulkCreateCommand.builder()
                .warehouseId(10L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .zone("A").rowFrom(1).rowTo(1).colFrom(1).colTo(1).levelCount(1)
                .createdBy(999L)
                .build();

        assertThatThrownBy(() -> locationService.bulkCreate(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.LOCATION_ALREADY_EXISTS);
    }

    @Test
    @DisplayName("위치를 수정하면 보관유형과 상태가 갱신된다")
    void update_success() {
        Warehouse warehouse = warehouseWithId(10L);
        Location location = Location.builder().warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1").build();
        ReflectionTestUtils.setField(location, "id", 100L);
        when(locationRepository.findActiveById(100L)).thenReturn(Optional.of(location));
        when(warehouseService.getAccessible(10L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);

        LocationUpdateCommand command = LocationUpdateCommand.builder()
                .id(100L).warehouseId(10L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .status(LocationStatus.DISABLED)
                .updatedBy(999L)
                .build();
        Location result = locationService.update(command);

        assertThat(result.getStatus()).isEqualTo(LocationStatus.DISABLED);
        assertThat(result.getUpdatedBy()).isEqualTo(999L);
    }

    @Test
    @DisplayName("다른 창고 소속 위치를 수정하려 하면 LOCATION_NOT_FOUND 예외가 발생한다")
    void update_wrongWarehouse_throws() {
        Warehouse warehouse = warehouseWithId(10L);
        Location location = Location.builder().warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1").build();
        ReflectionTestUtils.setField(location, "id", 100L);
        when(locationRepository.findActiveById(100L)).thenReturn(Optional.of(location));

        LocationUpdateCommand command = LocationUpdateCommand.builder()
                .id(100L).warehouseId(999L).companyId(1L).role(UserRole.COMPANY_ADMIN).principalWarehouseId(null)
                .status(LocationStatus.DISABLED)
                .updatedBy(999L)
                .build();

        assertThatThrownBy(() -> locationService.update(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.LOCATION_NOT_FOUND);
    }

    @Test
    @DisplayName("위치를 삭제하면 소프트 삭제된다")
    void delete_success() {
        Warehouse warehouse = warehouseWithId(10L);
        Location location = Location.builder().warehouse(warehouse).zone("A").row("01").col("01").level("1").code("A-01-01-1").build();
        ReflectionTestUtils.setField(location, "id", 100L);
        when(locationRepository.findActiveById(100L)).thenReturn(Optional.of(location));
        when(warehouseService.getAccessible(10L, 1L, UserRole.COMPANY_ADMIN, null)).thenReturn(warehouse);

        LocationDeleteCommand command = new LocationDeleteCommand(100L, 10L, 1L, UserRole.COMPANY_ADMIN, null, 999L);
        locationService.delete(command);

        assertThat(location.isActive()).isFalse();
    }

}
