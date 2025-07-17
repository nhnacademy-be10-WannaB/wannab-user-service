package shop.wannab.userservice.auth.dto.request;

import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SendMessageRequest {

    private String botName;
    private String text;
    private List<Attachment> attachments;

    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Attachment {
        private String title;
        private String text;
        private String titleLink;
        private String botIconImage;
        private String color;
    }

    public static SendMessageRequest unlockCodeMessage(String userId, int unlockCode) {
        return new SendMessageRequest(
                "휴면해제 인증봇",
                null,
                List.of(new SendMessageRequest.Attachment(
                        userId,
                        String.format("인증코드: %d", unlockCode),
                        null,
                        "https://static.dooray.com/static_images/dooray-bot.png",
                        "red"
                ))
        );
    }
}
