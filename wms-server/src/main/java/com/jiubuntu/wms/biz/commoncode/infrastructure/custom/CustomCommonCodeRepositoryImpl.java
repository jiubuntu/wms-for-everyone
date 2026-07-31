package com.jiubuntu.wms.biz.commoncode.infrastructure.custom;

import com.jiubuntu.wms.biz.commoncode.domain.CommonCode;
import com.jiubuntu.wms.biz.commoncode.domain.CommonCodeGroup;
import com.jiubuntu.wms.biz.commoncode.domain.QCommonCode;
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
public class CustomCommonCodeRepositoryImpl implements CustomCommonCodeRepository {

    private static final QCommonCode commonCode = QCommonCode.commonCode;

    private final JPAQueryFactory queryFactory;

    @Override
    public boolean existsActiveByCompanyAndGroupAndCode(Long companyId, CommonCodeGroup groupCode, String code) {
        Integer result = queryFactory
                .selectOne()
                .from(commonCode)
                .where(
                        commonCode.groupCode.eq(groupCode),
                        commonCode.code.eq(code),
                        companyEq(companyId),
                        activeEq()
                )
                .fetchFirst();
        return result != null;
    }

    @Override
    public Optional<CommonCode> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(commonCode)
                        .where(commonCode.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public Page<CommonCode> findActiveByGroupVisibleTo(CommonCodeGroup groupCode, Long companyId, String keyword, Pageable pageable) {
        List<CommonCode> content = queryFactory
                .selectFrom(commonCode)
                .where(
                        commonCode.groupCode.eq(groupCode),
                        companyScopeEq(companyId),
                        keywordMatches(keyword),
                        activeEq()
                )
                .orderBy(commonCode.sortOrder.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(commonCode.count())
                .from(commonCode)
                .where(
                        commonCode.groupCode.eq(groupCode),
                        companyScopeEq(companyId),
                        keywordMatches(keyword),
                        activeEq()
                )
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    @Override
    public List<CommonCode> findActiveByGroupVisibleTo(CommonCodeGroup groupCode, Long companyId) {
        return queryFactory
                .selectFrom(commonCode)
                .where(
                        commonCode.groupCode.eq(groupCode),
                        companyScopeEq(companyId),
                        activeEq()
                )
                .orderBy(commonCode.sortOrder.asc())
                .fetch();
    }

    private BooleanExpression keywordMatches(String keyword) {
        if (!StringUtils.hasText(keyword)) {
            return null;
        }
        return commonCode.code.containsIgnoreCase(keyword).or(commonCode.name.containsIgnoreCase(keyword));
    }

    private BooleanExpression companyEq(Long companyId) {
        return companyId == null ? commonCode.company.id.isNull() : commonCode.company.id.eq(companyId);
    }

    private BooleanExpression companyScopeEq(Long companyId) {
        return companyId == null ? commonCode.company.id.isNull() : commonCode.company.id.isNull().or(commonCode.company.id.eq(companyId));
    }

    private BooleanExpression activeEq() {
        return commonCode.active.isTrue();
    }

}
