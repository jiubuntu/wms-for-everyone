package com.jiubuntu.wms.biz.outbound.domain;

import com.jiubuntu.wms.biz.location.domain.Location;
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

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbound_item_locations")
public class OutboundItemLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "outbound_item_id")
    private OutboundItem outboundItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private String lotNumber;

    private int quantity;

    public OutboundItemLocation(OutboundItem outboundItem, Location location, String lotNumber, int quantity) {
        this.outboundItem = outboundItem;
        this.location = location;
        this.lotNumber = lotNumber;
        this.quantity = quantity;
    }

}
