package com.jiubuntu.wms.biz.product.domain;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.global.entity.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "products")
public class Product extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    private String skuCode;

    private String name;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private CommonCode category;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_type_id")
    private CommonCode storageType;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "base_unit_id")
    private ProductUnit baseUnit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sub_unit_id")
    private ProductUnit subUnit;

    private BigDecimal unitConversionRate;

    private boolean lotTracking;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Builder
    public Product(Company company, String skuCode, String name, CommonCode category, CommonCode storageType,
                    ProductUnit baseUnit, ProductUnit subUnit, BigDecimal unitConversionRate, boolean lotTracking,
                    String description) {
        this.company = company;
        this.skuCode = skuCode;
        this.name = name;
        this.category = category;
        this.storageType = storageType;
        this.baseUnit = baseUnit;
        this.subUnit = subUnit;
        this.unitConversionRate = unitConversionRate;
        this.lotTracking = lotTracking;
        this.description = description;
    }

    public void update(String name, CommonCode category, CommonCode storageType, ProductUnit baseUnit,
                        ProductUnit subUnit, BigDecimal unitConversionRate, boolean lotTracking, String description) {
        this.name = name;
        this.category = category;
        this.storageType = storageType;
        this.baseUnit = baseUnit;
        this.subUnit = subUnit;
        this.unitConversionRate = unitConversionRate;
        this.lotTracking = lotTracking;
        this.description = description;
    }

}
