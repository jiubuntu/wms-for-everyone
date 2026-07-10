package com.jiubuntu.wms.biz.company.infrastructure.custom;

import com.jiubuntu.wms.biz.company.domain.QCompany;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public class CustomCompanyRepositoryImpl implements CustomCompanyRepository {

    private static final QCompany company = QCompany.company;

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsActiveByBusinessNumber(String businessNumber) {
        Integer result = queryFactory
                .selectOne()
                .from(company)
                .where(company.businessNumber.eq(businessNumber), activeEq())
                .fetchFirst();
        return result != null;
    }

    private BooleanExpression activeEq() {
        return company.active.isTrue();
    }

}
