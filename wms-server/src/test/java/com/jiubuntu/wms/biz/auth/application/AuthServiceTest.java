package com.jiubuntu.wms.biz.auth.application;

import com.jiubuntu.wms.biz.auth.application.dto.result.AuthLoginResult;
import com.jiubuntu.wms.biz.auth.application.validator.AuthValidator;
import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.domain.AuthTokenType;
import com.jiubuntu.wms.biz.auth.infrastructure.AuthTokenRepository;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import com.jiubuntu.wms.global.security.authentication.AuthPrincipal;
import com.jiubuntu.wms.global.security.authentication.JwtProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    private static final long REFRESH_TOKEN_EXPIRATION = 1_209_600_000L;
    private static final int MAX_FAIL_COUNT = 5;
    private static final long LOCK_DURATION_MINUTES = 30L;

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private AuthValidator authValidator;

    @InjectMocks
    private AuthService authService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(authService, "refreshTokenExpiration", REFRESH_TOKEN_EXPIRATION);
        ReflectionTestUtils.setField(authService, "maxFailCount", MAX_FAIL_COUNT);
        ReflectionTestUtils.setField(authService, "lockDurationMinutes", LOCK_DURATION_MINUTES);
    }

    private User activeUser() {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        return new User(company, null, "user@test.com", "encoded-password",
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("로그인 성공 시 토큰이 발급되고 실패 카운트가 초기화된다")
    void login_success() {
        User user = activeUser();
        when(userService.findActiveByEmail("user@test.com")).thenReturn(Optional.of(user));
        when(jwtProvider.generateAccessToken(any(AuthPrincipal.class))).thenReturn("access-token");

        AuthLoginResult result = authService.login("user@test.com", "password1!");

        assertThat(result.getAccessToken()).isEqualTo("access-token");
        assertThat(result.getRefreshToken()).isNotBlank();
        assertThat(result.getRefreshTokenExpirationMillis()).isEqualTo(REFRESH_TOKEN_EXPIRATION);
        assertThat(user.getLoginFailedCount()).isZero();
        verify(authTokenRepository).save(any(AuthToken.class));
    }

    @Test
    @DisplayName("존재하지 않는 이메일이면 INVALID_CREDENTIALS 예외가 발생하고 검증 로직은 호출되지 않는다")
    void login_userNotFound() {
        when(userService.findActiveByEmail(anyString())).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.login("unknown@test.com", "password1!"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        verifyNoInteractions(authValidator);
    }

    @Test
    @DisplayName("비밀번호가 틀리면 실패 카운트가 증가하고 임계치 미만이면 잠기지 않는다")
    void login_invalidCredentials_belowThreshold() {
        User user = activeUser();
        ReflectionTestUtils.setField(user, "loginFailedCount", 2);
        when(userService.findActiveByEmail(anyString())).thenReturn(Optional.of(user));
        doThrow(new CommonException(ErrorCode.INVALID_CREDENTIALS))
                .when(authValidator).validateLogin(user, "wrong-password");

        assertThatThrownBy(() -> authService.login("user@test.com", "wrong-password"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_CREDENTIALS);
        assertThat(user.getLoginFailedCount()).isEqualTo(3);
        assertThat(user.getStatus()).isEqualTo(UserStatus.ACTIVE);
    }

    @Test
    @DisplayName("비밀번호 실패가 임계치에 도달하면 계정이 잠긴다")
    void login_invalidCredentials_reachesThreshold_locksAccount() {
        User user = activeUser();
        ReflectionTestUtils.setField(user, "loginFailedCount", MAX_FAIL_COUNT - 1);
        when(userService.findActiveByEmail(anyString())).thenReturn(Optional.of(user));
        doThrow(new CommonException(ErrorCode.INVALID_CREDENTIALS))
                .when(authValidator).validateLogin(user, "wrong-password");

        assertThatThrownBy(() -> authService.login("user@test.com", "wrong-password"))
                .isInstanceOf(CommonException.class);
        assertThat(user.getLoginFailedCount()).isEqualTo(MAX_FAIL_COUNT);
        assertThat(user.getStatus()).isEqualTo(UserStatus.LOCKED);
        assertThat(user.getLockedUntil()).isAfter(LocalDateTime.now());
    }

    @Test
    @DisplayName("자격 증명 외 사유(계정 잠금 등)로 검증에 실패하면 실패 카운트는 증가하지 않는다")
    void login_otherValidatorError_doesNotIncreaseFailCount() {
        User user = activeUser();
        when(userService.findActiveByEmail(anyString())).thenReturn(Optional.of(user));
        doThrow(new CommonException(ErrorCode.ACCOUNT_LOCKED))
                .when(authValidator).validateLogin(user, "password1!");

        assertThatThrownBy(() -> authService.login("user@test.com", "password1!"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.ACCOUNT_LOCKED);
        assertThat(user.getLoginFailedCount()).isZero();
    }

    @Test
    @DisplayName("리프레시 토큰 재발급 성공 시 기존 토큰은 비활성화되고 새 토큰이 발급된다")
    void refresh_success() {
        User user = activeUser();
        AuthToken oldToken = new AuthToken(user, "old-token", AuthTokenType.REFRESH, LocalDateTime.now().plusDays(1));
        when(authTokenRepository.findActiveByTokenAndType("old-token", AuthTokenType.REFRESH)).thenReturn(Optional.of(oldToken));
        when(jwtProvider.generateAccessToken(any(AuthPrincipal.class))).thenReturn("new-access-token");

        AuthLoginResult result = authService.refresh("old-token");

        ArgumentCaptor<AuthToken> savedTokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(savedTokenCaptor.capture());
        AuthToken savedToken = savedTokenCaptor.getValue();

        assertThat(oldToken.isActive()).isFalse();
        assertThat(savedToken.getToken()).isNotEqualTo(oldToken.getToken());
        assertThat(result.getRefreshToken()).isEqualTo(savedToken.getToken());
        assertThat(result.getAccessToken()).isEqualTo("new-access-token");
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰이면 INVALID_REFRESH_TOKEN 예외가 발생한다")
    void refresh_tokenNotFound() {
        when(authTokenRepository.findActiveByTokenAndType(anyString(), eq(AuthTokenType.REFRESH))).thenReturn(Optional.empty());

        assertThatThrownBy(() -> authService.refresh("unknown-token"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_REFRESH_TOKEN);
    }

    @Test
    @DisplayName("만료된 리프레시 토큰이면 EXPIRED_REFRESH_TOKEN 예외가 발생하고 토큰은 삭제되지 않는다")
    void refresh_expiredToken() {
        User user = activeUser();
        AuthToken expiredToken = new AuthToken(user, "expired-token", AuthTokenType.REFRESH, LocalDateTime.now().minusSeconds(1));
        when(authTokenRepository.findActiveByTokenAndType("expired-token", AuthTokenType.REFRESH)).thenReturn(Optional.of(expiredToken));
        doThrow(new CommonException(ErrorCode.EXPIRED_REFRESH_TOKEN))
                .when(authValidator).validateRefreshToken(expiredToken);

        assertThatThrownBy(() -> authService.refresh("expired-token"))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXPIRED_REFRESH_TOKEN);
        assertThat(expiredToken.isActive()).isTrue();
        verify(authTokenRepository, never()).save(any(AuthToken.class));
    }

    @Test
    @DisplayName("로그아웃 시 리프레시 토큰이 비활성화된다")
    void logout_tokenFound() {
        User user = activeUser();
        AuthToken token = new AuthToken(user, "token", AuthTokenType.REFRESH, LocalDateTime.now().plusDays(1));
        when(authTokenRepository.findActiveByTokenAndType("token", AuthTokenType.REFRESH)).thenReturn(Optional.of(token));

        authService.logout("token");

        assertThat(token.isActive()).isFalse();
    }

    @Test
    @DisplayName("존재하지 않는 리프레시 토큰으로 로그아웃해도 예외가 발생하지 않는다")
    void logout_tokenNotFound() {
        when(authTokenRepository.findActiveByTokenAndType(anyString(), eq(AuthTokenType.REFRESH))).thenReturn(Optional.empty());

        assertThatCode(() -> authService.logout("unknown-token")).doesNotThrowAnyException();
    }

}
