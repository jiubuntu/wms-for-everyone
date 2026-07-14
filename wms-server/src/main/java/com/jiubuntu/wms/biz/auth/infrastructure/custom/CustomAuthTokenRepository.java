package com.jiubuntu.wms.biz.auth.infrastructure.custom;

import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.domain.AuthTokenType;
import com.jiubuntu.wms.biz.user.domain.User;

import java.util.Optional;

public interface CustomAuthTokenRepository {

    Optional<AuthToken> findActiveByTokenAndType(String token, AuthTokenType type);

    void deactivateAllByUserAndType(User user, AuthTokenType type);

}
