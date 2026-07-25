package com.jiubuntu.wms.global.infrastructure;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class IdempotencyRecord {

    private IdempotencyRecordStatus status;
    private String requestHash;
    private Integer httpStatus;
    private String responseBody;

}
