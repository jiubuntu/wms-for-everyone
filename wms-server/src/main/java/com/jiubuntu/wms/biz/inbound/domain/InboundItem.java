package com.jiubuntu.wms.biz.inbound.domain;

import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.global.entity.BaseEntity;
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

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "inbound_items")
public class InboundItem extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_id")
    private Inbound inbound;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "unit_id")
    private ProductUnit unit;

    private int quantity;

    private String lotNumber;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    public InboundItem(Inbound inbound, Product product, ProductUnit unit, int quantity, String lotNumber,
                        LocalDate manufactureDate, LocalDate expiryDate) {
        this.inbound = inbound;
        this.product = product;
        this.unit = unit;
        this.quantity = quantity;
        this.lotNumber = lotNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
    }

}
