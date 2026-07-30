package com.jiubuntu.wms.biz.inventory.ui;

import com.jiubuntu.wms.biz.inventory.application.InventoryService;
import com.jiubuntu.wms.biz.inventory.application.dto.command.InventoryAdjustCommand;
import com.jiubuntu.wms.biz.inventory.application.dto.result.AvailableLocationResult;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryHistoryResult;
import com.jiubuntu.wms.biz.inventory.application.dto.result.InventoryResult;
import com.jiubuntu.wms.biz.inventory.domain.InventoryHistoryTargetType;
import com.jiubuntu.wms.biz.inventory.ui.payload.request.InventoryAdjustRequest;
import com.jiubuntu.wms.biz.inventory.ui.payload.response.AvailableLocationResponse;
import com.jiubuntu.wms.biz.inventory.ui.payload.response.InventoryHistoryResponse;
import com.jiubuntu.wms.biz.inventory.ui.payload.response.InventoryResponse;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.idempotency.Idempotent;
import com.jiubuntu.wms.global.payload.constants.PageConstants;
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

@Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER, UserRole.WORKER})
@RestController
@RequestMapping("/api/inventory")
@RequiredArgsConstructor
public class InventoryController {

    private final InventoryService inventoryService;

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<InventoryResponse>>> list(
            AuthPrincipal principal,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<InventoryResult> results = inventoryService.list(
                warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(), keyword,
                PageRequest.of(page - 1, limit));
        Page<InventoryResponse> mapped = results.map(InventoryResponse::from);

        ApiCommonResponse<ApiPageResponse<InventoryResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Idempotent("inventory-adjust")
    @PostMapping("/{id}/adjust")
    public ResponseEntity<ApiCommonResponse<InventoryResponse>> adjust(
            AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody InventoryAdjustRequest request
    ) {
        InventoryAdjustCommand command = new InventoryAdjustCommand(id, principal.getCompanyId(), principal.getRole(),
                principal.getWarehouseId(), request.getQuantity(), request.getReason(), principal.getUserId());
        InventoryResult result = inventoryService.adjust(command);

        ApiCommonResponse<InventoryResponse> body = ApiCommonResponse.success(InventoryResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping("/available-locations")
    public ResponseEntity<ApiCommonResponse<List<AvailableLocationResponse>>> availableLocations(
            AuthPrincipal principal,
            @RequestParam Long warehouseId,
            @RequestParam Long productId
    ) {
        List<AvailableLocationResult> results = inventoryService.getAvailableLocations(
                warehouseId, productId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId());
        List<AvailableLocationResponse> responses = results.stream().map(AvailableLocationResponse::from).toList();

        ApiCommonResponse<List<AvailableLocationResponse>> body = ApiCommonResponse.success(responses);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping("/history")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<InventoryHistoryResponse>>> history(
            AuthPrincipal principal,
            @RequestParam(required = false) Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) InventoryHistoryTargetType targetType,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<InventoryHistoryResult> results = inventoryService.getHistory(
                warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(),
                keyword, targetType, PageRequest.of(page - 1, limit));
        Page<InventoryHistoryResponse> mapped = results.map(InventoryHistoryResponse::from);

        ApiCommonResponse<ApiPageResponse<InventoryHistoryResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
