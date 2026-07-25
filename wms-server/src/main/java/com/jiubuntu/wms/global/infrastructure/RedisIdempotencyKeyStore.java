package com.jiubuntu.wms.global.infrastructure;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;

@Component
@RequiredArgsConstructor
public class RedisIdempotencyKeyStore implements IdempotencyKeyStore {

    private static final String KEY_PREFIX = "idem:";
    private static final Duration IN_PROGRESS_TTL = Duration.ofSeconds(60);
    private static final Duration COMPLETED_TTL = Duration.ofHours(24);

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public IdempotencyCheckResult check(String action, String idempotencyKey, String requestHash) {
        String key = buildKey(action, idempotencyKey);
        String inProgressValue = serialize(new IdempotencyRecord(IdempotencyRecordStatus.IN_PROGRESS, requestHash, null, null));

        Boolean created = redisTemplate.opsForValue().setIfAbsent(key, inProgressValue, IN_PROGRESS_TTL);
        if (Boolean.TRUE.equals(created)) {
            return IdempotencyCheckResult.proceed();
        }

        String existingValue = redisTemplate.opsForValue().get(key);
        if (existingValue == null) {
            return IdempotencyCheckResult.inProgress();
        }

        IdempotencyRecord existing = deserialize(existingValue);
        if (!existing.getRequestHash().equals(requestHash)) {
            return IdempotencyCheckResult.hashMismatch();
        }

        return switch (existing.getStatus()) {
            case IN_PROGRESS -> IdempotencyCheckResult.inProgress();
            case COMPLETED -> IdempotencyCheckResult.completed(existing.getHttpStatus(), existing.getResponseBody());
            case FAILED -> {
                redisTemplate.opsForValue().set(key, inProgressValue, IN_PROGRESS_TTL);
                yield IdempotencyCheckResult.proceed();
            }
        };
    }

    @Override
    public void complete(String action, String idempotencyKey, String requestHash, int httpStatus, String responseBody) {
        String key = buildKey(action, idempotencyKey);
        IdempotencyRecord record = new IdempotencyRecord(IdempotencyRecordStatus.COMPLETED, requestHash, httpStatus, responseBody);
        redisTemplate.opsForValue().set(key, serialize(record), COMPLETED_TTL);
    }

    @Override
    public void fail(String action, String idempotencyKey, String requestHash) {
        String key = buildKey(action, idempotencyKey);
        IdempotencyRecord record = new IdempotencyRecord(IdempotencyRecordStatus.FAILED, requestHash, null, null);
        redisTemplate.opsForValue().set(key, serialize(record), IN_PROGRESS_TTL);
    }

    private String buildKey(String action, String idempotencyKey) {
        return KEY_PREFIX + action + ":" + idempotencyKey;
    }

    private String serialize(IdempotencyRecord record) {
        try {
            return objectMapper.writeValueAsString(record);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("멱등성 레코드 직렬화에 실패했습니다.", e);
        }
    }

    private IdempotencyRecord deserialize(String value) {
        try {
            return objectMapper.readValue(value, IdempotencyRecord.class);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("멱등성 레코드 역직렬화에 실패했습니다.", e);
        }
    }

}
