package com.jiubuntu.wms.biz.inventory.infrastructure;

import com.jiubuntu.wms.biz.inventory.domain.Transfer;
import com.jiubuntu.wms.biz.inventory.infrastructure.custom.CustomTransferRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransferRepository extends JpaRepository<Transfer, Long>, CustomTransferRepository {
}
