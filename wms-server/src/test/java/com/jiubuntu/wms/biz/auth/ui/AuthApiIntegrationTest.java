package com.jiubuntu.wms.biz.auth.ui;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.jiubuntu.wms.IntegrationTestSupport;
import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.domain.AuthTokenType;
import com.jiubuntu.wms.biz.auth.infrastructure.AuthTokenRepository;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
import com.jiubuntu.wms.biz.user.infrastructure.UserRepository;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import com.jiubuntu.wms.global.infrastructure.EmailSender;
import com.jiubuntu.wms.global.infrastructure.FileStorage;
import jakarta.servlet.http.Cookie;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.cookie;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@Transactional
class AuthApiIntegrationTest extends IntegrationTestSupport {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private AuthTokenRepository authTokenRepository;

    @MockitoBean
    private FileStorage fileStorage;

    @MockitoBean
    private EmailSender emailSender;

    @BeforeEach
    void setUp() {
        when(fileStorage.upload(any(), anyString())).thenReturn("company/business-license/dummy-key");
    }

    private User seedActiveUser(String email, String rawPassword) {
        Company company = companyRepository.save(new Company("로그인테스트기업", "111-11-11111", CompanyStatus.ACTIVE));
        User user = new User(company, null, email, passwordEncoder.encode(rawPassword),
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.ACTIVE);
        return userRepository.save(user);
    }

    private LoginTokens login(String email, String rawPassword) throws Exception {
        MvcResult result = mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", rawPassword))))
                .andExpect(status().isOk())
                .andReturn();
        String accessToken = objectMapper.readTree(result.getResponse().getContentAsString())
                .path("data").path("accessToken").asText();
        String refreshToken = result.getResponse().getCookie(RefreshTokenCookieProvider.COOKIE_NAME).getValue();
        return new LoginTokens(accessToken, refreshToken);
    }

