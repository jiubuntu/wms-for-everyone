package com.jiubuntu.wms.biz.inventory.domain;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.warehouse.domain.Warehouse;
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
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "transfers")
public class Transfer extends BaseEntity {

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
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "from_location_id")
    private Location fromLocation;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "to_location_id")
    private Location toLocation;

    private String lotNumber;

    private int quantity;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reason_id")
    private CommonCode reason;

    @Column(columnDefinition = "TEXT")
    private String note;

    public Transfer(Company company, Warehouse warehouse, Product product, Location fromLocation,
                     Location toLocation, String lotNumber, int quantity, CommonCode reason, String note) {
        this.company = company;
        this.warehouse = warehouse;
        this.product = product;
        this.fromLocation = fromLocation;
        this.toLocation = toLocation;
        this.lotNumber = lotNumber;
        this.quantity = quantity;
        this.reason = reason;
        this.note = note;
    }

}
