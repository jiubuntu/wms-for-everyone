package com.jiubuntu.wms.global.infrastructure;

public interface IdempotencyKeyStore {

    IdempotencyCheckResult check(String action, String idempotencyKey, String requestHash);

    void complete(String action, String idempotencyKey, String requestHash, int httpStatus, String responseBody);

    void fail(String action, String idempotencyKey, String requestHash);

}
