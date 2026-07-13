package com.jiubuntu.wms.biz.company.infrastructure.custom;

import com.jiubuntu.wms.biz.company.domain.CompanyFile;
import com.jiubuntu.wms.biz.company.domain.CompanyFileType;

import java.util.Optional;

public interface CustomCompanyFileRepository {

    Optional<CompanyFile> findActiveByCompanyIdAndType(Long companyId, CompanyFileType type);

}
