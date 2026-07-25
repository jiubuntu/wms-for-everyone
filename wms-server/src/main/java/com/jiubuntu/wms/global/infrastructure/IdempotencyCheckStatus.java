package com.jiubuntu.wms.global.infrastructure;

public enum IdempotencyCheckStatus {

    NEW,
    IN_PROGRESS,
    COMPLETED,
    HASH_MISMATCH,

}
