package com.jiubuntu.wms.biz.inbound.infrastructure;

import com.jiubuntu.wms.biz.inbound.domain.InboundItemLocation;
import com.jiubuntu.wms.biz.inbound.infrastructure.custom.CustomInboundItemLocationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboundItemLocationRepository extends JpaRepository<InboundItemLocation, Long>, CustomInboundItemLocationRepository {

    List<InboundItemLocation> findByInboundItemIdAndActiveTrue(Long inboundItemId);

    List<InboundItemLocation> findByInboundItemIdInAndActiveTrue(List<Long> inboundItemIds);

}
