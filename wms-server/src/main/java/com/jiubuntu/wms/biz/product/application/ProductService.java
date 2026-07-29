package com.jiubuntu.wms.biz.product.application;

import com.jiubuntu.wms.biz.commoncode.application.CommonCodeService;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.company.application.CompanyService;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductCreateCommand;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductDeleteCommand;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductUpdateCommand;
import com.jiubuntu.wms.biz.product.application.dto.result.ProductResult;
import com.jiubuntu.wms.biz.product.application.validator.ProductValidator;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.infrastructure.ProductRepository;
import com.jiubuntu.wms.biz.productunit.application.ProductUnitService;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ProductService {

    private final ProductRepository productRepository;
    private final ProductValidator productValidator;
    private final CompanyService companyService;
    private final CommonCodeService commonCodeService;
    private final ProductUnitService productUnitService;

    public Page<ProductResult> list(Long companyId, Pageable pageable) {
        return productRepository.findActiveByCompany(companyId, pageable)
                .map(this::toResult);
    }

    public List<ProductResult> listAll(Long companyId) {
        return productRepository.findAllActiveByCompany(companyId).stream()
                .map(this::toResult)
                .toList();
    }

    public long countByCompany(Long companyId) {
        return productRepository.countActiveByCompany(companyId);
    }

    @Transactional
    public Product create(ProductCreateCommand command) {
        productValidator.validateCreate(command);

        Company company = companyService.getActiveById(command.getCompanyId());
        CommonCode category = commonCodeService.getAccessible(
                command.getCategoryId(), CommonCodeGroup.PRODUCT_CATEGORY, command.getCompanyId());
        CommonCode storageType = commonCodeService.getAccessible(
                command.getStorageTypeId(), CommonCodeGroup.STORAGE_TYPE, command.getCompanyId());
        ProductUnit baseUnit = productUnitService.getAccessible(command.getBaseUnitId(), command.getCompanyId());
        ProductUnit subUnit = command.getSubUnitId() != null
                ? productUnitService.getAccessible(command.getSubUnitId(), command.getCompanyId())
                : null;

        Product product = Product.builder()
                .company(company)
                .skuCode(command.getSkuCode())
                .name(command.getName())
                .category(category)
                .storageType(storageType)
                .baseUnit(baseUnit)
                .subUnit(subUnit)
                .unitConversionRate(command.getUnitConversionRate())
                .lotTracking(command.isLotTracking())
                .description(command.getDescription())
                .build();
        return productRepository.save(product);
    }

    @Transactional
    public Product update(ProductUpdateCommand command) {
        Product product = getActiveById(command.getId());
        productValidator.validateScope(product, command.getExpectedCompanyId());
        productValidator.validateUpdate(command);

        Long companyId = command.getExpectedCompanyId();
        CommonCode category = commonCodeService.getAccessible(command.getCategoryId(), CommonCodeGroup.PRODUCT_CATEGORY, companyId);
        CommonCode storageType = commonCodeService.getAccessible(command.getStorageTypeId(), CommonCodeGroup.STORAGE_TYPE, companyId);
        ProductUnit baseUnit = productUnitService.getAccessible(command.getBaseUnitId(), companyId);
        ProductUnit subUnit = command.getSubUnitId() != null
                ? productUnitService.getAccessible(command.getSubUnitId(), companyId)
                : null;

        product.update(command.getName(), category, storageType, baseUnit, subUnit,
                command.getUnitConversionRate(), command.isLotTracking(), command.getDescription());
        product.assignUpdater(command.getUpdatedBy());
        return product;
    }

    @Transactional
    public void delete(ProductDeleteCommand command) {
        Product product = getActiveById(command.getId());
        productValidator.validateScope(product, command.getExpectedCompanyId());

        product.assignUpdater(command.getUpdatedBy());
        product.delete();
    }

    public Product getAccessible(Long id, Long companyId) {
        Product product = getActiveById(id);
        productValidator.validateScope(product, companyId);
        return product;
    }

    private Product getActiveById(Long id) {
        return productRepository.findActiveById(id)
                .orElseThrow(() -> new CommonException(ErrorCode.PRODUCT_NOT_FOUND));
    }

    private ProductResult toResult(Product product) {
        return ProductResult.builder()
                .id(product.getId())
                .companyId(product.getCompany() != null ? product.getCompany().getId() : null)
                .skuCode(product.getSkuCode())
                .name(product.getName())
                .categoryId(product.getCategory().getId())
                .storageTypeId(product.getStorageType().getId())
                .baseUnitId(product.getBaseUnit().getId())
                .subUnitId(product.getSubUnit() != null ? product.getSubUnit().getId() : null)
                .unitConversionRate(product.getUnitConversionRate())
                .lotTracking(product.isLotTracking())
                .description(product.getDescription())
                .createdAt(product.getCreatedAt())
                .build();
    }

}
