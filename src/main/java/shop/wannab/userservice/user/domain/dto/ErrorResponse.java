package shop.wannab.userservice.user.domain.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ErrorResponse {
    private int status;
    private String code;
    private String message;

    public ErrorResponse() {
        this.status = 400;
        this.code = "미정";
        this.message = "유효하지 않은 요청입니다";
    }
}
