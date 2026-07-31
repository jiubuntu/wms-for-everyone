package com.jiubuntu.wms.biz.location.ui;

import com.jiubuntu.wms.biz.location.application.LocationService;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationBulkCreateCommand;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationDeleteCommand;
import com.jiubuntu.wms.biz.location.application.dto.command.LocationUpdateCommand;
import com.jiubuntu.wms.biz.location.application.dto.result.LocationResult;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.ui.payload.request.LocationBulkCreateRequest;
import com.jiubuntu.wms.biz.location.ui.payload.request.LocationDeleteRequest;
import com.jiubuntu.wms.biz.location.ui.payload.request.LocationUpdateRequest;
import com.jiubuntu.wms.biz.location.ui.payload.response.LocationResponse;
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
@RequestMapping("/api/warehouse/{warehouseId}/location")
@RequiredArgsConstructor
public class LocationController {

    private final LocationService locationService;

    @Secure({UserRole.COMPANY_ADMIN, UserRole.WAREHOUSE_MANAGER, UserRole.WORKER})
    @GetMapping("/all")
    public ResponseEntity<ApiCommonResponse<List<LocationResponse>>> listAll(
            AuthPrincipal principal,
            @PathVariable Long warehouseId
    ) {
        List<LocationResponse> results = locationService.listAll(
                        warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId())
                .stream()
                .map(LocationResponse::from)
                .toList();

        ApiCommonResponse<List<LocationResponse>> body = ApiCommonResponse.success(results);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @GetMapping("/list")
    public ResponseEntity<ApiCommonResponse<ApiPageResponse<LocationResponse>>> list(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @RequestParam(required = false) String keyword,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = PageConstants.DEFAULT_LIMIT) int limit
    ) {
        Page<LocationResult> results = locationService.list(
                warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(),
                keyword, PageRequest.of(page - 1, limit));
        Page<LocationResponse> mapped = results.map(LocationResponse::from);

        ApiCommonResponse<ApiPageResponse<LocationResponse>> body = ApiCommonResponse.success(ApiPageResponse.of(mapped));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/bulk-create")
    public ResponseEntity<ApiCommonResponse<List<LocationResponse>>> bulkCreate(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @Valid @RequestBody LocationBulkCreateRequest request
    ) {
        LocationBulkCreateCommand command = LocationBulkCreateCommand.builder()
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .zone(request.getZone())
                .rowFrom(request.getRowFrom())
                .rowTo(request.getRowTo())
                .colFrom(request.getColFrom())
                .colTo(request.getColTo())
                .levelCount(request.getLevelCount())
                .storageTypeId(request.getStorageTypeId())
                .createdBy(principal.getUserId())
                .build();
        List<Location> locations = locationService.bulkCreate(command);
        List<LocationResponse> responses = locations.stream().map(LocationResponse::from).toList();

        ApiCommonResponse<List<LocationResponse>> body = ApiCommonResponse.success(responses, ResponseCode.CREATED);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/{id}/update")
    public ResponseEntity<ApiCommonResponse<LocationResponse>> update(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @PathVariable Long id,
            @Valid @RequestBody LocationUpdateRequest request
    ) {
        LocationUpdateCommand command = LocationUpdateCommand.builder()
                .id(id)
                .warehouseId(warehouseId)
                .companyId(principal.getCompanyId())
                .role(principal.getRole())
                .principalWarehouseId(principal.getWarehouseId())
                .storageTypeId(request.getStorageTypeId())
                .status(request.getStatus())
                .updatedBy(principal.getUserId())
                .build();
        Location location = locationService.update(command);

        ApiCommonResponse<LocationResponse> body = ApiCommonResponse.success(LocationResponse.from(location));
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

    @PostMapping("/delete")
    public ResponseEntity<ApiCommonResponse<Void>> delete(
            AuthPrincipal principal,
            @PathVariable Long warehouseId,
            @Valid @RequestBody LocationDeleteRequest request
    ) {
        LocationDeleteCommand command = new LocationDeleteCommand(
                request.getId(), warehouseId, principal.getCompanyId(), principal.getRole(), principal.getWarehouseId(), principal.getUserId());
        locationService.delete(command);

        ApiCommonResponse<Void> body = ApiCommonResponse.success(null);
        return ResponseEntity.status(body.getHttpCode()).body(body);
    }

}
