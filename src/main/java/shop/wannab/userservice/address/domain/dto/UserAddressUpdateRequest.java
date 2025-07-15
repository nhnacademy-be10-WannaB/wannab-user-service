package shop.wannab.userservice.address.domain.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;

@Builder
@AllArgsConstructor
@Data
public class UserAddressUpdateRequest {
    @Size(max = 30)
    private String addressName;
    @NotNull
    @Size(min = 1, max = 100)
    private String address;
    @Size(max = 100)
    private String detailAddress;
}
