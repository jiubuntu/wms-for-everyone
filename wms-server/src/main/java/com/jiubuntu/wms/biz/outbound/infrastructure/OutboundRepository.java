package com.jiubuntu.wms.biz.outbound.infrastructure;

import com.jiubuntu.wms.biz.outbound.domain.Outbound;
import com.jiubuntu.wms.biz.outbound.infrastructure.custom.CustomOutboundRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface OutboundRepository extends JpaRepository<Outbound, Long>, CustomOutboundRepository {
}
