package com.jiubuntu.wms.biz.warehouse.ui;

import com.jiubuntu.wms.biz.location.application.LocationService;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.warehouse.application.WarehouseService;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseCreateCommand;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseDeleteCommand;
import com.jiubuntu.wms.biz.warehouse.application.dto.command.WarehouseUpdateCommand;
import com.jiubuntu.wms.biz.warehouse.application.dto.result.WarehouseResult;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.biz.warehouse.ui.payload.request.WarehouseCreateRequest;
import com.jiubuntu.wms.biz.warehouse.ui.payload.request.WarehouseDeleteRequest;
import com.jiubuntu.wms.biz.warehouse.ui.payload.request.WarehouseUpdateRequest;
import com.jiubuntu.wms.biz.warehouse.ui.payload.response.WarehouseResponse;
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
import java.util.Map;

@RestController
@RequestMapping("/api/warehouse")
@RequiredArgsConstructor
public class WarehouseController {

    private final WarehouseService warehouseService;
    private final LocationService locationService;

    @Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER})
    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<WarehouseResponse>>> list(
            AuthPrincipal principal,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<WarehouseResult> results = warehouseService.list(
                principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(), keyword, PageRequest.of(page - 1, limit));

        List<Long> warehouseIds = results.getContent().stream().map(WarehouseResult::getId).toList();
        Map<Long, Long> locationCounts = locationService.countActiveByWarehouseIds(warehouseIds);

        Page<WarehouseResponse> mapped = results.map(result ->
                WarehouseResponse.from(result, locationCounts.getOrDefault(result.getId(), 0L)));

        ApiCommonResponse<ApiPageResponse<WarehouseResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER, UserRole.WORKER} )
    @GetMapping("/all")
    public ResponseEntity<ApiCommonResponse<List<WarehouseResponse>>> listAll(AuthPrincipal principal) {
        List<WarehouseResponse> results = warehouseService.listAll(
                        principal.getCompanyId(), principal.getRole(), principal.getWarehouseId())
                .stream()
                .map(WarehouseResponse::from)
                .toList();

        ApiCommonResponse<List<WarehouseResponse>> body = ApiCommonResponse.success(results);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER})
    @GetMapping("/{id}")
    public ResponseEntity<ApiCommonResponse<WarehouseResponse>> get(
            AuthPrincipal principal,
            @PathVariable Long id
    ) {
        Warehouse warehouse = warehouseService.getAccessible(
                id, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId());

        ApiCommonResponse<WarehouseResponse> body = ApiCommonResponse.success(WarehouseResponse.from(warehouse));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Secure(UserRole.COMPANY_ADMIN)
    @PostMapping("/create")
    public ResponseEntity<ApiCommonResponse<WarehouseResponse>> create(
            AuthPrincipal principal,
            @Valid @RequestBody WarehouseCreateRequest request
    ) {
        WarehouseCreateCommand command = WarehouseCreateCommand.builder()
                .companyId(principal.getCompanyId())
                .name(request.getName())
                .address(request.getAddress())
                .build();
        Warehouse warehouse = warehouseService.create(command);

        ApiCommonResponse<WarehouseResponse> body = ApiCommonResponse.success(WarehouseResponse.from(warehouse), ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Secure(UserRole.COMPANY_ADMIN)
    @PostMapping("/{id}/update")
    public ResponseEntity<ApiCommonResponse<WarehouseResponse>> update(
            AuthPrincipal principal,
            @PathVariable Long id,
            @Valid @RequestBody WarehouseUpdateRequest request
    ) {
        WarehouseUpdateCommand command = WarehouseUpdateCommand.builder()
                .id(id)
                .expectedCompanyId(principal.getCompanyId())
                .name(request.getName())
                .address(request.getAddress())
                .updatedBy(principal.getUserId())
                .build();
        Warehouse warehouse = warehouseService.update(command);

        ApiCommonResponse<WarehouseResponse> body = ApiCommonResponse.success(WarehouseResponse.from(warehouse));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @Secure(UserRole.COMPANY_ADMIN)
    @PostMapping("/delete")
    public ResponseEntity<ApiCommonResponse<Void>> delete(
            AuthPrincipal principal,
            @Valid @RequestBody WarehouseDeleteRequest request
    ) {
        WarehouseDeleteCommand command = new WarehouseDeleteCommand(request.getId(), principal.getCompanyId(), principal.getUserId());
        warehouseService.delete(command);

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
