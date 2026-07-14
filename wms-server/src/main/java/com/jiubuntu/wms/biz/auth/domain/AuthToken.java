package com.jiubuntu.wms.biz.auth.domain;

import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "auth_tokens")
public class AuthToken extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String token;

    @Enumerated(EnumType.STRING)
    private AuthTokenType type;

    private LocalDateTime expiredAt;

    public AuthToken(User user, String token, AuthTokenType type, LocalDateTime expiredAt) {
        this.user = user;
        this.token = token;
        this.type = type;
        this.expiredAt = expiredAt;
    }

    public boolean isValid() {
        return isActive() && expiredAt.isAfter(LocalDateTime.now());
    }

}
