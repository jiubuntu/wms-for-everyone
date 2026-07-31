package com.jiubuntu.wms.biz.company.infrastructure;

import com.jiubuntu.wms.biz.company.domain.CompanyFile;
import com.jiubuntu.wms.biz.company.infrastructure.custom.CustomCompanyFileRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyFileRepository extends JpaRepository<CompanyFile, Long>, CustomCompanyFileRepository {
}
