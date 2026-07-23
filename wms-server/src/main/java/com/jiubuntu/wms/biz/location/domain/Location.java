package com.jiubuntu.wms.biz.location.domain;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
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
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "locations")
public class Location extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "warehouse_id")
    private Warehouse warehouse;

    private String zone;

    private String row;

    private String col;

    private String level;

    private String code;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "storage_type_id")
    private CommonCode storageType;

    @Enumerated(EnumType.STRING)
    private LocationStatus status;

    @Builder
    public Location(Warehouse warehouse, String zone, String row, String col, String level, String code,
                     CommonCode storageType) {
        this.warehouse = warehouse;
        this.zone = zone;
        this.row = row;
        this.col = col;
        this.level = level;
        this.code = code;
        this.storageType = storageType;
        this.status = LocationStatus.ACTIVE;
    }

    public void update(CommonCode storageType, LocationStatus status) {
        this.storageType = storageType;
        this.status = status;
    }

}
