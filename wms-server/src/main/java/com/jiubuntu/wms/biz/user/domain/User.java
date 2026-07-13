package com.jiubuntu.wms.biz.user.domain;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
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
@Table(name = "users")
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    private String email;

    private String password;

    private String name;

    private String phone;

    @Enumerated(EnumType.STRING)
    private UserRole role;

    @Enumerated(EnumType.STRING)
    private UserStatus status;

    private LocalDateTime emailVerifiedAt;

    private int loginFailedCount;

    private LocalDateTime lockedUntil;

    public User(Company company, Warehouse warehouse, String email, String password, String name, String phone,
                 UserRole role, UserStatus status) {
        this.company = company;
        this.warehouse = warehouse;
        this.email = email;
        this.password = password;
        this.name = name;
        this.phone = phone;
        this.role = role;
        this.status = status;
        this.loginFailedCount = 0;
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

    public void resetLoginFailedCount() {
        this.loginFailedCount = 0;
    }

    public void changePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    public void withdraw() {
        this.status = UserStatus.INACTIVE;
        delete();
    }

}
