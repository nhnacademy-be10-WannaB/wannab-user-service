package shop.wannab.userservice.address.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class UserAddressCreateRequest {
    @Size(max = 30)
    private String addressName;
    @NotNull
    @Size(min = 1, max = 100)
    private String address;
    @Size(max = 100)
    private String detailAddress;
}
