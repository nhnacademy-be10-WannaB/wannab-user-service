package shop.wannab.userservice.address.domain.dto.response;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserAddressResponse {
    private Long addressId;
    private String addressName;
    private String address;
    private String detailAddress;
}
