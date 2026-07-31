package com.jiubuntu.wms.global.security.authentication;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JwtProviderTest {

    private final JwtProvider jwtProvider = new JwtProvider(
            "test-secret-key-for-jwt-provider-unit-test-minimum-256-bits-long", 1_800_000L);

    @Test
    @DisplayName("액세스 토큰을 발급하고 파싱하면 mustChangePassword를 포함한 모든 claim이 그대로 복원된다")
    void generateAndParse_roundTripsMustChangePasswordTrue() {
        AuthPrincipal principal = new AuthPrincipal(1L, 2L, UserRole.WORKER, 3L, true);

        String accessToken = jwtProvider.generateAccessToken(principal);
        AuthPrincipal parsed = jwtProvider.parseAccessToken(accessToken);

        assertThat(parsed.getUserId()).isEqualTo(1L);
        assertThat(parsed.getCompanyId()).isEqualTo(2L);
        assertThat(parsed.getRole()).isEqualTo(UserRole.WORKER);
        assertThat(parsed.getWarehouseId()).isEqualTo(3L);
        assertThat(parsed.isMustChangePassword()).isTrue();
    }

    @Test
    @DisplayName("mustChangePassword가 false인 상태로 발급하면 파싱 결과도 false다")
    void generateAndParse_roundTripsMustChangePasswordFalse() {
        AuthPrincipal principal = new AuthPrincipal(1L, 2L, UserRole.COMPANY_ADMIN, null, false);

        String accessToken = jwtProvider.generateAccessToken(principal);
        AuthPrincipal parsed = jwtProvider.parseAccessToken(accessToken);

        assertThat(parsed.isMustChangePassword()).isFalse();
    }

}
