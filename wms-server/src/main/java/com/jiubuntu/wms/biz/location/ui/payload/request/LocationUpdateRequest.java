package com.jiubuntu.wms.biz.location.ui.payload.request;

import com.jiubuntu.wms.biz.location.domain.LocationStatus;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocationUpdateRequest {

    private Long storageTypeId;

    @NotNull(message = "상태를 선택해주세요.")
    private LocationStatus status;

}
