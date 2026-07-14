package com.jiubuntu.wms.biz.auth.application;

import com.jiubuntu.wms.biz.auth.application.dto.command.AuthPasswordResetConfirmCommand;
import com.jiubuntu.wms.biz.auth.application.validator.PasswordResetConfirmValidator;
import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.domain.AuthTokenType;
import com.jiubuntu.wms.biz.auth.infrastructure.AuthTokenRepository;
import com.jiubuntu.wms.biz.user.application.UserService;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import com.jiubuntu.wms.global.infrastructure.EmailSender;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PasswordResetService {

    private static final String EMAIL_SUBJECT = "[모두의 WMS] 비밀번호 재설정 안내";

    private final UserService userService;
    private final AuthTokenRepository authTokenRepository;
    private final EmailSender emailSender;
    private final PasswordEncoder passwordEncoder;
    private final PasswordResetConfirmValidator passwordResetConfirmValidator;

    @Value("${auth.password-reset.token-expiration-minutes}")
    private long tokenExpirationMinutes;

    @Value("${app.frontend.base-url}")
    private String frontendBaseUrl;

    @Transactional
    public void requestReset(String email) {
        userService.findActiveByEmail(email).ifPresent(this::issueTokenAndSendEmail);
    }

    @Transactional
    public void confirmReset(AuthPasswordResetConfirmCommand command) {
        AuthToken resetToken = authTokenRepository.findActiveByTokenAndType(command.getToken(), AuthTokenType.PASSWORD_RESET)
                .orElseThrow(() -> new CommonException(ErrorCode.INVALID_PASSWORD_RESET_TOKEN));

        try {
            passwordResetConfirmValidator.validate(resetToken, command);
        } catch (CommonException e) {
            if (e.getErrorCode() == ErrorCode.EXPIRED_PASSWORD_RESET_TOKEN) {
                resetToken.delete();
            }
            throw e;
        }

        User user = resetToken.getUser();
        user.changePassword(passwordEncoder.encode(command.getNewPassword()));
        resetToken.delete();
        authTokenRepository.deactivateAllByUserAndType(user, AuthTokenType.REFRESH);
    }

    private void issueTokenAndSendEmail(User user) {
        String tokenValue = UUID.randomUUID().toString();
        LocalDateTime expiredAt = LocalDateTime.now().plusMinutes(tokenExpirationMinutes);
        AuthToken resetToken = new AuthToken(user, tokenValue, AuthTokenType.PASSWORD_RESET, expiredAt);
        authTokenRepository.save(resetToken);

        String link = frontendBaseUrl + "/reset-password?token=" + tokenValue;
        String body = "아래 링크를 클릭해 비밀번호를 재설정해주세요. 이 링크는 %d분간 유효합니다.\n\n%s"
                .formatted(tokenExpirationMinutes, link);
        emailSender.send(user.getEmail(), EMAIL_SUBJECT, body);
    }

}
