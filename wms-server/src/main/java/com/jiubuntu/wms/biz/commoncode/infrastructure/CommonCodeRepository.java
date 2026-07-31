package com.jiubuntu.wms.biz.commoncode.infrastructure;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.infrastructure.custom.CustomCommonCodeRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CommonCodeRepository extends JpaRepository<CommonCode, Long>, CustomCommonCodeRepository {
}
