package com.jiubuntu.wms.biz.productunit.ui;

import com.jiubuntu.wms.biz.productunit.application.ProductUnitService;
import com.jiubuntu.wms.biz.productunit.application.dto.command.ProductUnitDeleteCommand;
import com.jiubuntu.wms.biz.productunit.application.dto.command.ProductUnitUpdateCommand;
import com.jiubuntu.wms.biz.productunit.application.dto.result.ProductUnitResult;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.productunit.ui.payload.request.ProductUnitCreateRequest;
import com.jiubuntu.wms.biz.productunit.ui.payload.request.ProductUnitDeleteRequest;
import com.jiubuntu.wms.biz.productunit.ui.payload.request.ProductUnitUpdateRequest;
import com.jiubuntu.wms.biz.productunit.ui.payload.response.ProductUnitResponse;
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
@RequestMapping("/api/product-unit")
@RequiredArgsConstructor
public class ProductUnitController {

    private final ProductUnitService productUnitService;

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<ProductUnitResponse>>> list(
            AuthPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<ProductUnitResult> results = productUnitService.list(principal.getCompanyId(), keyword, PageRequest.of(page - 1, limit));
        Page<ProductUnitResponse> mapped = results.map(ProductUnitResponse::from);

        ApiCommonResponse<ApiPageResponse<ProductUnitResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping("/all")
    public ResponseEntity<ApiCommonResponse<List<ProductUnitResponse>>> listAll(AuthPrincipal principal) {
        List<ProductUnitResponse> results = productUnitService.listAll(principal.getCompanyId()).stream()
                .map(ProductUnitResponse::from)
                .toList();

        ApiCommonResponse<List<ProductUnitResponse>> body = ApiCommonResponse.success(results);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<ProductUnitResponse>> create(
            AuthPrincipal principal,
            @Valid @RequestBody ProductUnitCreateRequest request
    ) {
        ProductUnit productUnit = productUnitService.create(principal.getCompanyId(), request.getName());

        ApiCommonResponse<ProductUnitResponse> body = ApiCommonResponse.success(ProductUnitResponse.from(productUnit), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiCommonResponse<ProductUnitResponse>> update(
            AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody ProductUnitUpdateRequest request
    ) {
        ProductUnitUpdateCommand command = new ProductUnitUpdateCommand(id, principal.getCompanyId(), request.getName(), principal.getUserId());
        ProductUnit productUnit = productUnitService.update(command);

        ApiCommonResponse<ProductUnitResponse> body = ApiCommonResponse.success(ProductUnitResponse.from(productUnit));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiCommonResponse<Void>> delete(
            AuthPrincipal principal,
            @Valid @RequestBody ProductUnitDeleteRequest request
    ) {
        ProductUnitDeleteCommand command = new ProductUnitDeleteCommand(request.getId(), principal.getCompanyId(), principal.getUserId());
        productUnitService.delete(command);

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
