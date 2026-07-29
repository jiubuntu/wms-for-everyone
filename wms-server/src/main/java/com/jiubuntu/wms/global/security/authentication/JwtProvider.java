package com.jiubuntu.wms.global.security.authentication;

import com.jiubuntu.wms.biz.user.domain.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {

    private static final String CLAIM_COMPANY_ID = "companyId";
    private static final String CLAIM_ROLE = "role";
    private static final String CLAIM_WAREHOUSE_ID = "warehouseId";
    private static final String CLAIM_MUST_CHANGE_PASSWORD = "mustChangePassword";

    private final SecretKey secretKey;
    private final long accessTokenExpiration;

    public JwtProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpiration
    ) {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
        this.accessTokenExpiration = accessTokenExpiration;
    }

    public String generateAccessToken(AuthPrincipal principal) {
        Date now = new Date();
        Date expiration = new Date(now.getTime() + accessTokenExpiration);

        return Jwts.builder()
                .subject(String.valueOf(principal.getUserId()))
                .claim(CLAIM_COMPANY_ID, principal.getCompanyId())
                .claim(CLAIM_ROLE, principal.getRole().name())
                .claim(CLAIM_WAREHOUSE_ID, principal.getWarehouseId())
                .claim(CLAIM_MUST_CHANGE_PASSWORD, principal.isMustChangePassword())
                .issuedAt(now)
                .expiration(expiration)
                .signWith(secretKey)
                .compact();
    }

    public AuthPrincipal parseAccessToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(secretKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        Long userId = Long.valueOf(claims.getSubject());
        Long companyId = claims.get(CLAIM_COMPANY_ID, Long.class);
        UserRole role = UserRole.valueOf(claims.get(CLAIM_ROLE, String.class));
        Long warehouseId = claims.get(CLAIM_WAREHOUSE_ID, Long.class);
        boolean mustChangePassword = Boolean.TRUE.equals(claims.get(CLAIM_MUST_CHANGE_PASSWORD, Boolean.class));

        return new AuthPrincipal(userId, companyId, role, warehouseId, mustChangePassword);
    }

}
