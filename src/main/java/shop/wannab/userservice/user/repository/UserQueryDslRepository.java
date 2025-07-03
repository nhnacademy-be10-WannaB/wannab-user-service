package shop.wannab.userservice.user.repository;

import java.util.List;

public interface UserQueryDslRepository {
    List<Long> findUserIdsByBirthMonth(int month);
}
