package com.jiubuntu.wms.biz.user.infrastructure.custom;

import com.jiubuntu.wms.biz.user.domain.QUser;
import com.jiubuntu.wms.biz.user.domain.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

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

    private BooleanExpression activeEq() {
        return user.active.isTrue();
    }

}
