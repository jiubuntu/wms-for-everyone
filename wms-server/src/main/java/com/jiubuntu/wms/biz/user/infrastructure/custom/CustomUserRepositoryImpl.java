package com.jiubuntu.wms.biz.user.infrastructure.custom;

import com.jiubuntu.wms.biz.user.domain.QUser;
import com.jiubuntu.wms.biz.user.domain.User;
import com.jiubuntu.wms.biz.user.domain.UserRole;
import com.jiubuntu.wms.biz.user.domain.UserStatus;
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
public class CustomUserRepositoryImpl implements CustomUserRepository {

    private static final QUser user = QUser.user;

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<User> findActiveByEmail(String email) {
        return Optional.ofNullable(
                queryFactory.selectFrom(user)
                        .where(user.email.eq(email), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public boolean existsActiveByEmail(String email) {
        Integer result = queryFactory
                .selectOne()
                .from(user)
                .where(user.email.eq(email), activeEq())
                .fetchFirst();
        return result != null;
    }

    @Override
    public Optional<User> findActiveById(Long id) {
        return Optional.ofNullable(
                queryFactory.selectFrom(user)
                        .where(user.id.eq(id), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public long countActiveByCompanyIdAndRole(Long companyId, UserRole role) {
        Long count = queryFactory
                .select(user.count())
                .from(user)
                .where(user.company.id.eq(companyId), user.role.eq(role), activeEq())
                .fetchOne();
        return count != null ? count : 0L;
    }

    @Override
    public Optional<User> findPendingByCompanyIdAndRole(Long companyId, UserRole role) {
        return Optional.ofNullable(
                queryFactory.selectFrom(user)
                        .where(user.company.id.eq(companyId), user.role.eq(role), user.status.eq(UserStatus.PENDING), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public boolean existsActiveByWarehouseId(Long warehouseId) {
        Integer result = queryFactory
                .selectOne()
                .from(user)
                .where(user.warehouse.id.eq(warehouseId), activeEq())
                .fetchFirst();
        return result != null;
    }

    @Override
    public Page<User> findActiveStaffByCompany(Long companyId, Long warehouseId, String keyword, Pageable pageable) {
        BooleanExpression condition = user.company.id.eq(companyId)
                .and(user.role.in(UserRole.WAREHOUSE_MANAGER, UserRole.WORKER))
                .and(activeEq());
        if (warehouseId != null) {
            condition = condition.and(user.warehouse.id.eq(warehouseId));
        }
        if (StringUtils.hasText(keyword)) {
            condition = condition.and(user.name.containsIgnoreCase(keyword).or(user.email.containsIgnoreCase(keyword)));
        }

        List<User> content = queryFactory
                .selectFrom(user)
                .where(condition)
                .orderBy(user.createdAt.asc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        Long total = queryFactory
                .select(user.count())
                .from(user)
                .where(condition)
                .fetchOne();

        return new PageImpl<>(content, pageable, total != null ? total : 0L);
    }

    private BooleanExpression activeEq() {
        return user.active.isTrue();
    }

}
