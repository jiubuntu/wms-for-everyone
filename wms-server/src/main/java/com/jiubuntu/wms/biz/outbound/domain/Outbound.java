package com.jiubuntu.wms.biz.outbound.domain;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
import com.jiubuntu.wms.global.entity.BaseEntity;
import jakarta.persistence.Column;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbound")
public class Outbound extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "company_id")
    private Company company;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    private String customerName;

    @Enumerated(EnumType.STRING)
    private OutboundStatus status;

    private Long processedBy;

    @Column(columnDefinition = "TEXT")
    private String note;

    public Outbound(Company company, Warehouse warehouse, String customerName, String note) {
        this.company = company;
        this.warehouse = warehouse;
        this.customerName = customerName;
        this.note = note;
        this.status = OutboundStatus.PENDING;
    }

    public void complete(Long processedBy) {
        this.status = OutboundStatus.COMPLETED;
        this.processedBy = processedBy;
    }

    public void cancel() {
        this.status = OutboundStatus.CANCELLED;
    }

}
