package com.jiubuntu.wms.biz.inventory.domain;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

/**
 * append-only — 수정/삭제 없음. BaseEntity를 상속하지 않음.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@EntityListeners(AuditingEntityListener.class)
@Table(name = "inventory_history")
public class InventoryHistory {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String lotNumber;

    private int quantityChange;

    private int quantityAfter;

    @Enumerated(EnumType.STRING)
    private InventoryHistoryTargetType targetType;

    private Long targetId;

    private String reason;

    @CreatedDate
    private LocalDateTime createdAt;

    private Long createdBy;

    public InventoryHistory(Company company, Warehouse warehouse, Location location, Product product,
                             String lotNumber, int quantityChange, int quantityAfter,
                             InventoryHistoryTargetType targetType, Long targetId, String reason, Long createdBy) {
        this.company = company;
        this.warehouse = warehouse;
        this.location = location;
        this.product = product;
        this.lotNumber = lotNumber;
        this.quantityChange = quantityChange;
        this.quantityAfter = quantityAfter;
        this.targetType = targetType;
        this.targetId = targetId;
        this.reason = reason;
        this.createdBy = createdBy;
    }

}
