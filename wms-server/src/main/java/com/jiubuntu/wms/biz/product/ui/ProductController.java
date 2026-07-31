package com.jiubuntu.wms.biz.product.ui;

import com.jiubuntu.wms.biz.product.application.ProductService;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductCreateCommand;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductDeleteCommand;
import com.jiubuntu.wms.biz.product.application.dto.command.ProductUpdateCommand;
import com.jiubuntu.wms.biz.product.application.dto.result.ProductResult;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.ui.payload.request.ProductCreateRequest;
import com.jiubuntu.wms.biz.product.ui.payload.request.ProductDeleteRequest;
import com.jiubuntu.wms.biz.product.ui.payload.request.ProductUpdateRequest;
import com.jiubuntu.wms.biz.product.ui.payload.response.ProductResponse;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.payload.constants.PageConstants;
import com.jiubuntu.wms.global.payload.constants.ResponseCode;
import com.jiubuntu.wms.global.payload.response.ApiCommonResponse;
import com.jiubuntu.wms.global.payload.response.ApiPageResponse;
import com.jiubuntu.wms.global.security.authentication.AuthPrincipal;
import com.jiubuntu.wms.global.security.resolver.annotation.Secure;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER})
@RestController
@RequestMapping("/api/product")
@RequiredArgsConstructor
public class ProductController {

    private final ProductService productService;

    @Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER, UserRole.WORKER})
    @GetMapping("/all")
    public ResponseEntity<ApiCommonResponse<List<ProductResponse>>> listAll(AuthPrincipal principal) {
        List<ProductResponse> results = productService.listAll(principal.getCompanyId()).stream()
                .map(ProductResponse::from)
                .toList();

        ApiCommonResponse<List<ProductResponse>> body = ApiCommonResponse.success(results);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<ProductResponse>>> list(
            AuthPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<ProductResult> results = productService.list(principal.getCompanyId(), keyword, PageRequest.of(page - 1, limit));
        Page<ProductResponse> mapped = results.map(ProductResponse::from);

        ApiCommonResponse<ApiPageResponse<ProductResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<ProductResponse>> create(
            AuthPrincipal principal,
            @Valid @RequestBody ProductCreateRequest request
    ) {
        ProductCreateCommand command = ProductCreateCommand.builder()
                .companyId(principal.getCompanyId())
                .skuCode(request.getSkuCode())
                .name(request.getName())
                .categoryId(request.getCategoryId())
                .storageTypeId(request.getStorageTypeId())
                .baseUnitId(request.getBaseUnitId())
                .subUnitId(request.getSubUnitId())
                .unitConversionRate(request.getUnitConversionRate())
                .lotTracking(request.isLotTracking())
                .description(request.getDescription())
                .build();
        Product product = productService.create(command);

        ApiCommonResponse<ProductResponse> body = ApiCommonResponse.success(ProductResponse.from(product), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiCommonResponse<ProductResponse>> update(
            AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ProductUpdateRequest request
    ) {
        ProductUpdateCommand command = ProductUpdateCommand.builder()
                .id(id)
                .expectedCompanyId(principal.getCompanyId())
                .name(request.getName())
                .categoryId(request.getCategoryId())
                .storageTypeId(request.getStorageTypeId())
                .baseUnitId(request.getBaseUnitId())
                .subUnitId(request.getSubUnitId())
                .unitConversionRate(request.getUnitConversionRate())
                .lotTracking(request.isLotTracking())
                .description(request.getDescription())
                .updatedBy(principal.getUserId())
                .build();
        Product product = productService.update(command);

        ApiCommonResponse<ProductResponse> body = ApiCommonResponse.success(ProductResponse.from(product));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiCommonResponse<Void>> delete(
            AuthPrincipal principal,
            @Valid @RequestBody ProductDeleteRequest request
    ) {
        ProductDeleteCommand command = new ProductDeleteCommand(request.getId(), principal.getCompanyId(), principal.getUserId());
        productService.delete(command);

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
