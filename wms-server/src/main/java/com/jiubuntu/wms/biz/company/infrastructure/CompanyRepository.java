package com.jiubuntu.wms.biz.company.infrastructure;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.infrastructure.custom.CustomCompanyRepository;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyRepository extends JpaRepository<Company, Long>, CustomCompanyRepository {
}
