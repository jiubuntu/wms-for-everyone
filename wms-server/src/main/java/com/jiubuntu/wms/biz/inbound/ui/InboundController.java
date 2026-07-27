package com.jiubuntu.wms.biz.inbound.ui;

import com.jiubuntu.wms.biz.inbound.application.InboundService;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundActionCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundItemCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundLocationCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundLocationReplaceCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.command.InboundRegisterCommand;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundListResult;
import com.jiubuntu.wms.biz.inbound.application.dto.result.InboundResult;
import com.jiubuntu.wms.biz.inbound.ui.payload.request.InboundCreateRequest;
import com.jiubuntu.wms.biz.inbound.ui.payload.request.InboundLocationReplaceRequest;
import com.jiubuntu.wms.biz.inbound.ui.payload.response.InboundListResponse;
import com.jiubuntu.wms.biz.inbound.ui.payload.response.InboundResponse;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.global.idempotency.Idempotent;
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

@Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER, UserRole.WORKER})
@RestController
@RequestMapping("/api/warehouse/{warehouseId}/inbound")
@RequiredArgsConstructor
public class InboundController {

    private final InboundService inboundService;

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<InboundListResponse>>> list(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<InboundListResult> results = inboundService.list(
                warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(),
                PageRequest.of(page - 1, limit));
        Page<InboundListResponse> mapped = results.map(InboundListResponse::from);

        ApiCommonResponse<ApiPageResponse<InboundListResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiCommonResponse<InboundResponse>> get(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @PathVariable Long id
    ) {
        InboundResult result = inboundService.getDetail(
                id, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId());

        ApiCommonResponse<InboundResponse> body = ApiCommonResponse.success(InboundResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Idempotent("inbound-register")
    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<InboundResponse>> create(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @Valid @RequestBody InboundCreateRequest request
    ) {
        List<InboundItemCommand> items = request.getItems().stream()
                .map(item -> new InboundItemCommand(item.getProductId(), item.getUnitId(), item.getQuantity(),
                        item.getLotNumber(), item.getManufactureDate(), item.getExpiryDate()))
                .toList();

        InboundRegisterCommand command = InboundRegisterCommand.builder()
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .supplierName(request.getSupplierName())
                .note(request.getNote())
                .items(items)
                .createdBy(principal.getUserId())
                .build();
        InboundResult result = inboundService.register(command);

        ApiCommonResponse<InboundResponse> body = ApiCommonResponse.success(InboundResponse.from(result), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/{id}/items/{itemId}/locations")
    public ResponseEntity<ApiCommonResponse<InboundResponse>> replaceItemLocations(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @PathVariable Long id,
            @PathVariable Long itemId,
            @Valid @RequestBody InboundLocationReplaceRequest request
    ) {
        List<InboundLocationCommand> locations = request.getLocations().stream()
                .map(location -> new InboundLocationCommand(location.getLocationId(), location.getQuantity()))
                .toList();

        InboundLocationReplaceCommand command = InboundLocationReplaceCommand.builder()
                .inboundId(id)
                .itemId(itemId)
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .locations(locations)
                .actorId(principal.getUserId())
                .build();
        InboundResult result = inboundService.replaceItemLocations(command);

        ApiCommonResponse<InboundResponse> body = ApiCommonResponse.success(InboundResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Idempotent("inbound-complete")
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiCommonResponse<InboundResponse>> complete(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @PathVariable Long id
    ) {
        InboundActionCommand command = InboundActionCommand.builder()
                .inboundId(id)
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .actorId(principal.getUserId())
                .build();
        InboundResult result = inboundService.complete(command);

        ApiCommonResponse<InboundResponse> body = ApiCommonResponse.success(InboundResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Idempotent("inbound-cancel")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiCommonResponse<InboundResponse>> cancel(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @PathVariable Long id
    ) {
        InboundActionCommand command = InboundActionCommand.builder()
                .inboundId(id)
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .actorId(principal.getUserId())
                .build();
        InboundResult result = inboundService.cancel(command);

        ApiCommonResponse<InboundResponse> body = ApiCommonResponse.success(InboundResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
