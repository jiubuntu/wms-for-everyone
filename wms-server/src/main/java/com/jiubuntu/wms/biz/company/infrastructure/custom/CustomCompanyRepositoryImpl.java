package com.jiubuntu.wms.biz.company.infrastructure.custom;

import com.jiubuntu.wms.biz.company.application.dto.result.CompanyResult;
import com.jiubuntu.wms.biz.company.domain.Company;
import com.jiubuntu.wms.biz.company.domain.CompanyStatus;
import com.jiubuntu.wms.biz.company.domain.QCompany;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

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

    @Override
    public Optional<Company> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(company)
                        .where(company.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Page<CompanyResult> findPendingList(Pageable pageable) {
        List<CompanyResult> content = queryFactory
                .select(Projections.constructor(
                        CompanyResult.class,
                        company.id,
                        company.name,
                        company.businessNumber,
                        company.status,
                        company.createdAt
                ))
                .from(company)
                .where(company.status.eq(CompanyStatus.PENDING), activeEq())
                .orderBy(company.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(company.count())
                .from(company)
                .where(company.status.eq(CompanyStatus.PENDING), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression activeEq() {
        return company.active.isTrue();
    }

}
