package com.jiubuntu.wms.biz.auth.application;

import com.jiubuntu.wms.biz.auth.application.dto.command.AuthPasswordResetConfirmCommand;
import com.jiubuntu.wms.biz.auth.application.validator.PasswordResetConfirmValidator;
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
import com.jiubuntu.wms.global.infrastructure.EmailSender;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PasswordResetServiceTest {

    private static final long TOKEN_EXPIRATION_MINUTES = 30L;
    private static final String FRONTEND_BASE_URL = "http://localhost:5173";

    @Mock
    private UserService userService;

    @Mock
    private AuthTokenRepository authTokenRepository;

    @Mock
    private EmailSender emailSender;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private PasswordResetConfirmValidator passwordResetConfirmValidator;

    @InjectMocks
    private PasswordResetService passwordResetService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(passwordResetService, "tokenExpirationMinutes", TOKEN_EXPIRATION_MINUTES);
        ReflectionTestUtils.setField(passwordResetService, "frontendBaseUrl", FRONTEND_BASE_URL);
    }

    private User activeUser() {
        Company company = new Company("테스트기업", "123-45-67890", CompanyStatus.ACTIVE);
        return new User(company, null, "user@test.com", "encoded-password",
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.ACTIVE, false);
    }

    @Test
    @DisplayName("가입된 이메일이면 재설정 토큰을 발급하고 링크가 담긴 이메일을 발송한다")
    void requestReset_existingUser_issuesTokenAndSendsEmail() {
        User user = activeUser();
        when(userService.findActiveByEmail("user@test.com")).thenReturn(Optional.of(user));

        passwordResetService.requestReset("user@test.com");

        ArgumentCaptor<AuthToken> tokenCaptor = ArgumentCaptor.forClass(AuthToken.class);
        verify(authTokenRepository).save(tokenCaptor.capture());

        AuthToken savedToken = tokenCaptor.getValue();
        assertThat(savedToken.getType()).isEqualTo(AuthTokenType.PASSWORD_RESET);
        assertThat(savedToken.getToken()).isNotBlank();
        assertThat(savedToken.getExpiredAt()).isAfter(LocalDateTime.now());

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq("user@test.com"), anyString(), bodyCaptor.capture());
        assertThat(bodyCaptor.getValue()).contains(FRONTEND_BASE_URL + "/reset-password?token=" + savedToken.getToken());
    }

    @Test
    @DisplayName("가입되지 않은 이메일이면 아무 것도 하지 않는다")
    void requestReset_userNotFound_doesNothing() {
        when(userService.findActiveByEmail(anyString())).thenReturn(Optional.empty());

        passwordResetService.requestReset("unknown@test.com");

        verifyNoInteractions(authTokenRepository, emailSender);
    }

    @Test
    @DisplayName("재설정 확정 성공 시 비밀번호가 변경되고 토큰은 소진되며 기존 리프레시 토큰이 전부 폐기된다")
    void confirmReset_success() {
        User user = activeUser();
        AuthToken resetToken = new AuthToken(user, "reset-token", AuthTokenType.PASSWORD_RESET, LocalDateTime.now().plusMinutes(30));
        AuthPasswordResetConfirmCommand command = new AuthPasswordResetConfirmCommand("reset-token", "newPassword1!", "newPassword1!");
        when(authTokenRepository.findActiveByTokenAndType("reset-token", AuthTokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(resetToken));
        when(passwordEncoder.encode("newPassword1!")).thenReturn("encoded-new-password");

        passwordResetService.confirmReset(command);

        assertThat(user.getPassword()).isEqualTo("encoded-new-password");
        assertThat(resetToken.isActive()).isFalse();
        verify(authTokenRepository).deactivateAllByUserAndType(user, AuthTokenType.REFRESH);
    }

    @Test
    @DisplayName("존재하지 않는 토큰이면 INVALID_PASSWORD_RESET_TOKEN 예외가 발생한다")
    void confirmReset_tokenNotFound() {
        when(authTokenRepository.findActiveByTokenAndType(anyString(), eq(AuthTokenType.PASSWORD_RESET)))
                .thenReturn(Optional.empty());
        AuthPasswordResetConfirmCommand command = new AuthPasswordResetConfirmCommand("unknown-token", "newPassword1!", "newPassword1!");

        assertThatThrownBy(() -> passwordResetService.confirmReset(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.INVALID_PASSWORD_RESET_TOKEN);
        verifyNoInteractions(passwordResetConfirmValidator);
    }

    @Test
    @DisplayName("만료된 토큰이면 비밀번호는 바꾸지 않지만 토큰 자체는 소진 처리해 재사용을 막는다")
    void confirmReset_expiredToken_deactivatesTokenButDoesNotChangePassword() {
        User user = activeUser();
        AuthToken resetToken = new AuthToken(user, "reset-token", AuthTokenType.PASSWORD_RESET, LocalDateTime.now().minusSeconds(1));
        AuthPasswordResetConfirmCommand command = new AuthPasswordResetConfirmCommand("reset-token", "newPassword1!", "newPassword1!");
        when(authTokenRepository.findActiveByTokenAndType("reset-token", AuthTokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(resetToken));
        doThrow(new CommonException(ErrorCode.EXPIRED_PASSWORD_RESET_TOKEN))
                .when(passwordResetConfirmValidator).validate(resetToken, command);

        assertThatThrownBy(() -> passwordResetService.confirmReset(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.EXPIRED_PASSWORD_RESET_TOKEN);
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(resetToken.isActive()).isFalse();
        verify(authTokenRepository, never()).deactivateAllByUserAndType(user, AuthTokenType.REFRESH);
    }

    @Test
    @DisplayName("새 비밀번호 확인이 틀린 경우(토큰 자체는 문제 없음) 토큰은 그대로 유효한 채 남아 재시도할 수 있다")
    void confirmReset_passwordMismatch_doesNotTouchTokenOrPassword() {
        User user = activeUser();
        AuthToken resetToken = new AuthToken(user, "reset-token", AuthTokenType.PASSWORD_RESET, LocalDateTime.now().plusMinutes(30));
        AuthPasswordResetConfirmCommand command = new AuthPasswordResetConfirmCommand("reset-token", "newPassword1!", "different1!");
        when(authTokenRepository.findActiveByTokenAndType("reset-token", AuthTokenType.PASSWORD_RESET))
                .thenReturn(Optional.of(resetToken));
        doThrow(new CommonException(ErrorCode.PASSWORD_MISMATCH))
                .when(passwordResetConfirmValidator).validate(resetToken, command);

        assertThatThrownBy(() -> passwordResetService.confirmReset(command))
                .isInstanceOf(CommonException.class)
                .extracting(e -> ((CommonException) e).getErrorCode())
                .isEqualTo(ErrorCode.PASSWORD_MISMATCH);
        assertThat(user.getPassword()).isEqualTo("encoded-password");
        assertThat(resetToken.isActive()).isTrue();

        verify(authTokenRepository, never()).deactivateAllByUserAndType(user, AuthTokenType.REFRESH);
    }

}