    private String requestResetAndCaptureToken(String email) throws Exception {
        mockMvc.perform(post("/api/auth/password/reset-request")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", email))))
                .andExpect(status().isOk());

        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(emailSender).send(eq(email), anyString(), bodyCaptor.capture());
        String body = bodyCaptor.getValue();
        return body.substring(body.indexOf("token=") + "token=".length()).trim();
    }

    private static class LoginTokens {
        private final String accessToken;
        private final String refreshToken;

        private LoginTokens(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }

    @Test
    @DisplayName("회원가입 성공 시 계정은 PENDING 상태로 생성된다")
    void signup_success() throws Exception {
        MockMultipartFile file = new MockMultipartFile("businessLicenseFile", "license.pdf", "application/pdf", new byte[]{1, 2, 3});

        mockMvc.perform(multipart("/api/auth/signup")
                        .file(file)
                        .param("email", "signup@test.com")
                        .param("password", "password1!")
                        .param("passwordConfirm", "password1!")
                        .param("name", "홍길동")
                        .param("phone", "010-1234-5678")
                        .param("companyName", "신규기업")
                        .param("businessNumber", "222-22-22222"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.data.email").value("signup@test.com"))
                .andExpect(jsonPath("$.data.status").value("PENDING"));
    }

    @Test
    @DisplayName("이미 가입된 이메일로 회원가입하면 409 EMAIL_ALREADY_EXISTS를 반환한다")
    void signup_duplicateEmail() throws Exception {
        MockMultipartFile file = new MockMultipartFile("businessLicenseFile", "license.pdf", "application/pdf", new byte[]{1, 2, 3});
        seedActiveUser("duplicate@test.com", "password1!");

        mockMvc.perform(multipart("/api/auth/signup")
                        .file(file)
                        .param("email", "duplicate@test.com")
                        .param("password", "password1!")
                        .param("passwordConfirm", "password1!")
                        .param("name", "홍길동")
                        .param("phone", "010-1234-5678")
                        .param("companyName", "또다른기업")
                        .param("businessNumber", "333-33-33333"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.EMAIL_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("이미 등록된 사업자등록번호로 회원가입하면 409 BUSINESS_NUMBER_ALREADY_EXISTS를 반환한다")
    void signup_duplicateBusinessNumber() throws Exception {
        MockMultipartFile file = new MockMultipartFile("businessLicenseFile", "license.pdf", "application/pdf", new byte[]{1, 2, 3});
        companyRepository.save(new Company("선점기업", "444-44-44444", CompanyStatus.ACTIVE));

        mockMvc.perform(multipart("/api/auth/signup")
                        .file(file)
                        .param("email", "another@test.com")
                        .param("password", "password1!")
                        .param("passwordConfirm", "password1!")
                        .param("name", "홍길동")
                        .param("phone", "010-1234-5678")
                        .param("companyName", "중복기업")
                        .param("businessNumber", "444-44-44444"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.message").value(ErrorCode.BUSINESS_NUMBER_ALREADY_EXISTS.getMessage()));
    }

    @Test
    @DisplayName("로그인 성공 시 액세스 토큰과 리프레시 토큰 쿠키가 발급된다")
    void login_success() throws Exception {
        seedActiveUser("login@test.com", "password1!");

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", "login@test.com", "password", "password1!"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andExpect(cookie().exists(RefreshTokenCookieProvider.COOKIE_NAME));
    }

    @Test
    @DisplayName("아직 승인되지 않은(PENDING) 계정은 로그인할 수 없다")
    void login_pendingAccount() throws Exception {
        Company company = companyRepository.save(new Company("승인대기기업", "555-55-55555", CompanyStatus.PENDING));
        userRepository.save(new User(company, null, "pending@test.com", passwordEncoder.encode("password1!"),
                "홍길동", "010-0000-0000", UserRole.COMPANY_ADMIN, UserStatus.PENDING));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", "pending@test.com", "password", "password1!"))))
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.message").value(ErrorCode.ACCOUNT_NOT_ACTIVE.getMessage()));
    }

    @Test
    @DisplayName("로그인 5회 실패 시 계정이 잠기고, 이후에는 올바른 비밀번호로도 로그인할 수 없다")
    void login_locksAfterMaxFailCount() throws Exception {
        seedActiveUser("lockme@test.com", "password1!");

        for (int i = 0; i < 5; i++) {
            mockMvc.perform(post("/api/auth/login")
                            .contentType("application/json")
                            .content(objectMapper.writeValueAsString(Map.of("email", "lockme@test.com", "password", "wrong-password"))))
                    .andExpect(status().isUnauthorized())
                    .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_CREDENTIALS.getMessage()));
        }

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", "lockme@test.com", "password", "password1!"))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.ACCOUNT_LOCKED.getMessage()));
    }

    @Test
    @DisplayName("리프레시 토큰으로 액세스 토큰을 재발급받고, 사용된 토큰은 재사용할 수 없다")
    void refresh_rotatesToken() throws Exception {
        seedActiveUser("refresh@test.com", "password1!");
        String oldRefreshToken = login("refresh@test.com", "password1!").refreshToken;

        MvcResult refreshResult = mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(RefreshTokenCookieProvider.COOKIE_NAME, oldRefreshToken)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.accessToken").isNotEmpty())
                .andReturn();
        String newRefreshToken = refreshResult.getResponse().getCookie(RefreshTokenCookieProvider.COOKIE_NAME).getValue();
        assertThat(newRefreshToken).isNotEqualTo(oldRefreshToken);

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(RefreshTokenCookieProvider.COOKIE_NAME, oldRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REFRESH_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("리프레시 토큰 쿠키 없이 재발급을 요청하면 401 INVALID_REFRESH_TOKEN을 반환한다")
    void refresh_withoutCookie_returnsInvalidRefreshToken() throws Exception {
        mockMvc.perform(post("/api/auth/refresh"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REFRESH_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("로그아웃하면 리프레시 토큰 쿠키가 만료되고 해당 토큰으로는 더 이상 재발급받을 수 없다")
    void logout_invalidatesRefreshToken() throws Exception {
        seedActiveUser("logout@test.com", "password1!");
        LoginTokens tokens = login("logout@test.com", "password1!");

        mockMvc.perform(post("/api/auth/logout")
                        .header("Authorization", "Bearer " + tokens.accessToken)
                        .cookie(new Cookie(RefreshTokenCookieProvider.COOKIE_NAME, tokens.refreshToken)))
                .andExpect(status().isOk())
                .andExpect(cookie().maxAge(RefreshTokenCookieProvider.COOKIE_NAME, 0));

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(RefreshTokenCookieProvider.COOKIE_NAME, tokens.refreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REFRESH_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("가입된 이메일로 재설정을 요청하면 재설정 링크가 담긴 이메일이 발송된다")
    void passwordReset_requestForExistingUser_sendsEmail() throws Exception {
        seedActiveUser("hasaccount@test.com", "password1!");

        String token = requestResetAndCaptureToken("hasaccount@test.com");

        assertThat(token).isNotBlank();
    }

    @Test
    @DisplayName("가입되지 않은 이메일로 재설정을 요청해도 동일하게 200을 반환하고 이메일은 발송되지 않는다")
    void passwordReset_requestForUnknownEmail_stillReturnsSuccess() throws Exception {
        mockMvc.perform(post("/api/auth/password/reset-request")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", "unknown@test.com"))))
                .andExpect(status().isOk());

        verifyNoInteractions(emailSender);
    }

    @Test
    @DisplayName("비밀번호 재설정 성공 시 새 비밀번호로 로그인 가능하고 기존 리프레시 토큰은 모두 폐기된다")
    void passwordReset_confirmSuccess_changesPasswordAndRevokesSessions() throws Exception {
        seedActiveUser("reset@test.com", "password1!");
        String oldRefreshToken = login("reset@test.com", "password1!").refreshToken;

        String token = requestResetAndCaptureToken("reset@test.com");

        mockMvc.perform(post("/api/auth/password/reset-confirm")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "newPassword", "newPassword1!",
                                "newPasswordConfirm", "newPassword1!"
                        ))))
                .andExpect(status().isOk());

        mockMvc.perform(post("/api/auth/refresh")
                        .cookie(new Cookie(RefreshTokenCookieProvider.COOKIE_NAME, oldRefreshToken)))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_REFRESH_TOKEN.getMessage()));

        mockMvc.perform(post("/api/auth/login")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of("email", "reset@test.com", "password", "newPassword1!"))))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("존재하지 않는 토큰으로 재설정을 시도하면 401 INVALID_PASSWORD_RESET_TOKEN을 반환한다")
    void passwordReset_confirmWithInvalidToken() throws Exception {
        mockMvc.perform(post("/api/auth/password/reset-confirm")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "no-such-token",
                                "newPassword", "newPassword1!",
                                "newPasswordConfirm", "newPassword1!"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PASSWORD_RESET_TOKEN.getMessage()));
    }

    @Test
    @DisplayName("새 비밀번호와 확인이 다르면 400 PASSWORD_MISMATCH를 반환한다")
    void passwordReset_confirmWithMismatchedPasswords() throws Exception {
        seedActiveUser("mismatch@test.com", "password1!");
        String token = requestResetAndCaptureToken("mismatch@test.com");

        mockMvc.perform(post("/api/auth/password/reset-confirm")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", token,
                                "newPassword", "newPassword1!",
                                "newPasswordConfirm", "different1!"
                        ))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(ErrorCode.PASSWORD_MISMATCH.getMessage()));
    }

    @Test
    @DisplayName("만료된 토큰으로 재설정을 시도하면 401 EXPIRED_PASSWORD_RESET_TOKEN을 반환한다")
    void passwordReset_confirmWithExpiredToken() throws Exception {
        User user = seedActiveUser("expired@test.com", "password1!");
        authTokenRepository.save(new AuthToken(user, "expired-reset-token", AuthTokenType.PASSWORD_RESET, LocalDateTime.now().minusSeconds(1)));

        mockMvc.perform(post("/api/auth/password/reset-confirm")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "expired-reset-token",
                                "newPassword", "newPassword1!",
                                "newPasswordConfirm", "newPassword1!"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.EXPIRED_PASSWORD_RESET_TOKEN.getMessage()));

        // 만료가 감지된 토큰은 그 자리에서 소진 처리되어, 같은 토큰으로 재시도하면 이제 "존재하지 않는 토큰" 취급된다
        mockMvc.perform(post("/api/auth/password/reset-confirm")
                        .contentType("application/json")
                        .content(objectMapper.writeValueAsString(Map.of(
                                "token", "expired-reset-token",
                                "newPassword", "newPassword1!",
                                "newPasswordConfirm", "newPassword1!"
                        ))))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value(ErrorCode.INVALID_PASSWORD_RESET_TOKEN.getMessage()));
    }

}
