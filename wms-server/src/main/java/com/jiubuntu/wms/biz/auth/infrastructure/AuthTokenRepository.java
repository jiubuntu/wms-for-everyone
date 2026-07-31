package com.jiubuntu.wms.biz.auth.infrastructure;

import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.infrastructure.custom.CustomAuthTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuthTokenRepository extends JpaRepository<AuthToken, Long>, CustomAuthTokenRepository {
}
