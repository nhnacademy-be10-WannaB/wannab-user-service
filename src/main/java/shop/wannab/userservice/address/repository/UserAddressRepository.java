package shop.wannab.userservice.address.repository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import shop.wannab.userservice.address.domain.entity.UserAddress;
import shop.wannab.userservice.user.domain.entity.User;

@Repository
public interface UserAddressRepository extends JpaRepository<UserAddress, Long> {
    Optional<UserAddress> findByUserAndAddressId(User user, Long addressId);

    long countByUser(User user);

    List<UserAddress> findAllByUser(User user);
}
