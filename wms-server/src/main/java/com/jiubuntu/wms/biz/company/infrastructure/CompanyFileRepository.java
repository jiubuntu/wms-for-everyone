package com.jiubuntu.wms.biz.company.infrastructure;

import com.jiubuntu.wms.biz.company.domain.CompanyFile;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CompanyFileRepository extends JpaRepository<CompanyFile, Long> {
}
