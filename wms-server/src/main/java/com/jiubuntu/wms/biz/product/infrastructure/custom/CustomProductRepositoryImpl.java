package com.jiubuntu.wms.biz.product.infrastructure.custom;

import com.jiubuntu.wms.biz.product.domain.Product;
import com.jiubuntu.wms.biz.product.domain.QProduct;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Optional;

@RequiredArgsConstructor
public class CustomProductRepositoryImpl implements CustomProductRepository {

    private static final QProduct product = QProduct.product;

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsActiveByCompanyAndSkuCode(Long companyId, String skuCode) {
        Integer result = queryFactory
                .selectOne()
                .from(product)
                .where(product.company.id.eq(companyId), product.skuCode.eq(skuCode), activeEq())
                .fetchFirst();
        return result != null;
    }

    @Override
    public Optional<Product> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(product)
                        .where(product.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Page<Product> findActiveByCompany(Long companyId, String keyword, Pageable pageable) {
        List<Product> content = queryFactory
                .selectFrom(product)
                .where(product.company.id.eq(companyId), keywordMatches(keyword), activeEq())
                .orderBy(product.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(product.count())
                .from(product)
                .where(product.company.id.eq(companyId), keywordMatches(keyword), activeEq())
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression keywordMatches(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return product.skuCode.containsIgnoreCase(keyword).or(product.name.containsIgnoreCase(keyword));
    }

    @Override
    public List<Product> findAllActiveByCompany(Long companyId) {
        return queryFactory.selectFrom(product)
                .where(product.company.id.eq(companyId), activeEq())
                .orderBy(product.createdAt.asc())
                .fetch();
    }

    @Override
    public long countActiveByCompany(Long companyId) {
        Long count = queryFactory
                .select(product.count())
                .from(product)
                .where(product.company.id.eq(companyId), activeEq())
                .fetchOne();
        return count != null ? count : 0L;
    }

    private BooleanExpression activeEq() {
        return product.active.isTrue();
    }

}
