package com.jiubuntu.wms.biz.productunit.application;

import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.productunit.application.dto.command.ProductUnitDeleteCommand;
import com.jiubuntu.wms.biz.productunit.application.dto.command.ProductUnitUpdateCommand;
import com.jiubuntu.wms.biz.productunit.application.dto.result.ProductUnitResult;
import com.jiubuntu.wms.biz.productunit.application.validator.ProductUnitValidator;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.productunit.infrastructure.ProductUnitRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductUnitService {

    private final ProductUnitRepository productUnitRepository;
    private final ProductUnitValidator productUnitValidator;
    private final CompanyService companyService;

    public Page<ProductUnitResult> list(Long companyId, String keyword, Pageable pageable) {
        return productUnitRepository.findActiveByCompany(companyId, keyword, pageable)
                .map(this::toResult);
    }

    public List<ProductUnitResult> listAll(Long companyId) {
        return productUnitRepository.findActiveByCompany(companyId).stream()
                .map(this::toResult)
                .toList();
    }

    @Transactional
    public ProductUnit create(Long companyId, String name) {
        productUnitValidator.validateCreate(companyId, name);

        Company company = companyService.getActiveById(companyId);
        ProductUnit productUnit = new ProductUnit(company, name);
        return productUnitRepository.save(productUnit);
    }

    @Transactional
    public ProductUnit update(ProductUnitUpdateCommand command) {
        ProductUnit productUnit = getActiveById(command.getId());
        productUnitValidator.validateScope(productUnit, command.getExpectedCompanyId());

        productUnit.update(command.getName());
        productUnit.assignUpdater(command.getUpdatedBy());
        return productUnit;
    }

    @Transactional
    public void delete(ProductUnitDeleteCommand command) {
        ProductUnit productUnit = getActiveById(command.getId());
        productUnitValidator.validateScope(productUnit, command.getExpectedCompanyId());

        productUnit.assignUpdater(command.getUpdatedBy());
        productUnit.delete();
    }

    public ProductUnit getAccessible(Long id, Long companyId) {
        ProductUnit productUnit = getActiveById(id);

        Long ownerId = productUnit.getCompany() != null ? productUnit.getCompany().getId() : null;
        if (!Objects.equals(ownerId, companyId)) {
            throw new CommonException(ErrorCode.PRODUCT_UNIT_NOT_FOUND);
        }
        return productUnit;
    }

    /**
     * item 개수만큼 getAccessible()을 반복 호출하는 대신, 요청된 id 전체를 한 번에 조회한다.
     */
    public Map<Long, ProductUnit> getAllAccessible(Collection<Long> ids, Long companyId) {
        Map<Long, ProductUnit> unitsById = productUnitRepository.findAllActiveByIdIn(ids).stream()
                .collect(Collectors.toMap(ProductUnit::getId, unit -> unit));
        for (Long id : ids) {
            ProductUnit unit = unitsById.get(id);
            Long ownerId = unit != null && unit.getCompany() != null ? unit.getCompany().getId() : null;
            if (unit == null || !Objects.equals(ownerId, companyId)) {
                throw new CommonException(ErrorCode.PRODUCT_UNIT_NOT_FOUND);
            }
        }
        return unitsById;
    }

    private ProductUnit getActiveById(Long id) {
        return productUnitRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_UNIT_NOT_FOUND));
    }

    private ProductUnitResult toResult(ProductUnit productUnit) {
        return new ProductUnitResult(
                productUnit.getId(),
                productUnit.getCompany() != null ? productUnit.getCompany().getId() : null,
                productUnit.getName(),
                productUnit.getCreatedAt()
        );
    }

}
