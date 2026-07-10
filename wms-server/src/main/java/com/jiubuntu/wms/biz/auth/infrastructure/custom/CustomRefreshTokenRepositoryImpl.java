package com.jiubuntu.wms.biz.auth.infrastructure.custom;

import com.jiubuntu.wms.biz.auth.domain.QRefreshToken;
import com.jiubuntu.wms.biz.auth.domain.RefreshToken;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;

import java.util.Optional;

@RequiredArgsConstructor
public class CustomRefreshTokenRepositoryImpl implements CustomRefreshTokenRepository {

    private static final QRefreshToken refreshToken = QRefreshToken.refreshToken;

    private final JPAQueryFactory queryFactory;

    @Override
    public Optional<RefreshToken> findActiveByToken(String token) {
        return Optional.ofNullable(
                queryFactory.selectFrom(refreshToken)
                        .where(refreshToken.token.eq(token), activeEq())
                        .fetchOne()
        );
    }

    private BooleanExpression activeEq() {
        return refreshToken.active.isTrue();
    }

}
