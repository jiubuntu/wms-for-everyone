package com.jiubuntu.wms.domain.user.domain;

import com.jiubuntu.wms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private Long companyId;

    private Long warehouseId;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Column(nullable = false)
    private String name;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private UserStatus status;

    private LocalDateTime emailVerifiedAt;

    @Column(nullable = false)
    private int loginFailedCount;

    private LocalDateTime lockedUntil;

    private User(Long companyId, Long warehouseId, String email, String password, String name,
                  UserRole role, UserStatus status) {
        this.companyId = companyId;
        this.warehouseId = warehouseId;
        this.email = email;
        this.password = password;
        this.name = name;
        this.role = role;
        this.status = status;
        this.loginFailedCount = 0;
    }

    public static User create(Long companyId, Long warehouseId, String email, String password, String name,
                               UserRole role, UserStatus status) {
        return new User(companyId, warehouseId, email, password, name, role, status);
    }

    public void verifyEmail() {
        this.emailVerifiedAt = LocalDateTime.now();
    }

    public void approve() {
        this.status = UserStatus.ACTIVE;
    }

    public void lock(LocalDateTime lockedUntil) {
        this.status = UserStatus.LOCKED;
        this.lockedUntil = lockedUntil;
    }

    public void unlock() {
        this.status = UserStatus.ACTIVE;
        this.lockedUntil = null;
        this.loginFailedCount = 0;
    }

    public void increaseLoginFailedCount() {
        this.loginFailedCount++;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

}
