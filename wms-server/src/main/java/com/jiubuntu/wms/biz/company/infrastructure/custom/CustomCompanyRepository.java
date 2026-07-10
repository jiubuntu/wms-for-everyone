package com.jiubuntu.wms.biz.company.infrastructure.custom;

public interface CustomCompanyRepository {

    boolean existsActiveByBusinessNumber(String businessNumber);

}
