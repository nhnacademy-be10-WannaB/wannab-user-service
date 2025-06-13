package shop.wannab.userservice.user.domain.dto;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
@AllArgsConstructor
public class CommonResponse<T> {
    @NotNull
    private Status status;
    private T data;
    private ErrorResponse error;

    // 응답할 값이 없는 경우
    public CommonResponse() {
        this.status = Status.SUCCESS;
        this.data = null;
        this.error = null;
    }
}
