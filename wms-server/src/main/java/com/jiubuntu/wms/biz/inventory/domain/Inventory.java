package com.jiubuntu.wms.biz.inventory.domain;

import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.global.entity.BaseEntity;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "inventory")
public class Inventory extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id")
    private Product product;

    private String lotNumber;

    private LocalDate manufactureDate;

    private LocalDate expiryDate;

    private int quantity;

    private int reservedQuantity;

    @Version
    private Long version;

    public Inventory(Location location, Product product, String lotNumber, LocalDate manufactureDate,
                      LocalDate expiryDate, int quantity) {
        this.location = location;
        this.product = product;
        this.lotNumber = lotNumber;
        this.manufactureDate = manufactureDate;
        this.expiryDate = expiryDate;
        this.quantity = quantity;
        this.reservedQuantity = 0;
    }

    public int getAvailableQuantity() {
        return quantity - reservedQuantity;
    }

    public void increaseQuantity(int amount) {
        this.quantity += amount;
    }

    public void decreaseQuantity(int amount) {
        this.quantity -= amount;
    }

    public void reserve(int amount) {
        this.reservedQuantity += amount;
    }

    public void releaseReservation(int amount) {
        this.reservedQuantity -= amount;
    }

    public void confirmReservation(int amount) {
        this.quantity -= amount;
        this.reservedQuantity -= amount;
    }

    public void adjustTo(int newQuantity) {
        this.quantity = newQuantity;
    }

}
