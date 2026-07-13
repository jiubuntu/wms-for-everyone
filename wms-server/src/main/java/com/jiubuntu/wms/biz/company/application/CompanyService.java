package com.jiubuntu.wms.biz.company.application;

import com.jiubuntu.wms.biz.company.application.dto.result.CompanyResult;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyFile;
import com.jiubuntu.wms.biz.company.domain.CompanyFileType;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyFileRepository;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.global.exception.CommonException;
import com.jiubuntu.wms.global.exception.constants.ErrorCode;
import com.jiubuntu.wms.global.infrastructure.FileStorage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CompanyService {

    private static final String BUSINESS_LICENSE_DIRECTORY = "company/business-license";

    private final CompanyRepository companyRepository;
    private final CompanyFileRepository companyFileRepository;
    private final FileStorage fileStorage;

    @Transactional
    public Company create(String name, String businessNumber) {
        Company company = new Company(name, businessNumber, CompanyStatus.PENDING);
        return companyRepository.save(company);
    }

    public boolean existsByBusinessNumber(String businessNumber) {
        return companyRepository.existsActiveByBusinessNumber(businessNumber);
    }

    @Transactional
    public CompanyFile addBusinessLicenseFile(Company company, MultipartFile businessLicenseFile) {
        String filePath = fileStorage.upload(businessLicenseFile, BUSINESS_LICENSE_DIRECTORY);
        CompanyFile companyFile = new CompanyFile(
                company,
                CompanyFileType.BUSINESS_LICENSE,
                filePath,
                businessLicenseFile.getOriginalFilename(),
                businessLicenseFile.getContentType()
        );
        return companyFileRepository.save(companyFile);
    }

    public Company getActiveById(Long companyId) {
        return companyRepository.findActiveById(companyId)
                .orElseThrow(() -> new CommonException(ErrorCode.COMPANY_NOT_FOUND));
    }

    public Page<CompanyResult> findPendingList(Pageable pageable) {
        return companyRepository.findPendingList(pageable);
    }

    public String getBusinessLicenseFileUrl(Long companyId) {
        CompanyFile companyFile = companyFileRepository.findActiveByCompanyIdAndType(companyId, CompanyFileType.BUSINESS_LICENSE)
                .orElseThrow(() -> new CommonException(ErrorCode.COMPANY_FILE_NOT_FOUND));
        return fileStorage.getUrl(companyFile.getFilePath());
    }

    @Transactional
    public Company approve(Long companyId, Long updatedBy) {
        Company company = getActiveById(companyId);
        if (company.getStatus() != CompanyStatus.PENDING) {
            throw new CommonException(ErrorCode.COMPANY_NOT_PENDING);
        }
        company.approve();
        company.assignUpdater(updatedBy);
        return company;
    }

}
