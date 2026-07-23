package com.jiubuntu.wms.biz.inventory.ui;

import com.jiubuntu.wms.biz.inventory.application.TransferService;
import com.jiubuntu.wms.biz.inventory.application.dto.command.TransferCreateCommand;
import com.jiubuntu.wms.biz.inventory.application.dto.result.TransferResult;
import com.jiubuntu.wms.biz.inventory.ui.payload.request.TransferCreateRequest;
import com.jiubuntu.wms.biz.inventory.ui.payload.response.TransferResponse;
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

@Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER, UserRole.WORKER})
@RestController
@RequestMapping("/api/warehouse/{warehouseId}/transfer")
@RequiredArgsConstructor
public class TransferController {

    private final TransferService transferService;

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<TransferResponse>>> list(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<TransferResult> results = transferService.list(
                warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(),
                PageRequest.of(page - 1, limit));
        Page<TransferResponse> mapped = results.map(TransferResponse::from);

        ApiCommonResponse<ApiPageResponse<TransferResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<TransferResponse>> create(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @Valid @RequestBody TransferCreateRequest request
    ) {
        TransferCreateCommand command = TransferCreateCommand.builder()
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .productId(request.getProductId())
                .fromLocationId(request.getFromLocationId())
                .toLocationId(request.getToLocationId())
                .lotNumber(request.getLotNumber())
                .quantity(request.getQuantity())
                .reasonId(request.getReasonId())
                .note(request.getNote())
                .createdBy(principal.getUserId())
                .build();
        TransferResult result = transferService.create(command);

        ApiCommonResponse<TransferResponse> body = ApiCommonResponse.success(TransferResponse.from(result), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
