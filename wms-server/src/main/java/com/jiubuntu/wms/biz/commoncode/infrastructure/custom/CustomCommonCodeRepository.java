package com.jiubuntu.wms.biz.commoncode.infrastructure.custom;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface CustomCommonCodeRepository {

    boolean existsActiveByCompanyAndGroupAndCode(Long companyId, CommonCodeGroup groupCode, String code);

    Optional<CommonCode> findActiveById(Long id);

    Page<CommonCode> findActiveByGroupVisibleTo(CommonCodeGroup groupCode, Long companyId, Pageable pageable);

    List<CommonCode> findActiveByGroupVisibleTo(CommonCodeGroup groupCode, Long companyId);

}
