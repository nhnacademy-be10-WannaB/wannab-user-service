package shop.wannab.userservice.user.client;

import java.net.URI;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.UriComponentsBuilder;
import shop.wannab.userservice.user.domain.dto.PaycoMemberInfoResponse;
import shop.wannab.userservice.user.domain.dto.PaycoTokenResponse;

@Component
@RequiredArgsConstructor
public class PaycoApiClient {
    private final RestTemplate restTemplate;
    @Value("${payco.client_id}")
    private String clientId;
    @Value("${payco.client_secret}")
    private String clientSecret;


    public PaycoTokenResponse getPaycoToken(String authorizationCode) {
        URI uri = UriComponentsBuilder.fromHttpUrl("https://id.payco.com/oauth2.0/token")
                .queryParam("grant_type", "authorization_code")
                .queryParam("client_id", clientId)
                .queryParam("client_secret", clientSecret)
                .queryParam("code", authorizationCode)
                .build(true)
                .toUri();

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaycoTokenResponse> response = restTemplate.exchange(
                uri,
                HttpMethod.POST,
                request,
                PaycoTokenResponse.class
        );
        return response.getBody();
    }

    public PaycoMemberInfoResponse.Member getPaycoMemberInfo(String accessToken) {
        String url = "https://apis-payco.krp.toastoven.net/payco/friends/find_member_v2.json";

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("client_id", clientId);
        headers.set("access_token", accessToken);

        HttpEntity<Void> request = new HttpEntity<>(headers);

        ResponseEntity<PaycoMemberInfoResponse> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                request,
                PaycoMemberInfoResponse.class
        );

        if (Boolean.TRUE.equals(response.getBody().getHeader().getIsSuccessful())) {
            return response.getBody().getData().getMember();
        } else {
            throw new RuntimeException("회원 정보 조회 실패: " + response.getBody().getHeader().getResultMessage());
        }
    }
}
