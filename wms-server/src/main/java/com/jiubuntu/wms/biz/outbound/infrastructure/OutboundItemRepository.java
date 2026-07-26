package com.jiubuntu.wms.biz.outbound.infrastructure;

import com.jiubuntu.wms.biz.outbound.domain.OutboundItem;
import com.jiubuntu.wms.biz.outbound.infrastructure.custom.CustomOutboundItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface OutboundItemRepository extends JpaRepository<OutboundItem, Long>, CustomOutboundItemRepository {

    List<OutboundItem> findByOutboundIdAndActiveTrue(Long outboundId);

}
