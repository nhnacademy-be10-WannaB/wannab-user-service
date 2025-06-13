package shop.wannab.userservice.user.domain.dto;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;


@Getter
@Setter
@AllArgsConstructor
public class CommonResponse<T> {
    private Status status;
    private List<T> date;
    private ErrorResponse error;

    public CommonResponse() {
        this.status = Status.SUCCESS;
        this.date = null;
        this.error = null;
    }
}
