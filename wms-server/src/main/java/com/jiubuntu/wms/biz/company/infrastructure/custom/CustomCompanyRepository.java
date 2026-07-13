package com.jiubuntu.wms.biz.company.infrastructure.custom;

import com.jiubuntu.wms.biz.company.application.dto.result.CompanyResult;
import com.jiubuntu.wms.biz.company.domain.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

public interface CustomCompanyRepository {

    boolean existsActiveByBusinessNumber(String businessNumber);

    Optional<Company> findActiveById(Long id);

    Page<CompanyResult> findPendingList(Pageable pageable);

}
