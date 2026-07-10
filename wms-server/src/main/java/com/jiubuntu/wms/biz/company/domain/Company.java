package com.jiubuntu.wms.biz.company.domain;

import com.jiubuntu.wms.global.entity.BaseEntity;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "companies")
public class Company extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String name;

    private String businessNumber;

    @Enumerated(EnumType.STRING)
    private CompanyStatus status;

    public Company(String name, String businessNumber, CompanyStatus status) {
        this.name = name;
        this.businessNumber = businessNumber;
        this.status = status;
    }

    public void approve() {
        this.status = CompanyStatus.ACTIVE;
    }

    public void suspend() {
        this.status = CompanyStatus.SUSPENDED;
    }

}
