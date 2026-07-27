package com.jiubuntu.wms.biz.inbound.domain;

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
@Table(name = "inbound_item_locations")
public class InboundItemLocation extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "inbound_item_id")
    private InboundItem inboundItem;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "location_id")
    private Location location;

    private int quantity;

    public InboundItemLocation(InboundItem inboundItem, Location location, int quantity) {
        this.inboundItem = inboundItem;
        this.location = location;
        this.quantity = quantity;
    }

}
