package com.jiubuntu.wms.biz.auth.infrastructure;

import com.jiubuntu.wms.biz.auth.domain.RefreshToken;
import com.jiubuntu.wms.biz.auth.infrastructure.custom.CustomRefreshTokenRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long>, CustomRefreshTokenRepository {
}
