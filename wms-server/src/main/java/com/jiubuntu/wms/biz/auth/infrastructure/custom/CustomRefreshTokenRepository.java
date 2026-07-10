package com.jiubuntu.wms.biz.auth.infrastructure.custom;

import com.jiubuntu.wms.biz.auth.domain.RefreshToken;

import java.util.Optional;

public interface CustomRefreshTokenRepository {

    Optional<RefreshToken> findActiveByToken(String token);

}
