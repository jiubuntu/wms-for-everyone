package com.jiubuntu.wms.biz.auth.infrastructure.custom;

import com.jiubuntu.wms.biz.auth.domain.AuthToken;
import com.jiubuntu.wms.biz.auth.domain.AuthTokenType;
import com.jiubuntu.wms.biz.auth.domain.QAuthToken;
import com.jiubuntu.wms.biz.user.domain.User;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CustomAuthTokenRepositoryImpl implements CustomAuthTokenRepository {

    private static final QAuthToken authToken = QAuthToken.authToken;

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<AuthToken> findActiveByTokenAndType(String token, AuthTokenType type) {
        return Optional.ofNullable(
                queryFactory.selectFrom(authToken)
                        .where(authToken.token.eq(token), authToken.type.eq(type), activeEq())
                        .fetchOne()
        );
    }

    @Override
    public void deactivateAllByUserAndType(User user, AuthTokenType type) {
        queryFactory.update(authToken)
                .set(authToken.active, false)
                .where(authToken.user.id.eq(user.getId()), authToken.type.eq(type), activeEq())
                .execute();
    }

    private BooleanExpression activeEq() {
        return authToken.active.isTrue();
    }

}
