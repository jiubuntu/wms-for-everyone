package com.jiubuntu.wms.biz.outbound.ui;

import com.jiubuntu.wms.biz.outbound.application.OutboundService;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundActionCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundAllocationCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundItemCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.command.OutboundRegisterCommand;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundListResult;
import com.jiubuntu.wms.biz.outbound.application.dto.result.OutboundResult;
import com.jiubuntu.wms.biz.outbound.ui.payload.request.OutboundCreateRequest;
import com.jiubuntu.wms.biz.outbound.ui.payload.response.OutboundListResponse;
import com.jiubuntu.wms.biz.outbound.ui.payload.response.OutboundResponse;
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
@RequestMapping("/api/warehouse/{warehouseId}/outbound")
@RequiredArgsConstructor
public class OutboundController {

    private final OutboundService outboundService;

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<OutboundListResponse>>> list(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<OutboundListResult> results = outboundService.list(
                warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(),
                PageRequest.of(page - 1, limit));
        Page<OutboundListResponse> mapped = results.map(OutboundListResponse::from);

        ApiCommonResponse<ApiPageResponse<OutboundListResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiCommonResponse<OutboundResponse>> get(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @PathVariable Long id
    ) {
        OutboundResult result = outboundService.getDetail(
                id, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId());

        ApiCommonResponse<OutboundResponse> body = ApiCommonResponse.success(OutboundResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Idempotent("outbound-register")
    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<OutboundResponse>> create(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @Valid @RequestBody OutboundCreateRequest request
    ) {
        List<OutboundItemCommand> items = request.getItems().stream()
                .map(item -> new OutboundItemCommand(
                        item.getProductId(),
                        item.getUnitId(),
                        item.getQuantity(),
                        item.getAllocationType(),
                        item.getAllocations() == null ? List.of()
                                : item.getAllocations().stream()
                                        .map(allocation -> new OutboundAllocationCommand(
                                                allocation.getLocationId(), allocation.getLotNumber(), allocation.getQuantity()))
                                        .toList()
                ))
                .toList();

        OutboundRegisterCommand command = OutboundRegisterCommand.builder()
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .customerName(request.getCustomerName())
                .note(request.getNote())
                .items(items)
                .createdBy(principal.getUserId())
                .build();
        OutboundResult result = outboundService.register(command);

        ApiCommonResponse<OutboundResponse> body = ApiCommonResponse.success(OutboundResponse.from(result), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Idempotent("outbound-complete")
    @PostMapping("/{id}/complete")
    public ResponseEntity<ApiCommonResponse<OutboundResponse>> complete(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @PathVariable Long id
    ) {
        OutboundActionCommand command = OutboundActionCommand.builder()
                .outboundId(id)
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .actorId(principal.getUserId())
                .build();
        OutboundResult result = outboundService.complete(command);

        ApiCommonResponse<OutboundResponse> body = ApiCommonResponse.success(OutboundResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Idempotent("outbound-cancel")
    @PostMapping("/{id}/cancel")
    public ResponseEntity<ApiCommonResponse<OutboundResponse>> cancel(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @PathVariable Long id
    ) {
        OutboundActionCommand command = OutboundActionCommand.builder()
                .outboundId(id)
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .actorId(principal.getUserId())
                .build();
        OutboundResult result = outboundService.cancel(command);

        ApiCommonResponse<OutboundResponse> body = ApiCommonResponse.success(OutboundResponse.from(result));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
