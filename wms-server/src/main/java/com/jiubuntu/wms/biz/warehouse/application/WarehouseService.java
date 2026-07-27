package com.jiubuntu.wms.biz.warehouse.application;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseCreateCommand;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseDeleteCommand;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseUpdateCommand;
import com.jiubuntu.wms.biz.warehouse.application.dto.result.WarehouseResult;
import com.jiubuntu.wms.biz.warehouse.application.validator.WarehouseValidator;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.biz.warehouse.infrastructure.WarehouseRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Objects;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class WarehouseService {

    private final WarehouseRepository warehouseRepository;
    private final WarehouseValidator warehouseValidator;
    private final CompanyService companyService;
    private final CommonCodeService commonCodeService;

    public Warehouse getActiveById(Long id) {
        return warehouseRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.WAREHOUSE_NOT_FOUND));
    }

    public Warehouse getAccessible(Long id, Long companyId, UserRole role, Long principalWarehouseId) {
        Warehouse warehouse = getActiveById(id);

        if (!Objects.equals(warehouse.getCompany().getId(), companyId)) {
            throw new CommonException(ErrorCode.COMPANY_SCOPE_VIOLATION);
        }
        if ((role == UserRole.WAREHOUSE_MANAGER || role == UserRole.WORKER)
                && !Objects.equals(warehouse.getId(), principalWarehouseId)) {
            throw new CommonException(ErrorCode.WAREHOUSE_SCOPE_VIOLATION);
        }
        return warehouse;
    }

    public Page<WarehouseResult> list(Long companyId, UserRole role, Long principalWarehouseId, Pageable pageable) {
        Page<Warehouse> warehouses = role == UserRole.WAREHOUSE_MANAGER
                ? warehouseRepository.findActiveByCompanyAndId(companyId, principalWarehouseId, pageable)
                : warehouseRepository.findActiveByCompany(companyId, pageable);
        return warehouses.map(this::toResult);
    }

    /**
     * 전역 창고 선택기용 — 페이지네이션 없이 전체 목록. 창고관리자·작업자는 담당 창고 1건만 반환한다.
     */
    public List<WarehouseResult> listAll(Long companyId, UserRole role, Long principalWarehouseId) {
        List<Warehouse> warehouses = (role == UserRole.WAREHOUSE_MANAGER || role == UserRole.WORKER)
                ? warehouseRepository.findAllActiveByCompanyAndId(companyId, principalWarehouseId)
                : warehouseRepository.findAllActiveByCompany(companyId);
        return warehouses.stream().map(this::toResult).toList();
    }

    @Transactional
    public Warehouse create(WarehouseCreateCommand command) {
        Company company = companyService.getActiveById(command.getCompanyId());
        CommonCode storageType = commonCodeService.getAccessible(
                command.getStorageTypeId(), CommonCodeGroup.STORAGE_TYPE, command.getCompanyId());

        Warehouse warehouse = new Warehouse(company, command.getName(), storageType, command.getAddress());
        return warehouseRepository.save(warehouse);
    }

    @Transactional
    public Warehouse update(WarehouseUpdateCommand command) {
        Warehouse warehouse = getAccessible(command.getId(), command.getExpectedCompanyId(), UserRole.COMPANY_ADMIN, null);
        CommonCode storageType = commonCodeService.getAccessible(
                command.getStorageTypeId(), CommonCodeGroup.STORAGE_TYPE, command.getExpectedCompanyId());

        warehouse.update(command.getName(), storageType, command.getAddress());
        warehouse.assignUpdater(command.getUpdatedBy());
        return warehouse;
    }

    @Transactional
    public void delete(WarehouseDeleteCommand command) {
        Warehouse warehouse = getAccessible(command.getId(), command.getExpectedCompanyId(), UserRole.COMPANY_ADMIN, null);
        warehouseValidator.validateDelete(warehouse);

        warehouse.assignUpdater(command.getUpdatedBy());
        warehouse.delete();
    }

    private WarehouseResult toResult(Warehouse warehouse) {
        return WarehouseResult.builder()
                .id(warehouse.getId())
                .companyId(warehouse.getCompany() != null ? warehouse.getCompany().getId() : null)
                .name(warehouse.getName())
                .storageTypeId(warehouse.getStorageType() != null ? warehouse.getStorageType().getId() : null)
                .address(warehouse.getAddress())
                .active(warehouse.isActive())
                .createdAt(warehouse.getCreatedAt())
                .build();
    }

}
