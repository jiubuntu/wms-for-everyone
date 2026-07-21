package com.jiubuntu.wms.biz.productunit.infrastructure.custom;

import com.jiubuntu.wms.biz.productunit.domain.ProductUnit;
import com.jiubuntu.wms.biz.productunit.domain.QProductUnit;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomProductUnitRepositoryImpl implements CustomProductUnitRepository {

    private static final QProductUnit productUnit = QProductUnit.productUnit;

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsActiveByCompanyAndName(Long companyId, String name) {
        Integer result = queryFactory
                .selectOne()
                .from(productUnit)
                .where(productUnit.company.id.eq(companyId), productUnit.name.eq(name), activeEq())
                .fetchFirst();
        return result != null;
    }

    @Override
    public Optional<ProductUnit> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(productUnit)
                        .where(productUnit.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Page<ProductUnit> findActiveByCompany(Long companyId, Pageable pageable) {
        List<ProductUnit> content = queryFactory
                .selectFrom(productUnit)
                .where(productUnit.company.id.eq(companyId), activeEq())
                .orderBy(productUnit.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(productUnit.count())
                .from(productUnit)
                .where(productUnit.company.id.eq(companyId), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression activeEq() {
        return productUnit.active.isTrue();
    }

}
