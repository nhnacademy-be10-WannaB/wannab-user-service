package shop.wannab.userservice.user.repository.Impl;

import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.transaction.annotation.Transactional;
import shop.wannab.userservice.user.domain.entity.QUser;
import shop.wannab.userservice.user.repository.UserQueryDslRepository;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class UserQueryDslRepositoryImpl implements UserQueryDslRepository {

    private final JPAQueryFactory queryFactory;

    @Override
    public List<Long> findUserIdsByBirthMonth(int month) {
        QUser user = QUser.user;
        return queryFactory
                .select(user.userId)
                .from(user)
                .where(
                        Expressions.numberTemplate(Integer.class, "MONTH({0})", user.birth).eq(month)
                )
                .fetch();
    }
}
