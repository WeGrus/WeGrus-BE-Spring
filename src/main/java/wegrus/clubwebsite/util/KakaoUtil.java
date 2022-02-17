package wegrus.clubwebsite.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;
import wegrus.clubwebsite.dto.error.ErrorCode;
import wegrus.clubwebsite.dto.error.ErrorResponse;
import wegrus.clubwebsite.exception.AuthorizationCodeInvalidException;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
public class KakaoUtil {

    @Value("${oauth.kakao.rest-api-key}")
    private String KAKAO_CLIENT_REST_API_KEY;
    @Value("${front.host}")
    private String FRONT_HOST;
    private RestTemplate restTemplate = new RestTemplate();

    public String getUserIdFromKakaoAPI(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        headers.set("Authorization", "Bearer " + accessToken);

        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);
        final ResponseEntity<Map> responseEntity = restTemplate.exchange("https://kapi.kakao.com/v2/user/me", HttpMethod.GET, requestEntity, Map.class);
        final Map response = responseEntity.getBody();
        final String userId = "kakao_" + response.get("id");

        restTemplate.exchange("https://kapi.kakao.com/v1/user/logout", HttpMethod.POST, requestEntity, Map.class);
        return userId;
    }

    public String getAccessTokenFromKakaoAPI(String authorizationCode) {
        HttpHeaders headers = new HttpHeaders();
        headers.set("Content-type", "application/x-www-form-urlencoded;charset=utf-8");
        final String url = "https://kauth.kakao.com/oauth/token";
        final String grant_type = "authorization_code";
        final String redirect_url = FRONT_HOST + "/oauth/kakao/callback";
        HttpEntity<MultiValueMap<String, String>> requestEntity = new HttpEntity<>(null, headers);
        final ResponseEntity<Map> responseEntity;
        try {
            responseEntity = restTemplate.exchange(
                    url + "?grant_type=" + grant_type + "&client_id=" + KAKAO_CLIENT_REST_API_KEY + "&redirect_url=" + redirect_url + "&code=" + authorizationCode
                    , HttpMethod.POST, requestEntity, Map.class);
            final Map response = responseEntity.getBody();
            final String accessToken = (String) response.get("access_token");

            return accessToken;
        } catch(Exception e) {
            final List<ErrorResponse.FieldError> errors = new ArrayList<>();
            errors.add(new ErrorResponse.FieldError("authorizationCode", authorizationCode, ErrorCode.INVALID_AUTHORIZATION_CODE.getMessage()));
            throw new AuthorizationCodeInvalidException(errors);
        }
    }
}
