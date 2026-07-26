package com.jiubuntu.wms.biz.outbound.infrastructure;

import com.jiubuntu.wms.biz.outbound.domain.OutboundItemLocation;
import com.jiubuntu.wms.biz.outbound.infrastructure.custom.CustomOutboundItemLocationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboundItemLocationRepository extends JpaRepository<OutboundItemLocation, Long>, CustomOutboundItemLocationRepository {

    List<OutboundItemLocation> findByOutboundItemIdInAndActiveTrue(List<Long> outboundItemIds);

}
