package com.jiubuntu.wms.biz.company.application;

import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyFile;
import com.jiubuntu.wms.biz.company.domain.CompanyFileType;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyFileRepository;
import com.jiubuntu.wms.biz.company.infrastructure.CompanyRepository;
import com.jiubuntu.wms.global.infrastructure.S3Uploader;
import lombok.RequiredArgsConstructor;
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
    private final S3Uploader s3Uploader;

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
        String filePath = s3Uploader.upload(businessLicenseFile, BUSINESS_LICENSE_DIRECTORY);
        CompanyFile companyFile = new CompanyFile(
                company,
                CompanyFileType.BUSINESS_LICENSE,
                filePath,
                businessLicenseFile.getOriginalFilename(),
                businessLicenseFile.getContentType()
        );
        return companyFileRepository.save(companyFile);
    }

}
