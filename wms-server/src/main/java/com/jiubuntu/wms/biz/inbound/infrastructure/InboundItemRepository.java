package com.jiubuntu.wms.biz.inbound.infrastructure;

import com.jiubuntu.wms.biz.inbound.domain.InboundItem;
import com.jiubuntu.wms.biz.inbound.infrastructure.custom.CustomInboundItemRepository;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InboundItemRepository extends JpaRepository<InboundItem, Long>, CustomInboundItemRepository {

    List<InboundItem> findByInboundIdAndActiveTrue(Long inboundId);

}
