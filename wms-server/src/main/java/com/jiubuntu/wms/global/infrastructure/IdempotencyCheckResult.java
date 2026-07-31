package com.jiubuntu.wms.global.infrastructure;

import lombok.Getter;

@Getter
public class IdempotencyCheckResult {

    private final IdempotencyCheckStatus status;
    private final Integer cachedHttpStatus;
    private final String cachedResponseBody;

    private IdempotencyCheckResult(IdempotencyCheckStatus status, Integer cachedHttpStatus, String cachedResponseBody) {
        this.status = status;
        this.cachedHttpStatus = cachedHttpStatus;
        this.cachedResponseBody = cachedResponseBody;
    }

    public static IdempotencyCheckResult proceed() {
        return new IdempotencyCheckResult(IdempotencyCheckStatus.NEW, null, null);
    }

    public static IdempotencyCheckResult inProgress() {
        return new IdempotencyCheckResult(IdempotencyCheckStatus.IN_PROGRESS, null, null);
    }

    public static IdempotencyCheckResult hashMismatch() {
        return new IdempotencyCheckResult(IdempotencyCheckStatus.HASH_MISMATCH, null, null);
    }

    public static IdempotencyCheckResult completed(int cachedHttpStatus, String cachedResponseBody) {
        return new IdempotencyCheckResult(IdempotencyCheckStatus.COMPLETED, cachedHttpStatus, cachedResponseBody);
    }

}
