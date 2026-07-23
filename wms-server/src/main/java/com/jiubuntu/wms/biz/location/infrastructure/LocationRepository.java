package com.jiubuntu.wms.biz.location.infrastructure;

import com.jiubuntu.wms.biz.location.domain.Location;
import com.jiubuntu.wms.biz.location.infrastructure.custom.CustomLocationRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LocationRepository extends JpaRepository<Location, Long>, CustomLocationRepository {
}
