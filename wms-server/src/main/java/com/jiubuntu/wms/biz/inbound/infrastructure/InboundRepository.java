package com.jiubuntu.wms.biz.inbound.infrastructure;

import com.jiubuntu.wms.biz.inbound.domain.Inbound;
import com.jiubuntu.wms.biz.inbound.infrastructure.custom.CustomInboundRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InboundRepository extends JpaRepository<Inbound, Long>, CustomInboundRepository {
}
