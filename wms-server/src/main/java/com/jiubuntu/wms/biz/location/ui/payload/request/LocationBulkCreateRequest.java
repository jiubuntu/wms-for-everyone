package com.jiubuntu.wms.biz.location.ui.payload.request;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
public class LocationBulkCreateRequest {

    @NotBlank(message = "구역명을 입력해주세요.")
    private String zone;

    @NotNull(message = "시작 행을 입력해주세요.")
    @Min(value = 1, message = "시작 행은 1 이상이어야 합니다.")
    private Integer rowFrom;

    @NotNull(message = "종료 행을 입력해주세요.")
    @Min(value = 1, message = "종료 행은 1 이상이어야 합니다.")
    private Integer rowTo;

    @NotNull(message = "시작 열을 입력해주세요.")
    @Min(value = 1, message = "시작 열은 1 이상이어야 합니다.")
    private Integer colFrom;

    @NotNull(message = "종료 열을 입력해주세요.")
    @Min(value = 1, message = "종료 열은 1 이상이어야 합니다.")
    private Integer colTo;

    @NotNull(message = "단 수를 입력해주세요.")
    @Min(value = 1, message = "단 수는 1 이상이어야 합니다.")
    private Integer levelCount;

    private Long storageTypeId;

}
