package com.jiubuntu.wms.biz.company.infrastructure.custom;

import com.jiubuntu.wms.biz.company.domain.CompanyFile;
import com.jiubuntu.wms.biz.company.domain.CompanyFileType;
import com.jiubuntu.wms.biz.company.domain.QCompanyFile;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CustomCompanyFileRepositoryImpl implements CustomCompanyFileRepository {

    private static final QCompanyFile companyFile = QCompanyFile.companyFile;

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<CompanyFile> findActiveByCompanyIdAndType(Long companyId, CompanyFileType type) {
        return Optional.ofNullable(
                queryFactory.selectFrom(companyFile)
                        .where(companyFile.company.id.eq(companyId), companyFile.type.eq(type), activeEq())
                        .fetchOne()
        );
    }

    private BooleanExpression activeEq() {
        return companyFile.active.isTrue();
    }

}
